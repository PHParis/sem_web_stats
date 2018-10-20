package fr.cnam.ph;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartialResults {
	private static final Logger log = LoggerFactory.getLogger(PartialResults.class);

	Set<String> predicates_seen;
	Set<String> subjects_seen;
	Map<String, Set<String>> predicatesRange; // key is predicate
	Map<String, Set<String>> predicatesDomain; // key is predicate
	Map<String, Set<String>> predicatesSubject; // key is predicate
	Map<String, Integer> predicatesTriples; // key is predicate
	Map<String, Set<String>> subjects_by_type; // key is type
	Integer total_triple_count;
	Integer subjects_count;

	public Set<String> getPredicates_seen() {
		return predicates_seen;
	}

	public Set<String> getSubjects_seen() {
		return subjects_seen;
	}

	public Map<String, Set<String>> getPredicatesRange() {
		return predicatesRange;
	}

	public Map<String, Set<String>> getPredicatesDomain() {
		return predicatesDomain;
	}

	public Map<String, Set<String>> getPredicatesSubject() {
		return predicatesSubject;
	}

	public Map<String, Integer> getPredicatesTriples() {
		return predicatesTriples;
	}

	public Map<String, Set<String>> getSubjects_by_type() {
		return subjects_by_type;
	}

	public Integer getTotal_triple_count() {
		return total_triple_count;
	}

	public Integer getSubjects_count() {
		return subjects_count;
	}

	public PartialResults() {
		predicates_seen = new HashSet<>();
		subjects_seen = new HashSet<>();
		predicatesRange = new HashMap<>(); // key is predicate
		predicatesDomain = new HashMap<>(); // key is predicate
		predicatesSubject = new HashMap<>(); // key is predicate
		predicatesTriples = new HashMap<>(); // key is predicate
		subjects_by_type = new HashMap<>(); // key is type
		total_triple_count = 0;
		subjects_count = -1;
	}

	public void executeAndGetResults(String fileHDT) throws IOException, NotFoundException {
		HDT hdt = null;
		try {
			// Create HDT
//			hdt = HDTManager.mapIndexedHDT(fileHDT, null);
			hdt = HDTManager.loadHDT(fileHDT, null);

		} catch (EOFException e) {
			log.error("EOFException in mapIndexedHDT of executeAndGetResults (with file; " + fileHDT + ")", e);
		}
		if (hdt == null) {
			return;
		}

		try {
//			// Create Jena wrapper on top of HDT.
//			HDTGraph graph = new HDTGraph(hdt);
//			Model model = ModelFactory.createModelForGraph(graph);
//
//			// Use Jena ARQ to execute the query.
//			Query query = QueryFactory.create(sparqlQuery);
//			QueryExecution qe = QueryExecutionFactory.create(query, model);
//
//			try {
//				// Perform the query and output the results, depending on query type
//				if (query.isSelectType()) {
//					ResultSet results = qe.execSelect();
//					while (results.hasNext()) {
			IteratorTripleString it = hdt.search("", "", "");
			while (it.hasNext()) {
				TripleString ts = it.next();
				String s = ts.getSubject().toString();
				String p = ts.getPredicate().toString();
				total_triple_count++;
//						QuerySolution querySolution = (QuerySolution) results.next();
//						Resource sResource = querySolution.getResource("s");
//						Resource pResource = querySolution.getResource("p");
//						String s = sResource.getURI();
//						String p = pResource.getURI();
				subjects_seen.add(s);
				predicates_seen.add(p);
				if (RDF.type.getURI().equals(p)) {
					String o = ts.getObject().toString();
//							RDFNode oNode = querySolution.get("o");
//							String o = oNode.toString();
					if (subjects_by_type.containsKey(o)) {
						subjects_by_type.get(o).add(s);
					} else {
						Set<String> set = new HashSet<>();
						set.add(s);
						subjects_by_type.put(o, set);
					}
				} else if (RDFS.range.getURI().equals(p)) { // s should be a property
//							RDFNode oNode = querySolution.get("o");
//							String o = oNode.toString();
					String o = ts.getObject().toString();
					if (predicatesRange.containsKey(s)) {
						predicatesRange.get(s).add(o);
					} else {
						Set<String> set = new HashSet<>();
						set.add(o);
						predicatesRange.put(s, set);
					}
				} else if (RDFS.domain.getURI().equals(p)) { // s should be a property
//							RDFNode oNode = querySolution.get("o");
//							String o = oNode.toString();
					String o = ts.getObject().toString();
					if (predicatesDomain.containsKey(s)) {
						predicatesDomain.get(s).add(o);
					} else {
						Set<String> set = new HashSet<>();
						set.add(o);
						predicatesDomain.put(s, set);
					}
				} else {
					if (predicatesTriples.containsKey(p)) {
						predicatesTriples.put(p, predicatesTriples.get(p) + 1);
						predicatesSubject.get(p).add(s);
					} else {
						predicatesTriples.put(p, 1);
						Set<String> set = new HashSet<>();
						set.add(s);
						predicatesSubject.put(p, set);
					}
				}
			}
			// in predicatesDomain and predicatesRange there might be predicates used as subject but
			// not used as property! Thus, this might corrupt the stats about the number of 
			// predicate ACTUALLY USED in the data as property and without range (or domain). 
			// Therefore, to prevent negative count, we have to remove those predicates in predicatesRange (and predicatesDomain)
			// that have not been used as property. We use predicate_seen to do so.
			Set<String> keyToRemove = new HashSet<>();
			for (Entry<String, Set<String>> entry : predicatesRange.entrySet()) {
				if (!predicates_seen.contains(entry.getKey())) {
					keyToRemove.add(entry.getKey());
				}
			}
			for (String key : keyToRemove) {
				predicatesRange.remove(key);
			}
			keyToRemove = new HashSet<>();
			for (Entry<String, Set<String>> entry : predicatesDomain.entrySet()) {
				if (!predicates_seen.contains(entry.getKey())) {
					keyToRemove.add(entry.getKey());
				}
			}
			for (String key : keyToRemove) {
				predicatesDomain.remove(key);
			}
			// }
//			} finally {
//				qe.close();				
//			}
		} finally {
			// Close
			hdt.close();
		}
	}

	public static void main(String[] args) throws IOException, NotFoundException {
		if (args == null || args.length <= 0) {
			System.out.println("you must provide a path to an HDT file!");
			return;
		}
		
		// Test of an HDT file
		// This one has a problem: f27f6c53760f6c7c760e3181f2cc4179an
		PartialResults pr = new PartialResults();
//		pr.executeAndGetResults("C:\\Users\\PH\\Downloads\\c8e0b8b6e1bde2aaa1819fd2d1daf2fb.hdt");
		pr.executeAndGetResults(args[0]);
		System.out.println(pr.total_triple_count);
	}

}
