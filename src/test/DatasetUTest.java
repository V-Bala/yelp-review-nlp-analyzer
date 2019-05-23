package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import main.algorithm.NLPHelper;
import main.app.YelpUtil;
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
	
	private final String OUTPUT_TEST_FILE_500 = "C:/Users/Vijay/yelp-dataset/review_test_500.json";
	private final String TEST_DATA_FILE = "C:/Users/Vijay/yelp-dataset/review_test.txt";
	
	// Path to summary file
	private final String SYSTEM_RESULTS_FILE = "C:/Users/Vijay/yelp-dataset/system_results.txt";
	
	private final int DISPLAY_LIMIT = 20;
	
	private Dataset dataset;
	private NLPHelper nlpHelper;
	
	@Before
	public void setup()
	{
		dataset = new Dataset();
		nlpHelper = new NLPHelper();
	}
	
	public void testTrainingData200Records() throws IOException
	{
		// Initialize model and begin training
		DocumentCategorizer categorizer = nlpHelper.trainTextClassificationModel(TRAINING_DATA_FILE_200);
		
		// Read reviews.json into internal objects with unique ID
		Map<Integer, Review> reviews = dataset.readJSON(OUTPUT_FILE_200);

		// Evaluate model against test reviews data
		Map<Review, Integer> scores = nlpHelper.testTextClassificationModel(categorizer, reviews);
		
		YelpUtil.displayScores(scores, false, DISPLAY_LIMIT);
	}
	
	public void testTrainingData500Records() throws IOException
	{
		// Initialize model and begin training
		DocumentCategorizer categorizer = nlpHelper.trainTextClassificationModel(TRAINING_DATA_FILE_500);
		
		// Read reviews.json into internal objects with unique ID
		Map<Integer, Review> reviews = dataset.readJSON(OUTPUT_FILE_500);

		// Evaluate model against test reviews data
		Map<Review, Integer> scores = nlpHelper.testTextClassificationModel(categorizer, reviews);
		
		YelpUtil.displayScores(scores, false, DISPLAY_LIMIT);
	}

	@Test
	public void test500Records() throws IOException
	{
		// Step 1 - Initialize model and begin training with reviews in training file
		// 			Using a NaiveBayes classifer
		LOGGER.info("-----------------------INITIALIZE MODEL AND TRAIN-------------------------"+"\n");
		DocumentCategorizer categorizer = nlpHelper.trainTextClassificationModel(TRAINING_DATA_FILE_500);
		
		// Step 2 - Read reviews in test file into internal objects with unique ID
		Map<Integer, Review> reviewsMap = dataset.readJSON(OUTPUT_TEST_FILE_500);

		// Step 3 - Evaluate learned model against test reviews data
		LOGGER.info("-----------------------EVALUATE MODEL FOR SENTIMENT SCORE-------------------------"+"\n");
		Map<Review, Integer> scores = nlpHelper.testTextClassificationModel(categorizer, reviewsMap);
		YelpUtil.displayScores(scores, true, DISPLAY_LIMIT);
		
		// Step 4 - Tag parts of speech and pull out all nouns to classify topic
		LOGGER.info("-----------------------RUN TOPIC CATEGORIZATION-------------------------"+"\n");
		String POS_MODEL = "C:/Users/Vijay/photon-BU622/YelpNLP/models/en-pos-maxent.bin";
		List<Review> reviewNounsList = nlpHelper.tagNounsInReviewList(reviewsMap, POS_MODEL);
		
		// Step 5 - Determine the topic using the important part of the review text
		Map<String, Integer> wordFrequencyMap = nlpHelper.computeWordFrequencyMatrix(reviewNounsList);
		YelpUtil.displayWordFrequencyMap(wordFrequencyMap, DISPLAY_LIMIT);
		
		// Step 6 - Use word frequency map and review text to select topic
		Map<Review, String> reviewToTopicMap = nlpHelper.runTopicCategorization(reviewNounsList, wordFrequencyMap);
		YelpUtil.displayTopics(reviewToTopicMap, scores,  DISPLAY_LIMIT);
		
		// Step 6 - Aggregate data (score, topic, and business_id of review) for conclusions
		List<ReviewResult> reviewResults = nlpHelper.summarizeResults(reviewToTopicMap, scores);
		YelpUtil.displayReviewResults(reviewResults, DISPLAY_LIMIT);
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
		YelpUtil.displayWordFrequencyMap(wordFrequencyMap, DISPLAY_LIMIT);

	}
}
