package fr.cnam.ph;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class Dataset {
	@SerializedName("i")
	private String id;

	@SerializedName("t")
	private int triplesCount;

	@SerializedName("s")
	private int subjectsCount;

	@SerializedName("w")
	private int subjectsWithoutTypeCount;

	@SerializedName("p")
	private int predicatesCount;

	@SerializedName("r")
	private int predicatesWithoutRangeCount;

	@SerializedName("d")
	private int predicatesWithoutDomainCount;

	@SerializedName("sd")
	private ArrayList<SemanticInformation> semanticDict;

//    private Map<String, Map<String, Integer>> semanticDict;
	

	public String getId() {
		return id;
	}

	public int getTriplesCount() {
		return triplesCount;
	}

	public void setTriplesCount(int triplesCount) {
		this.triplesCount = triplesCount;
	}

//	public void incrementTriplesCount() {
//		this.triplesCount++;
//	}
	public int getSubjectsCount() {
		return subjectsCount;
	}

	public void setSubjectsCount(int subjectsCount) {
		this.subjectsCount = subjectsCount;
	}

	public int getSubjectsWithoutTypeCount() {
		return subjectsWithoutTypeCount;
	}

	public void setSubjectsWithoutTypeCount(int subjectsWithoutTypeCount) {
		this.subjectsWithoutTypeCount = subjectsWithoutTypeCount;
	}

	public int getPredicatesCount() {
		return predicatesCount;
	}

	public void setPredicatesCount(int predicatesCount) {
		this.predicatesCount = predicatesCount;
	}

	public int getPredicatesWithoutRangeCount() {
		return predicatesWithoutRangeCount;
	}

	public void setPredicatesWithoutRangeCount(int predicatesWithoutRangeCount) {
		this.predicatesWithoutRangeCount = predicatesWithoutRangeCount;
	}

	public int getPredicatesWithoutDomainCount() {
		return predicatesWithoutDomainCount;
	}

	public void setPredicatesWithoutDomainCount(int predicatesWithoutDomainCount) {
		this.predicatesWithoutDomainCount = predicatesWithoutDomainCount;
	}

	public ArrayList<SemanticInformation> getSemanticDict() {
		if (semanticDict == null)
			semanticDict = new ArrayList<>();
		return semanticDict;
	}

	public Dataset(String id) {
		this.id = id;
		this.predicatesCount = 0;
		this.predicatesWithoutDomainCount = 0;
		this.predicatesWithoutRangeCount = 0;
		this.subjectsCount = 0;
		this.subjectsWithoutTypeCount = 0;
		this.triplesCount = 0;
		this.semanticDict = null;//new HashMap<>();
	}

	public String toJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(this);
		return json;
	}
}
