package fr.cnam.ph;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.n3.turtle.ParserBase;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.lang.LangTurtle;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Stats {

	private static final Logger log = LoggerFactory.getLogger(Stats.class);

	Map<String, Set<String>> owl_properties;
	Set<String> visited_uris;
	Model ontologies;
	ReentrantLock lock;
	long ontologySize;
	long visitedUrisSize;
	Configuration conf;

	public static void main(String[] args) throws IOException {
//		Stats stats = new StatsStream();
//		long startTime1 = System.currentTimeMillis();
//		Stats stats = new StatsSparql();
//		stats.launch(args);
//		long endTime1 = System.currentTimeMillis();
		long startTime2 = System.currentTimeMillis();
		Stats stats = new StatsStream();
		stats.launch(args);
		long endTime2 = System.currentTimeMillis();
//		log.info(String.format("Elapsed time StatsSparql: %s", durationToString(Duration.ofMillis(endTime1 - startTime1))));
		log.info(String.format("Elapsed time StatsStream: %s",
				durationToString(Duration.ofMillis(endTime2 - startTime2))));
	}

	void launch(String[] args) throws IOException {
		// cd D:\dev\java\sem_web_stats && mvn clean compile && mvn clean install && cd hdt-java-package && mvn assembly:single && cd ..

		// cd C:\GoogleDrive\dev\java\Stats_LOD_Wardrobe_HDT && mvn clean compile && mvn
		// clean install && cd OU
		// cd C:\google_drive\dev\java\Stats_LOD_Wardrobe_HDT && mvn clean compile && mvn
		// clean install && cd OU
		// cd C:\dev\java\hdt-java && mvn clean compile && mvn clean install && cd
		// hdt-java-package && mvn assembly:single && cd ..
		// in hdt-java-package dir, get target directory
		// java -server -Xmx300g -Xms8g -Dfile.encoding=UTF-8
		// -Dlog4j.configurationFile=/data2/hamdif/doctorants/ph/wardrobe_java/log4j2.xml
		// -cp "/data2/hamdif/doctorants/ph/wardrobe_java/hdt-java/lib/*"
		// fr.cnam.ph.Stats /data2/hamdif/doctorants/ph/wardrobe_java/conf.json
		long startTime = System.nanoTime();
		log.info("start");
		String configurationFilePath = null;
		if (args != null && args.length > 0) {
			configurationFilePath = args[0];
		} 
		else {
			log.error("You must provide a valid configuration file path");
			throw new FileNotFoundException("You must provide a valid configuration file path");
		}
		conf = Configuration.fromJson(configurationFilePath);
		createDirectoryIfNotExists(conf.hdtDirectory);
		createDirectoryIfNotExists(conf.jsonDirectory);

		if (conf.loadOntologies && isFileExist(conf.ontologiesPath)) {
			log.info("loading ontologies...");
			List<String> triples = Files.lines(Paths.get(conf.ontologiesPath)).collect(Collectors.toList());
			log.info(triples.size() + " triples to load in ontology");
			ontologies = ModelFactory.createDefaultModel();
			double total = (double)triples.size();
			double currentPourcentage = 0d;
			int index = 0;
			for (String triple : triples) {
				try (StringReader reader = new StringReader(triple)) {
					Graph graph = GraphFactory.createDefaultGraph();					
					StreamRDF sink = StreamRDFLib.graph(graph);
					RDFParser.create().source(reader).lang(Lang.NT).parse(sink);
					if (!graph.isEmpty()) {
						Model tmp = ModelFactory.createModelForGraph(graph);
						ontologies.add(tmp);
					}
				} catch (RiotException e) {
					log.info("RiotException with: " + triple);
					log.info(e.toString());
				}
				index++;
				double pourcentage = Math.ceil(index / total * 100d);
				if (pourcentage > currentPourcentage) {
					currentPourcentage = pourcentage;
					log.info(currentPourcentage + "%");
				}
				
			}
			log.info("ontologies loaded with " + ontologies.size() + " triples!");
//			try {
//				ontologies = RDFDataMgr.loadModel(conf.ontologiesPath);
////				// récup num ligne et col
////				// utiliser ça: URLEncoder.encode(s, enc)
////				// remplacer le caractère dans fichier
////				// recommencer
//
////				ontologies = ModelFactory.createDefaultModel();
////				PipedRDFIterator<Triple> iter = new PipedRDFIterator<>();
////		        final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);
////
////		        // PipedRDFStream and PipedRDFIterator need to be on different threads
////		        ExecutorService executor = Executors.newSingleThreadExecutor();
////
////		        // Create a runnable for our parser thread
////		        Runnable parser = new Runnable() {
////
////		            @Override
////		            public void run() {
////		                RDFParser.source(conf.ontologiesPath).parse(inputStream);
////		            }
////		        };
////
////		        // Start the parser on another thread
////		        executor.submit(parser);
////
////		        // We will consume the input on the main thread here
////
////		        // We can now iterate over data as it is parsed, parsing only runs as
////		        // far ahead of our consumption as the buffer size allows
////		        int c = 0;
////		        int er = 0;
////		        while (iter.hasNext()) {
////		        	c++;
////		        	try {
////		        		Triple next = iter.next();
////		        		// Do something with each triple
////		        		System.out.println(next.getSubject().toString());
//////		        		next.getSubject().
//////		        		ontologies.add(s)
////					} catch (RiotException e) {
////						log.error("RiotException when loading ontologies...", e);
////						er++;
////					}
////		        }
////		        System.out.println(c);
////		        System.out.println(er);
////				int i = 2 / 0;
//////				StreamRDF sink = StreamRDFLib.graph(ontologies.getGraph());
//////				RDFDataMgr.parse(sink, conf.ontologiesPath, Lang.TURTLE);
//				log.info("ontologies loaded");
////				log.info("saving to ntriples (only once)");
////				try (FileOutputStream output = new FileOutputStream(conf.ontologiesPath.replace("ttl", "nt"))) {
////					RDFDataMgr.write(output, ontologies, Lang.NTRIPLES);
////				}
////				log.info("saved");
//			} catch (RiotException e) {
//				log.error("RiotException when loading ontologies...", e);
//				throw e;
//			}
		} else {
			ontologies = ModelFactory.createDefaultModel();
		}
		ontologySize = ontologies.size();
		if (isFileExist(conf.visitedUrisPath)) {
			log.info("loading visited uris");
			visited_uris = Files.lines(Paths.get(conf.visitedUrisPath)).collect(Collectors.toSet());
			log.info("visited uris loaded");
		} else {
			visited_uris = new HashSet<>();
		}
		visitedUrisSize = visited_uris.size();
		owl_properties = get_owl_properties();
		List<String> datasetNames = getDatasetNames(conf.datasetNamesLimit);
		Set<String> idsToProcessed = datasetNames.stream().map(x -> getIdFromDatasetName(x)).filter(x -> !x.isEmpty())
				.collect(Collectors.toSet());
		if (!conf.forceRecomputation) {
			Set<String> idsProcessed = getIdsFromProcessedJsonFiles();
			idsToProcessed.removeAll(idsProcessed);
		}

		final List<String> datasetErrors = new CopyOnWriteArrayList<>();
		if (conf.passPreviousDatasetInError) {
			Set<String> idInErrorLastTime = getDatasetErrors();
			datasetErrors.addAll(idInErrorLastTime);
			idsToProcessed.removeAll(idInErrorLastTime);
		}
		if (idsToProcessed.isEmpty()) {
			log.info("no files to process !");
			return;
		}
		// id problems:
		// c2a6cdc658256de3699bc3587ddc484d
		log.info("downloading hdt files");
		double total = idsToProcessed.size();
		int countPercent = 0;
		int curPercent = 0;
		for (String id : idsToProcessed) {
			datasetIdToHdtFile(id);
			countPercent++;
			int percent = (int) Math.ceil(countPercent / total * 100d);
			if (percent > (curPercent)) {
				curPercent = percent;
				log.info(curPercent + "%");
			}
		}
		log.info("hdt files downloaded");
//		Map<String, Long> hdtFilesSizeById = getHdtFileSizeById(idsToProcessed);
//		// key is the id of the dataset, value array is the (i)size of the file in bytes (long)
//		// (ii) the time spend on it in Duration
//		Map<String, Object[]> hdtFilesSizeProcessedById = new HashMap<>();
//		AtomicLong totalSizeProcessed = new AtomicLong(0);
//		AtomicLong totalMilisSpendDuringProcess = new AtomicLong(0);
//		AtomicLong remainingSize = new AtomicLong(hdtFilesSizeById.entrySet().stream().mapToLong(x -> x.getValue()).sum());

		log.info("# datasets: " + (int) total);
		log.info("starting loop");
		AtomicInteger count = new AtomicInteger(0);
		AtomicInteger currentPercent = new AtomicInteger(0);
		lock = new ReentrantLock();
		final long loop_start = System.currentTimeMillis();
		try {
			idsToProcessed.parallelStream().forEach((datasetId) -> {
				try {
					// long loop_start = System.currentTimeMillis();
					String jsonPath = datasetIdToJsonFile(datasetId);
					if (conf.forceRecomputation || !isFileExist(jsonPath)) {
						String fileHDT = null;
						try {
							fileHDT = datasetIdToHdtFile(datasetId);
						} catch (IOException e) {
							log.error("IOException in datasetIdToHdtFile (" + datasetId + ")", e);
							datasetErrors.add(datasetId);
						}
						if (fileHDT != null) {
							Dataset ds = null;
							try {
								ds = ComputeDatasetStats(fileHDT, datasetId);
							} catch (IOException e) {
								log.error("IOException in ComputeDatasetStats (" + datasetId + ")", e);
								datasetErrors.add(datasetId);
							} catch (RiotException e) {
								log.error("RiotException in ComputeDatasetStats (" + datasetId + ")", e);
								datasetErrors.add(datasetId);
							}
							if (ds != null && ds.getTriplesCount() > 0) {
								String jsonText = ds.toJson();
								try {
									saveJsonToFile(jsonText, jsonPath);
								} catch (FileNotFoundException e) {
									log.error("FileNotFoundException in saveJsonToFile (" + datasetId + ")", e);
									datasetErrors.add(datasetId);
								}
							}
						}
					}
				} catch (IllegalArgumentException e) {
					log.error("IllegalArgumentException in the main loop (" + datasetId + ")", e);
					datasetErrors.add(datasetId);
				} catch (Exception e) {
					log.error("Exception in the main loop (" + datasetId + ")", e);
					datasetErrors.add(datasetId);
				}
				lock.lock();
				try {
					int percent = (int) Math.ceil(count.incrementAndGet() / total * 10000d);
//		            long size = hdtFilesSizeById.remove(datasetId);
//		            long sizeProcessed = totalSizeProcessed.addAndGet(size);
//		            remainingSize.set(remainingSize.get() - size);
//		            long timeSpend = totalMilisSpendDuringProcess.addAndGet(elapsed_time);
					if ((percent / 100d) > (currentPercent.doubleValue() / 100d)) {
						currentPercent.set(percent);
//			            long estimated_time_to_complete = timeSpend * remainingSize.get() / sizeProcessed;
//			            Duration duration = Duration.ofMillis(estimated_time_to_complete);
						long now = System.currentTimeMillis();
						long elapsed_time = now - loop_start;
						double estimated_time_to_complete = 100d * elapsed_time / (percent / 100d);
						long remaining_time = (long) (estimated_time_to_complete - elapsed_time);
						Duration duration = Duration.ofMillis(remaining_time);
						log.info((currentPercent.doubleValue() / 100d) + "% (ETA: " + durationToString(duration) + ")");
					}
				} finally {
					lock.unlock();
				}
			});
		} catch (OutOfMemoryError e) {
			log.error("OutOfMemoryError in the main loop", e);
		} catch (Exception e) {
			log.error("Exception in the main loop", e);
		}

		long newOntologySize = ontologies.size();
		if (newOntologySize > ontologySize) {
			try {
				saveOntology();
			} catch (FileNotFoundException e) {
				log.error("FileNotFoundException in saveOntology", e);
			} catch (IOException e) {
				log.error("IOException in saveOntology", e);
			}
		}
		int newVisitedUrisSize = visited_uris.size();
		if (newVisitedUrisSize > visitedUrisSize) {
			try {
				saveVisitedUris();
			} catch (IOException e) {
				log.error("IOException in saveVisitedUris", e);
			}
		}
		saveDatasetErrors(datasetErrors);
//		for (String datasetId : datasetErrors) {
//			log.info(String.format("error on dataset: %s", datasetId));
//		}
		long endTime = System.nanoTime();
		long durationInMs = (endTime - startTime) / 1000000; // divide by 1000000 to get milliseconds.
		Duration duration = Duration.ofMillis(durationInMs);
		log.info("end in: " + durationToString(duration));
	}

	void saveOntology() throws FileNotFoundException, IOException {
		log.info("saving ontologies");
		try (FileOutputStream output = new FileOutputStream(conf.ontologiesPath.replace("ttl", "nt"))) {
			RDFDataMgr.write(output, ontologies, Lang.NTRIPLES);
		}
		log.info("ontologies saved");
	}

	void saveVisitedUris() throws IOException {
		log.info("saving visited uris");
		Files.write(Paths.get(conf.visitedUrisPath), visited_uris, StandardOpenOption.CREATE);
		log.info("visited uris saved");
	}

	void saveDatasetErrors(List<String> datasetErrors) throws IOException {
		if (!datasetErrors.isEmpty()) {
			log.info("saving dataset ids with errors");
			Files.write(Paths.get(conf.datasetErrorsPath), datasetErrors, StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
			log.info("dataset ids with errors saved");
		}
	}

	Set<String> getDatasetErrors() throws IOException {
		log.info("getting dataset ids with errors");
		return Files.lines(Paths.get(conf.datasetErrorsPath)).collect(Collectors.toSet());
	}

	void createDirectoryIfNotExists(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	void saveJsonToFile(String jsonText, String jsonPath) throws FileNotFoundException {
		try (PrintWriter out = new PrintWriter(jsonPath)) {
			out.println(jsonText);
		}
	}

	String getIdFromDatasetName(String datasetName) {
		String uri = datasetName.substring(0, datasetName.length() - 8);
		return uri.replace("http://ldf.lodlaundromat.org/", "");
	}

	String datasetIdToHdtFile(String datasetId) throws IOException {
		// get a dataset id and return the hdt file path on the local system
		// download it if not present!

		String fileName = conf.hdtDirectory + datasetId + ".hdt";
		if (conf.forceHDTDownload || !isFileExist(fileName)) {
			downloadHdtFile(datasetId, fileName);
		}
		return fileName;
	}

	String datasetIdToJsonFile(String datasetId) {
		String fileName = conf.jsonDirectory + datasetId + ".json";
		return fileName;
	}

	Map<String, Long> getHdtFileSizeById(Set<String> datasetIdsToProcess) {
		// we suppose all hdt files needed have been downloaded.
		log.info("getting sizes of hdt files downloaded");
		File folder = new File(conf.hdtDirectory);
		File[] listOfFiles = folder.listFiles();
		Map<String, Long> hdtSizeById = new HashMap<>();
		for (File file : listOfFiles) {
			if (file.getName().endsWith(".hdt")) {
				String id = file.getName().replace(".hdt", "");
				if (datasetIdsToProcess.contains(id)) {
					long size = file.length();
					hdtSizeById.put(id, size);
				}
			}
		}
		return hdtSizeById;
	}

	Set<String> getIdsFromProcessedJsonFiles() {
		// we suppose all hdt files needed have been downloaded.
		log.info("getting dataset ids already processed");
		File folder = new File(conf.jsonDirectory);
		File[] listOfFiles = folder.listFiles();
		Set<String> idsProcessed = new HashSet<>();
		for (File file : listOfFiles) {
			if (file.getName().endsWith(".json")) {
				String id = file.getName().replace(".json", "");
				idsProcessed.add(id);
			}
		}
		return idsProcessed;
	}

	void downloadHdtFile(String datasetId, String fileName) throws IOException {
		String url = "http://download.lodlaundromat.org/" + datasetId + "?type=hdt";
		log.debug("downloading: " + url);
		URL website = new URL(url);
		try (ReadableByteChannel rbc = Channels.newChannel(website.openStream())) {
			try (FileOutputStream fos = new FileOutputStream(fileName)) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
		} catch (java.io.FileNotFoundException e) {
			log.error("FileNotFoundException when downloading " + datasetId, e);
		}
	}

	static String durationToString(Duration duration) {
		String result = "";
		if (duration.toDays() > 0) {
			long days = duration.toDays();
			result += String.format("%d days ", days);
			duration = duration.minusDays(days);
		}
		long hours = duration.toHours();
		if (hours > 9)
			result += String.format("%d:", hours);
		else
			result += String.format("0%d:", hours);
		duration = duration.minusHours(hours);

		long min = duration.toMinutes();
		if (min > 9)
			result += String.format("%d:", min);
		else
			result += String.format("0%d:", min);
		duration = duration.minusMinutes(min);

		long sec = duration.getSeconds();
		if (sec > 9)
			result += String.format("%d ", sec);
		else
			result += String.format("0%d ", sec);
		duration = duration.minusSeconds(sec);

		long ms = duration.toMillis();
		result += String.format("%d ms", ms);
		return result;
	}

	boolean isFileExist(String path) {
		File f = new File(path);
		return f.exists() && !f.isDirectory();
	}

	List<String> getDatasetNames(Integer limit) throws IOException {
		// e.g. http://ldf.lodlaundromat.org/31b129841b2e26ae5df22aab773bf4c8#dataset
//		// d1c8ececa2dce9b75d1929f5f21c55b1
//		// 6a9a47310a8fc9c5978c70a3f80ed11f out ot mem
//		List<String> list = new ArrayList<>();
//		list.add("http://ldf.lodlaundromat.org/c8e0b8b6e1bde2aaa1819fd2d1daf2fb#dataset");
//		return list;
		if (limit != null && limit > 0)
			return Files.lines(Paths.get(conf.datasetNamesFilePath)).limit(limit).collect(Collectors.toList());
		return Files.lines(Paths.get(conf.datasetNamesFilePath)).collect(Collectors.toList());

	}

	Dataset ComputeDatasetStats(String fileHDT, String datasetId) throws Exception {
		Dataset ds = new Dataset(datasetId);
		PartialResults pr = getPartialResults(fileHDT);
//        PartialResults pr = new PartialResults();
//        pr.executeAndGetResults(fileHDT, sparqlQuery);
		ds.setTriplesCount(pr.getTotal_triple_count());
		Set<String> predicates_seen = pr.getPredicates_seen();
		Set<String> subjects_seen = pr.getSubjects_seen();
		Map<String, Set<String>> predicatesRange = pr.getPredicatesRange(); // key is predicate
		Map<String, Set<String>> predicatesDomain = pr.getPredicatesDomain(); // key is predicate
		Map<String, Set<String>> predicatesSubject = pr.getPredicatesSubject(); // key is predicate
		Map<String, Integer> predicatesTriples = pr.getPredicatesTriples(); // key is predicate
		Map<String, Set<String>> subjects_by_type = pr.getSubjects_by_type(); // key is type

		if (ds.getTriplesCount() <= 0) {
			return ds;
		}

		ds.setPredicatesCount(predicates_seen.size());
		if (pr.getSubjects_count() == -1)
			ds.setSubjectsCount(subjects_seen.size());
		else
			ds.setSubjectsCount(pr.getSubjects_count());
		Set<String> subjectsWithType = subjects_by_type.entrySet().stream().flatMap(x -> x.getValue().stream())
				.collect(Collectors.toSet());
		ds.setSubjectsWithoutTypeCount(ds.getSubjectsCount() - subjectsWithType.size());

		// look before in ontology and eventualy try to download info
		for (String p : predicates_seen) {
			String main_uri = p;
			if (p.contains("#")) {
				// remove fragment to prevent downloading several times the same uri
				main_uri = p.split("#")[0];
			}
			if (!subjects_seen.contains(p) && !visited_uris.contains(main_uri)) {
				// we have to try to dereference p!
				boolean is_in_onto = ontologies.contains(ontologies.createResource(p), null);
				if (!is_in_onto) {
					try {
						Model m = RDFDataMgr.loadModel(main_uri);
						is_in_onto = m.contains(m.createResource(p), null);
						if (!m.isEmpty()) {
							ontologies.add(m);
						}
					} catch (RiotNotFoundException e) {
						log.debug("RiotNotFoundException with " + p, e);
					} catch (RiotException e) {
						log.debug("RiotException with " + p, e);
					} catch (HttpException e) {
						log.debug("RiotNotFoundException with " + p, e);
					}
				}
				// now either p was already in onto, either we try to add it by dereferencing it
				// if p is not in onto we can't to anything more
				if (is_in_onto) {
					StmtIterator iter = ontologies.listStatements(ontologies.createResource(p), null, (RDFNode) null);
					while (iter.hasNext()) {
						Statement statement = iter.next();
						if (RDF.type.getURI().equals(statement.getPredicate().getURI())) {
							String type = statement.getObject().toString();
							if (subjects_by_type.containsKey(type)) {
								subjects_by_type.get(type).add(p);
							} else {
								Set<String> set = new HashSet<>();
								set.add(p);
								subjects_by_type.put(type, set);
							}
						} else if (RDFS.range.getURI().equals(statement.getPredicate().getURI())) {
							if (predicatesRange.containsKey(p)) {
								predicatesRange.get(p).add(statement.getObject().toString());
							} else {
								Set<String> set = new HashSet<>();
								set.add(statement.getObject().toString());
								predicatesRange.put(p, set);
							}
						} else if (RDFS.domain.getURI().equals(statement.getPredicate().getURI())) {
							if (predicatesDomain.containsKey(p)) {
								predicatesDomain.get(p).add(statement.getObject().toString());
							} else {
								Set<String> set = new HashSet<>();
								set.add(statement.getObject().toString());
								predicatesDomain.put(p, set);
							}
						}
					}
				}
				visited_uris.add(main_uri);
			}
		}

		Set<String> predicatesWithRange = predicatesRange.entrySet().stream().map(x -> x.getKey())
				.collect(Collectors.toSet());
		ds.setPredicatesWithoutRangeCount(predicates_seen.size() - predicatesWithRange.size());
//		if (ds.getPredicatesWithoutRangeCount() <= 0) {
//			throw new Exception("range negative for: " + ds.getId());
//		}
		Set<String> predicatesWithDomain = predicatesDomain.entrySet().stream().map(x -> x.getKey())
				.collect(Collectors.toSet());
		ds.setPredicatesWithoutDomainCount(predicates_seen.size() - predicatesWithDomain.size());
//		if (ds.getPredicatesWithoutRangeCount() <= 0) {
//			throw new Exception("domain negative for: " + ds.getId());
//		}

		for (String owl_property : owl_properties.get("predicate_types")) {
			// e.g. owl_property = FunctionalProperty
			if (subjects_by_type.containsKey(owl_property)) {
				int triples_count = 0;
				int predicates_count = subjects_by_type.get(owl_property).size();
				Set<String> subjects_set = new HashSet<>();
				for (String p : subjects_by_type.get(owl_property)) {
					if (predicatesTriples.containsKey(p)) {
						triples_count += predicatesTriples.get(p);
					}
					if (predicatesSubject.containsKey(p)) {
						subjects_set.addAll(predicatesSubject.get(p));
					}
				}
				int subjects_count = subjects_set.size();
				Map<String, Integer> tmp_dict = new HashMap<>();
				if (triples_count > 0)
					tmp_dict.put("triples_count", triples_count);
				if (predicates_count > 0)
					tmp_dict.put("predicates_count", predicates_count);
				if (subjects_count > 0)
					tmp_dict.put("subjects_count", subjects_count);
				if (!tmp_dict.isEmpty()) {
					SemanticInformation se = new SemanticInformation();
					se.name = owl_property;
					if (triples_count > 0)
						se.triplesCount = tmp_dict.get("triples_count");
					if (subjects_count > 0)
						se.subjectsCount = tmp_dict.get("subjects_count");
					if (predicates_count > 0)
						se.predicatesCount = tmp_dict.get("predicates_count");
//					ArrayList<SemanticInformation> sd = ds.getSemanticDict();
//					if (sd == null) {
//						sd = new ArrayList<>();
//					}
					ds.getSemanticDict().add(se);// owl_property, tmp_dict);
				}
			}
		}
		for (String owl_predicate : owl_properties.get("owl_predicates")) {
			if (predicates_seen.contains(owl_predicate)) {
				Map<String, Integer> tmp_dict = new HashMap<>();
				tmp_dict.put("triples_count", predicatesTriples.get(owl_predicate));
				tmp_dict.put("subjects_count", predicatesSubject.get(owl_predicate).size());
				if (!tmp_dict.isEmpty()) {
					SemanticInformation se = new SemanticInformation();
					se.name = owl_predicate;
					se.triplesCount = tmp_dict.get("triples_count");
					se.subjectsCount = tmp_dict.get("subjects_count");
//					ArrayList<SemanticInformation> sd = ds.getSemanticDict();
//					if (sd == null) {
//						sd = new ArrayList<>();
//					}
					ds.getSemanticDict().add(se);// owl_property, tmp_dict);
				}
//				ds.getSemanticDict().put(owl_predicate, tmp_dict);
			}
		}

		for (String owl_class : owl_properties.get("owl_classes")) {
			if (subjects_by_type.containsKey(owl_class)) {
				int count = subjects_by_type.get(owl_class).size();
				Map<String, Integer> tmp_dict = new HashMap<>();
				tmp_dict.put("triples_count", count);
				tmp_dict.put("subjects_count", count);
				if (!tmp_dict.isEmpty()) {
					SemanticInformation se = new SemanticInformation();
					se.name = owl_class;
					se.triplesCount = tmp_dict.get("triples_count");
					se.subjectsCount = tmp_dict.get("subjects_count");
//					ArrayList<SemanticInformation> sd = ds.getSemanticDict();
//					if (sd == null) {
//						sd = new ArrayList<>();
//					}
					ds.getSemanticDict().add(se);// owl_property, tmp_dict);
				}
//				ds.getSemanticDict().put(owl_class, tmp_dict);
			}
		}

		return ds;
	}

	abstract PartialResults getPartialResults(String fileHDT) throws IOException, NotFoundException;

	Map<String, Set<String>> get_owl_properties() {
		String rdfUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
		String rdfsUri = "http://www.w3.org/2000/01/rdf-schema";
		Model rdfGraph = RDFDataMgr.loadModel(rdfUri);
		Model rdfsGraph = RDFDataMgr.loadModel(rdfsUri);
		Model owl_graph = RDFDataMgr.loadModel("https://www.w3.org/2002/07/owl");
		owl_graph = owl_graph.add(rdfGraph);
		owl_graph = owl_graph.add(rdfsGraph);
		Reasoner reasoner = ReasonerRegistry.getTransitiveReasoner();
		InfModel inf = ModelFactory.createInfModel(reasoner, owl_graph);

		Set<String> predicate_types = new HashSet<>();
		ResIterator iter = inf.listSubjectsWithProperty(RDFS.subClassOf, RDF.Property);
		while (iter.hasNext()) {
			Resource resource = (Resource) iter.next();
			predicate_types.add(resource.getURI());
		}

		Set<String> owl_predicates = new HashSet<>();
		iter = inf.listSubjectsWithProperty(RDF.type, RDF.Property);
		while (iter.hasNext()) {
			Resource resource = (Resource) iter.next();
			owl_predicates.add(resource.getURI());
		}

		Set<String> owl_classes = new HashSet<>();
		iter = inf.listSubjectsWithProperty(RDF.type, RDFS.Class);
		while (iter.hasNext()) {
			Resource resource = (Resource) iter.next();
			owl_classes.add(resource.getURI());
		}

		Map<String, Set<String>> results = new HashMap<>();
		results.put("predicate_types", predicate_types);
		results.put("owl_predicates", owl_predicates);
		results.put("owl_classes", owl_classes);
		return results;
	}
}
