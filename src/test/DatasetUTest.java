package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;
import main.algorithm.NLPHelper;
import main.model.Dataset;
import main.model.Review;
import main.model.ReviewResult;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.util.Span;

public class DatasetUTest 
{
	
	private final Logger LOGGER = Logger.getLogger(DatasetUTest.class.getSimpleName());
	
	private final String REVIEW_DATA_FILE = "C:/Users/Vijay/yelp-dataset/review.json";
	private final String TRAINING_DATA_FILE_BASE = "C:/Users/Vijay/yelp-dataset/review_train.txt";
	
	// Set 1
	private final String OUTPUT_FILE_1 = "C:/Users/Vijay/yelp-dataset/review_1.json";
	private final String OUTPUT_FILE_50 = "C:/Users/Vijay/yelp-dataset/review_50.json";
	
	// Set 2
	private final String OUTPUT_FILE_200 = "C:/Users/Vijay/yelp-dataset/review_200.json";
	private final String TRAINING_DATA_FILE_200 = "C:/Users/Vijay/yelp-dataset/review_train_200.txt";

	// Set 3
	private final String OUTPUT_FILE_500 = "C:/Users/Vijay/yelp-dataset/review_500.json";
	private final String TRAINING_DATA_FILE_500 = "C:/Users/Vijay/yelp-dataset/review_train_500.txt";
	
	// TODO Test model with new data. Accuracy is 96% on training data!
	private final String OUTPUT_TEST_FILE_500 = "C:/Users/Vijay/yelp-dataset/review_test_500.json";
	private final String TEST_DATA_FILE = "C:/Users/Vijay/yelp-dataset/review_test.txt";
	
	// Path to summary file
	private final String SYSTEM_RESULTS_FILE = "C:/Users/Vijay/yelp-dataset/system_results.txt";
	
	private Dataset dataset;
	private NLPHelper nlpHelper;
	
	@Before
	public void setup()
	{
		dataset = new Dataset();
		nlpHelper = new NLPHelper(dataset);
	}
	
	//------------------------------------------------------------//
	// BASE LEVEL EVALUATION OF MODEL WITH TRAIN DATA = TEST DATA //
	//------------------------------------------------------------//
	
	public void testTrainingData200Records() throws IOException
	{
		// Initialize model and begin training
		DocumentCategorizer categorizer = nlpHelper.trainTextClassificationModel(TRAINING_DATA_FILE_200);
		
		// Read reviews.json into internal objects with unique ID
		Map<Integer, Review> reviews = dataset.readJSON(OUTPUT_FILE_200);

		// Evaluate model against test reviews data
		Map<Review, Integer> scores = nlpHelper.testTextClassificationModel(categorizer, reviews);
		
		displayScores(scores, false);
	}
	
	public void testTrainingData500Records() throws IOException
	{
		// Initialize model and begin training
		DocumentCategorizer categorizer = nlpHelper.trainTextClassificationModel(TRAINING_DATA_FILE_500);
		
		// Read reviews.json into internal objects with unique ID
		Map<Integer, Review> reviews = dataset.readJSON(OUTPUT_FILE_500);

		// Evaluate model against test reviews data
		Map<Review, Integer> scores = nlpHelper.testTextClassificationModel(categorizer, reviews);
		
		displayScores(scores, false);
	}

	//------------------------------------------------------------//
	// NEXT LEVEL EVALUATION OF MODEL WITH REAL TEST DATA         //
	//------------------------------------------------------------//
	@Test
	public void test500Records() throws IOException
	{
		// TODO Initial data read should scrub reviews with no text
		// TODO Initial data read should include only RESTAURANTS? clear this up for paper
		
		// Step 1 - Initialize model and begin training with reviews in training file
		// 			Using a MaximumEntropy classifer
		LOGGER.info("-----------------------INITIALIZE MODEL AND TRAIN-------------------------"+"\n");
		DocumentCategorizer categorizer = nlpHelper.trainTextClassificationModel(TRAINING_DATA_FILE_500);
		
		// Step 2 - Read reviews in test file into internal objects with unique ID
		Map<Integer, Review> reviewsMap = dataset.readJSON(OUTPUT_TEST_FILE_500);

		// Step 3 - Evaluate learned model against test reviews data
		LOGGER.info("-----------------------EVALUATE MODEL FOR SENTIMENT SCORE-------------------------"+"\n");
		Map<Review, Integer> scores = nlpHelper.testTextClassificationModel(categorizer, reviewsMap);
		// Verify and output
		 displayScores(scores, false);
		
		// Step 4 - Tag parts of speech and pull out all nouns to classify topic
		LOGGER.info("-----------------------RUN TOPIC CATEGORIZATION-------------------------"+"\n");
		// Step 4 - Run parts of speech tagger to determine the most frequently occurring nouns
		String POS_MODEL = "C:/Users/Vijay/photon-BU622/YelpNLP/models/en-pos-maxent.bin";
		List<Review> reviewNounsList = nlpHelper.tagNounsInReviewList(reviewsMap, POS_MODEL);
		
		// Step 4 - Determine the topic using the IMPORTANT part of the review text
		Map<String, Integer> wordFrequencyMap = nlpHelper.computeWordFrequencyMatrix(reviewNounsList);
//		didplayWordFrequencyMap(wordFrequencyMap);
		
		// Step 5 - Use word frequency map and review text to select topic
		Map<Review, String> reviewToTopicMap = nlpHelper.runTopicCategorization(reviewNounsList, wordFrequencyMap);
//		displayTopics(reviewToTopicMap, scores,  5);
		
		// Step 6 - Aggregate data (score, topic, and business_id of review) for conclusions
		List<ReviewResult> reviewResults = nlpHelper.summarizeResults(reviewToTopicMap, scores);
		displayReviewResults(reviewResults, 2);
	}
	
	private void displayReviewResults(List<ReviewResult> reviewResults, int limitOutputLineNumber) throws IOException 
	{
//		BufferedWriter writer = new BufferedWriter(new FileWriter(SYSTEM_RESULTS_FILE));
		int counter = 0;
		for (ReviewResult r : reviewResults)
		{
			if (r.getReview().text.isEmpty() || r.getReview().text.equals(" "))
			{
				// Skip
				continue;
			}
			if (counter > limitOutputLineNumber)
			{
				break;
			}
			System.out.println((r.toString()+"\n"));
			counter++;
		}
		
//		writer.close();
	}

	private void displayTopics(Map<Review, String> reviewToTopicMap, Map<Review, Integer> scores, int limitOutputLineNumber) 
	{
		LOGGER.info("Display topic information...\n");
		int count = 0;
		for (Review review : scores.keySet())
		{
			if (!reviewToTopicMap.containsKey(review))
			{
				continue;
			}
			if (count > limitOutputLineNumber)
			{
				break;
			}
			System.out.println(review.text);
			System.out.println("TOPIC=" + reviewToTopicMap.get(review));
			count++;
		}
		
	}

//	@Test
	public void offlineLoadTopics() throws IOException
	{
		// Step 2 - Read reviews in test file into internal objects with unique ID
		Map<Integer, Review> reviewsMap = dataset.readJSON(OUTPUT_TEST_FILE_500);

		// Step 3 - Tag parts of speech and pull out all nouns to classify topic
		LOGGER.info("-----------------------RUN TOPIC CATEGORIZATION-------------------------"+"\n");
		// Step 4 - Run parts of speech tagger to determine the most frequently occurring nouns
		String POS_MODEL = "C:/Users/Vijay/photon-BU622/YelpNLP/models/en-pos-maxent.bin";
		List<Review> reviewNounsList = nlpHelper.tagNounsInReviewList(reviewsMap, POS_MODEL);
		
		// Step 4 - Determine the topic using the IMPORTANT part of the review text
		Map<String, Integer> wordFrequencyMap = nlpHelper.computeWordFrequencyMatrix(reviewNounsList);
		didplayWordFrequencyMap(wordFrequencyMap);

	}
	
	private void didplayWordFrequencyMap(Map<String, Integer> wordFrequencyMap) 
	{
		LOGGER.info("Display word frequency map...\n");
		List<Integer> frequencies = new ArrayList<Integer>();
		for (String word : wordFrequencyMap.keySet())
		{
			int count = wordFrequencyMap.get(word);
			frequencies.add(count);
			LOGGER.info(word + " " + count);
		}
		
		Collections.sort(frequencies);
		
		for (Integer i : frequencies)
		{
			LOGGER.info("Count= " + i + ", ");
		}
	}

	/**
	 * Display parts of speech string array
	 * 
	 * @param reviewToEntityMap
	 * @param detailed
	 */
	private void displayPartsOfSpeech(Map<Review, List<String>> reviewsOnlyNounsInTextMap, boolean b) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Running parts of speech tagger...\n");
		for (Review review : reviewsOnlyNounsInTextMap.keySet())
		{
			sb.append("ID=" + review.id + "\n");
			for (int i = 0; i < reviewsOnlyNounsInTextMap.get(review).size(); i++)
			{
				sb.append(reviewsOnlyNounsInTextMap.get(review).get(i) + " ");
			}
			sb.append("\n");
		}
		
		LOGGER.info(sb.toString());
	}
		


	/**
	 * Display reviews and entity recognition results
	 * 
	 * @param reviewToEntityMap
	 * @param detailed
	 */
	private void displayEntities(Map<Review, List<List<Span>>> reviewToEntityMap, boolean detailed) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Running entity recognitation on names, locations, and organizations...\n");
		for (Review review : reviewToEntityMap.keySet())
		{
			double maxScore = -1;
			String topic = "";
			
			sb.append(review.text + "\n");
			
			for (List<Span> spans: reviewToEntityMap.get(review))
			{
				sb.append("-----------------------SPAN RESULTS-------------------------"+"\n");
				// Print span
				for (Span s : spans)
				{
					if (maxScore < s.getProb())
					{
						maxScore = s.getProb();
						
					}
					sb.append(nlpHelper.getTokens(review)[s.getStart()]);
					sb.append("\n");
				}
			}
		}
		
		LOGGER.info(sb.toString());
	}

	/**
	 * Display scores to standard out
	 * 
	 * @param scores map of review to score
	 * boolean detailed level of debug 
	 */
	private void displayScores(Map<Review, Integer> scores, boolean detailed) 
	{
		StringBuilder sb = new StringBuilder();
		
		int correct = 0;
		int incorrect = 0;
		for (Review r : scores.keySet())
		{
			if (r.stars == scores.get(r).intValue())
			{
				correct++;
			}
			else
			{
				incorrect++;
			}
			
			if (detailed == true)
			{
				sb.append(r.toString());
				sb.append(" Actual="+ r.stars+" Predicted="+scores.get(r).intValue());
				sb.append("\n");			
			}
		}
		
		float accuracy = (float) correct/(correct+incorrect);
		sb.append("Overall Prediction Accuracy = " + accuracy +
				", Correct(#) = " + correct + ", Incorrect(#) = " + incorrect);
		
		LOGGER.info(sb.toString());
	}
	
	/**
	 * 
	 * @throws IOException
	 */
//	@Test
	public void createTestDataFile() throws IOException
	{
		Dataset dataset = new Dataset();
		dataset.streamFile(REVIEW_DATA_FILE, OUTPUT_TEST_FILE_500, 600);
		Map<Integer, Review> reviews = dataset.readJSON(OUTPUT_TEST_FILE_500);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void createTrainingDataFile() throws IOException
	{
		Dataset dataset = new Dataset();
		dataset.streamFile(REVIEW_DATA_FILE, OUTPUT_FILE_200, 200);
		Map<Integer, Review> reviews = dataset.readJSON(OUTPUT_FILE_200);
		dataset.createTrainingDataFile(TRAINING_DATA_FILE_500, reviews);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void writeReviewsToTestFile() throws IOException 
	{
		Dataset dataset = new Dataset();
		dataset.streamFile(REVIEW_DATA_FILE, OUTPUT_FILE_200, 200);
		print(dataset.getReviewIdToReviewTextMap());
	}

	private void print(Map<Integer, Review> reviewIdToReviewTextMap) 
	{
		for (Integer id : reviewIdToReviewTextMap.keySet())
		{
			// PRINT ID
			System.out.println("ID " + id + "-");			
			System.out.println(reviewIdToReviewTextMap.get(id).toString());
		}
	}

}
