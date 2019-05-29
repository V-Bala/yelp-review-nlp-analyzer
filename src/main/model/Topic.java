package main.model;

import java.util.Arrays;
import java.util.List;

public class Topic {
	// TODO Use word frequency matrix to predefine categories
	// FOOD
	// SERVICE
	// ATMOSPHERE
	// LOCATION
	// VIBE
	// DRINKS
	// STAFF
	// SCENE
	// EXPERIENCE

	private static String FOOD = "food";
	private static String SERVICE = "service";
	private static String ATMOSPHERE = "atmosphere";
//	private static String LOCATION = "location";
	private static String VIBE = "vibe";
	private static String STAFF = "staff";
//	private static String SCENE = "scene";
//	private static String EXPERIENCE = "experience";

	public static List<String> TOPICS = Arrays.asList(FOOD, SERVICE, ATMOSPHERE, VIBE, STAFF);

}
