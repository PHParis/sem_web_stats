package fr.cnam.ph;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsStream extends Stats {
	
	private static final Logger log = LoggerFactory.getLogger(StatsStream.class);

	@Override
	PartialResults getPartialResults(String fileHDT) throws IOException, NotFoundException {
		String sparqlQuery = "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }";
        PartialResults pr = new PartialResults();
        pr.executeAndGetResults(fileHDT);
        return pr;
	}

	

}
