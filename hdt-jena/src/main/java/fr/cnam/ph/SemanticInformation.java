package fr.cnam.ph;

import com.google.gson.annotations.SerializedName;

public class SemanticInformation {
	@SerializedName("n")
	String name;

	@SerializedName("t")
	int triplesCount;

	@SerializedName("s")
	int subjectsCount;

	@SerializedName("p")
	int predicatesCount;

}
