package main.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents YELP review data
 * 
 * @author vbala
 *
 */
public class Dataset 
{
	/**
	 * Map to store customer review ID and customer review text
	 */
	private Map<Integer, Review> reviewIdToReviewMap;
	
	/**
	 * Constructor - Initialize map to store customer review ID and
	 * customer review text
	 */
	public Dataset()
	{
		this.reviewIdToReviewMap = new HashMap<Integer, Review>();
	}

	/**
	 * Create the test data file
	 * 
	 * @param pathToFile
	 * @param pathToOutputFile
	 * @param reviewLimit
	 * @throws IOException
	 */
	public void createTestDataFile(
			String pathToFile,
			String pathToOutputFile, 
			int reviewLimit) throws IOException
	{
		int maxLineCount = reviewLimit + 100;
		
		BufferedWriter outputFile = new BufferedWriter(new FileWriter(pathToOutputFile));
		FileInputStream inputStream = null;
		Scanner sc = null;
		try 
		{
		    inputStream = new FileInputStream(pathToFile);
		    sc = new Scanner(inputStream, "UTF-8");
		    
		    int count = 0;
		    while (sc.hasNextLine()) 
		    {
		    	if (count < reviewLimit)
		    	{
		    		continue;
		    	}
		    	
		    	if (count > maxLineCount)
		    	{
		    		sc.close();
		    		break;
		    	}
		    	if (count == 0)
		    	{
		    		outputFile.write("[" + "\n");
		    	}
		        String line = sc.nextLine();
		        System.out.println(line);
		        if (count != 0 && (count >= reviewLimit) && (count <= maxLineCount))
		        {
		        	outputFile.write(",");
		        }

		        outputFile.write(line);
		        count++;
		    }
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    if (sc != null) {
		    	outputFile.write("]" + "\n");
		    	outputFile.close();
		        sc.close();
		    }
		}
	}

	/**
	 * Create training data file
	 * 
	 * @param pathToOutputFile
	 * @param reviews
	 * @throws IOException
	 */
	public void createTrainingDataFile(
			String pathToOutputFile,
			Map<Integer, Review> reviews) throws IOException
	{		
		BufferedWriter outputFile = new BufferedWriter(new FileWriter(pathToOutputFile));
		for (Integer id : reviews.keySet())
		{
			try
			{
				outputFile.write(reviews.get(id).stars);
				outputFile.write(" " + reviews.get(id).text);
				
				if (!reviews.get(id).id.equals(reviews.keySet().size()-1))
				{
					outputFile.write("\n");
				}
			}
			finally
			{
				outputFile.close();
			}

		}
	}
	
	/**
	 * Stream the file and output to path
	 * 
	 * @param pathToFile
	 * @param pathToOutputFile
	 * @param reviewLimit
	 * @throws IOException
	 */
	public void streamFile(String pathToFile, String pathToOutputFile, int reviewLimit) throws IOException
	{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter(pathToOutputFile));
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {
		    inputStream = new FileInputStream(pathToFile);
		    sc = new Scanner(inputStream, "UTF-8");
		    
		    int count = 0;
		    while (sc.hasNextLine()) {
		    	if (count > reviewLimit)
		    	{
		    		sc.close();
		    		break;
		    	}
		    	if (count == 0)
		    	{
		    		outputFile.write("[" + "\n");
		    	}
		        String line = sc.nextLine();
		        System.out.println(line);
		        if (count != 0 && count <= reviewLimit)
		        {
		        	outputFile.write(",");
		        }

		        outputFile.write(line);
		        count++;
		    }
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    if (sc != null) {
		    	outputFile.write("]" + "\n");
		    	outputFile.close();
		        sc.close();
		    }
		}
	}
	
	/**
	 * Read a JSON file into memory and store review data.
	 * 
	 * @param pathToFile pointer to JSON file containing customer review
	 * 		  data
	 * @return 
	 * @throws IOException error opening file
	 */
	public Map<Integer, Review> readJSON(String pathToFile) throws IOException 
	{
		// Create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();
		
		// Read json like DOM parser
		JsonNode rootNode = objectMapper.readTree(new File(pathToFile));
		
		// Parse json data and populate map
		return parseJSON(rootNode);
	};
	
	/**
	 * Parse the JSON Node returned from reading the file and populate 
	 * {@link #reviewIdToReviewMap} with data for each restaurant review
	 * 
	 * @return reviewIdToReviewMap map containing review IDs and 
	 *         review text
	 */
	private Map<Integer, Review> parseJSON(JsonNode jsonNode) 
	{
		Map<Integer, Review> reviewIdToReviewMap = new HashMap<Integer, Review>();
		
		// Convert raw text to internal objects
		int internalIndex = 0;
		for (int i = 0; i < jsonNode.size()-1; i++)
		{
			JsonNode node = jsonNode.get(i);
			
			String id = node.findValue("review_id").asText();
			String text = node.findValue("text").asText();
			// Skip if no text in review
			if (text.isEmpty())
			{
				continue;
			}
			
			// Clean review text
			String cleanText = cleanText(text);
			int stars = node.findValue("stars").asInt();
			String businessId = node.findValue("business_id").asText();
			// Build review and populate map
			Review review = new Review(id, cleanText, stars, businessId);
			reviewIdToReviewMap.put(internalIndex, review);
			internalIndex++;
		}

		return reviewIdToReviewMap;
	}
	/**
	 * Clean up deficiencies in text which could slow down
	 * algorithm performance; start with punctuation and delimiting
	 * characters
	 * 
	 * @param text the text node to cleanup
	 * @return converted data as an iterator
	 */
	private String cleanText(String text) 
	{
		String cleaned = text.trim();
		cleaned = cleaned.replaceAll("[^a-zA-Z\\s\\r]", "");   //Removes Special Characters and Digits
		cleaned = cleaned.replace("\n", " ");
		return cleaned;
	}

	/**
	 * @return the reviewIdToReviewMap
	 */
	public Map<Integer, Review> getReviewIdToReviewMap() 
	{
		return reviewIdToReviewMap;
	}

	/**
	 * @param reviewIdToReviewMap the reviewIdToReviewMap to set
	 */
	public void setReviewIdToReviewMap(Map<Integer, Review> reviewIdToReviewMap) 
	{
		this.reviewIdToReviewMap = reviewIdToReviewMap;
	}

}
