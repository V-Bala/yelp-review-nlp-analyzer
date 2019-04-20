package main.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Dataset {
	
	/**
	 * Map to store customer review ID and customer review text
	 */
	private Map<Long, Object> reviewIdToReviewTextMap;
	
	/**
	 * Constructor - Initialize map to store customer review ID and
	 * customer review text
	 */
	public Dataset()
	{
		this.reviewIdToReviewTextMap = new HashMap<Long, Object>();
	}
	
	/**
	 * Read a JSON file into memory and store review data.
	 * 
	 * @param fileName pointer to JSON file containing customer review
	 * 		  data
	 * @throws IOException error opening file
	 */
	public void readJSON(String fileName) throws IOException {
		
		// Read json file data to String
		byte[] jsonData = Files.readAllBytes(Paths.get(fileName));
		
		// Create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();
		
		// Read json like DOM parser
		JsonNode rootNode = objectMapper.readTree(jsonData);
		
		// Parse json data and populate map
		this.reviewIdToReviewTextMap = parseJSON(rootNode);
	};
	
	/**
	 * Parse the JSON array returned from reading the file and populate 
	 * {@link #reviewIdToReviewTextMap} with the review_id and 
	 * review_text.
	 * 
	 * @return reviewIdToReviewTextMap map containing review IDs and 
	 *         review text
	 */
	private Map<Long, Object> parseJSON(JsonNode jsonNode) {
		Map<Long, Object> reviewIdToReviewTextMap = new HashMap<Long, Object>();
		
		// Extract review IDs and review text field
		JsonNode reviewIdNode = jsonNode.path("review_id");
		JsonNode reviewTextNode = jsonNode.path("review_text");
		
		// Clean review text field and use an iterator for easy traversal
		Iterator<JsonNode> reviewIds = reviewIdNode.elements();
		Iterator<JsonNode> reviewTexts = clean(reviewTextNode);
		while (reviewIds.hasNext())
		{
			// Convert data into Java objects
			Long reviewId = reviewIds.next().asLong();
			String reviewText = reviewTexts.next().asText();
			
			// Populate map
			reviewIdToReviewTextMap.put(reviewId, reviewText);
		}
		
		return reviewIdToReviewTextMap;
	}

	/**
	 * TODO Clean up deficiencies in text which could slow down
	 * algorithm performance; start with punctuation and delimiting
	 * characters
	 * 
	 * @param reviewTextNode the JSON node to cleanup
	 * @return converted data as an iterator
	 */
	private Iterator<JsonNode> clean(JsonNode reviewTextNode) {
		return null;
	}

	/**
	 * @return the reviewIdToReviewTextMap
	 */
	public Map<Long, Object> getReviewIdToReviewTextMap() {
		return reviewIdToReviewTextMap;
	}

	/**
	 * @param reviewIdToReviewTextMap the reviewIdToReviewTextMap to set
	 */
	public void setReviewIdToReviewTextMap(Map<Long, Object> reviewIdToReviewTextMap) {
		this.reviewIdToReviewTextMap = reviewIdToReviewTextMap;
	};
}
