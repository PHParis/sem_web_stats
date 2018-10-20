package fr.cnam.ph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;

public class Configuration {
//	public String hdtDirectory = "C:\\google_drive\\dev\\python\\hydra-py-master3\\data\\hdt\\";
//	public String jsonDirectory = "C:\\google_drive\\dev\\python\\hydra-py-master3\\data\\files3\\";
//	public String datasetNamesFilePath = "C:\\google_drive\\dev\\python\\hydra-py-master3\\data\\dataset_names";
////	static String ontologiesPath = "C:\\google_drive\\dev\\python\\hydra-py-master3\\data\\ontologies.ttl";
////	static String visitedUrisPath = "C:\\google_drive\\dev\\python\\hydra-py-master3\\data\\visited_uris";
//	public String ontologiesPath = "C:\\dev\\ontologies.ttl";
//	public String visitedUrisPath = "C:\\dev\\visited_uris";
//	public Integer datasetNamesLimit = 20;
//	public boolean downloadOntologies = false;
//	public boolean forceRecomputation = true;
	public String hdtDirectory;
	public String jsonDirectory;
	public String datasetNamesFilePath;
	public String ontologiesPath;
	public String visitedUrisPath;
	public String datasetErrorsPath;
	public Integer datasetNamesLimit;
	public boolean loadOntologies;
	public boolean forceRecomputation;
	public boolean forceHDTDownload;
	public boolean passPreviousDatasetInError;
	
	public static Configuration fromJson(String configurationFilePath) throws IOException {
		Gson gson = new Gson();
		Configuration conf = new Configuration();
		List<String> lines = Files.readAllLines(Paths.get(configurationFilePath));
		String json = String.join("", lines);
		conf = gson.fromJson(json, conf.getClass());
		return conf;		
	}
}
