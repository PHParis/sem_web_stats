package fr.cnam.ph;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdtjena.cmd.HDTSparql;

public class StatsSparql extends Stats {

	@Override
	PartialResults getPartialResults(String fileHDT) throws IOException {
		HDTSparql hdtSparql = new HDTSparql();
		hdtSparql.fileHDT = fileHDT;
		PartialResults pr = new PartialResults();
//		// pr.total_triple_count
//		hdtSparql.sparqlQuery = "SELECT (COUNT(*) AS ?count) { ?s ?p ?o . }";
//		ResultSet rs = hdtSparql.executeAndGetResults();
//		while (rs.hasNext()) {
//			QuerySolution querySolution = (QuerySolution) rs.next();
//			Literal countNode = querySolution.get("count").asLiteral();
//			pr.total_triple_count = countNode.getInt();
//		}
//		predicates_seen = new HashSet<>();
//    	predicatesTriples = new HashMap<>(); // key is predicate
		hdtSparql.sparqlQuery = "SELECT ?p (COUNT(*) AS ?count) { ?s ?p ?o . } GROUP BY ?p";
		ResultSet rs = hdtSparql.executeAndGetResults();
		while (rs.hasNext()) {
			QuerySolution querySolution = (QuerySolution) rs.next();
			Resource p = querySolution.get("p").asResource();
			Literal count = querySolution.get("count").asLiteral();
			// TODO: Maybe we should check if there is no bug
			// and if it's realy the first time 'p' is put in the hashmap...
			pr.predicatesTriples.put(p.toString(), count.getInt());
			pr.predicates_seen.add(p.toString());
		}
		// pr.total_triple_count
		pr.total_triple_count = pr.predicatesTriples
				.entrySet().stream().mapToInt(x -> x.getValue()).sum();
//		hdtSparql.sparqlQuery = "SELECT DISTINCT ?p { ?s ?p ?o . }";
//		rs = hdtSparql.executeAndGetResults();
//		while (rs.hasNext()) {
//			QuerySolution querySolution = (QuerySolution) rs.next();
//			Resource p = querySolution.get("p").asResource();
//			pr.predicates_seen.add(p.toString());
//		}
		
////    	subjects_seen = new HashSet<>();
//		// TODO: replace this set by a simple count.
//		// In this set we only need to add properties that are subject.
		//// FIXME: WE HAD TO HAD A GLOBAL COUNT OF SUBJECTS
		hdtSparql.sparqlQuery = "SELECT (COUNT(DISTINCT ?s) AS ?count) { ?s ?p ?o . }";
		rs = hdtSparql.executeAndGetResults();
		while (rs.hasNext()) {
			QuerySolution querySolution = (QuerySolution) rs.next();
			Literal p = querySolution.get("count").asLiteral();
			pr.subjects_count = p.getInt();
		}
//		hdtSparql.sparqlQuery = "SELECT DISTINCT ?s { ?s ?p ?o . }";
//		rs = hdtSparql.executeAndGetResults();
//		while (rs.hasNext()) {
//			QuerySolution querySolution = (QuerySolution) rs.next();
//			Resource p = querySolution.get("s").asResource();
//			pr.subjects_seen.add(p.toString());
//		}
//    	predicatesRange = new HashMap<>(); // key is predicate
		if (pr.predicates_seen.contains("http://www.w3.org/2000/01/rdf-schema#range")) {
			hdtSparql.sparqlQuery = "SELECT ?p ?type { ?p <http://www.w3.org/2000/01/rdf-schema#range> ?type . }";
			rs = hdtSparql.executeAndGetResults();
			while (rs.hasNext()) {
				QuerySolution querySolution = (QuerySolution) rs.next();
				Resource p = querySolution.get("p").asResource();
				Resource type = querySolution.get("type").asResource();
				if (pr.predicatesRange.containsKey(type.toString()))
					pr.predicatesRange.get(type.toString()).add(p.toString());
				else {
					Set<String> set = new HashSet<>();
					set.add(p.toString());
					pr.predicatesRange.put(type.toString(), set);
					pr.subjects_seen.add(p.toString());
				}
			}
		}
//    	predicatesDomain = new HashMap<>(); // key is predicate
		if (pr.predicates_seen.contains("http://www.w3.org/2000/01/rdf-schema#domain")) {
			hdtSparql.sparqlQuery = "SELECT ?p ?type { ?p <http://www.w3.org/2000/01/rdf-schema#domain> ?type . }";
			rs = hdtSparql.executeAndGetResults();
			while (rs.hasNext()) {
				QuerySolution querySolution = (QuerySolution) rs.next();
				Resource p = querySolution.get("p").asResource();
				Resource type = querySolution.get("type").asResource();
				if (pr.predicatesDomain.containsKey(type.toString()))
					pr.predicatesDomain.get(type.toString()).add(p.toString());
				else {
					Set<String> set = new HashSet<>();
					set.add(p.toString());
					pr.predicatesDomain.put(type.toString(), set);
					pr.subjects_seen.add(p.toString());
				}
			}
		}
//      subjects_by_type = new HashMap<>(); // key is type
		if (pr.predicates_seen.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
			hdtSparql.sparqlQuery = "SELECT ?s ?type { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type . }";
			rs = hdtSparql.executeAndGetResults();
			while (rs.hasNext()) {
				QuerySolution querySolution = (QuerySolution) rs.next();
				Resource p = querySolution.get("s").asResource();
				Resource type = querySolution.get("type").asResource();
				if (pr.subjects_by_type.containsKey(type.toString()))
					pr.subjects_by_type.get(type.toString()).add(p.toString());
				else {
					Set<String> set = new HashSet<>();
					set.add(p.toString());
					pr.subjects_by_type.put(type.toString(), set);
					pr.subjects_seen.add(p.toString());
				}
			}
		}
//    	predicatesSubject = new HashMap<>(); // key is predicate
//		hdtSparql.sparqlQuery = "SELECT ?p ?s { ?s ?p ?o . }";
//		rs = hdtSparql.executeAndGetResults();
//		while (rs.hasNext()) {
//			QuerySolution querySolution = (QuerySolution) rs.next();
//			Resource p = querySolution.get("p").asResource();
//			Resource s = querySolution.get("s").asResource();
//			if (pr.predicatesSubject.containsKey(p.toString()))
//				pr.predicatesSubject.get(p.toString()).add(s.toString());
//			else {
//				Set<String> set = new HashSet<>();
//				set.add(s.toString());
//				pr.predicatesSubject.put(p.toString(), set);
//			}
//		}
		return pr;
	}

}
