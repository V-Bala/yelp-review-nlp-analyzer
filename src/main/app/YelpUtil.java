package main.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import main.model.Dataset;
import main.model.Review;
import main.model.ReviewResult;
import test.DatasetUTest;

/**
 * Utility class with paths to data files and display methods
 * 
 * @author vbala
 *
 */
public class YelpUtil {
	private final static Logger LOGGER = Logger.getLogger(DatasetUTest.class.getSimpleName());

	final static String REVIEW_DATA_FILE = "C:/Users/Vijay/yelp-dataset/review.json";
	final static String TRAINING_DATA_FILE_BASE = "C:/Users/Vijay/yelp-dataset/review_train.txt";

	// Set 1
	final static String OUTPUT_FILE_1 = "C:/Users/Vijay/yelp-dataset/review_1.json";
	final static String OUTPUT_FILE_50 = "C:/Users/Vijay/yelp-dataset/review_50.json";

	// Set 2
	final static String OUTPUT_FILE_200 = "C:/Users/Vijay/yelp-dataset/review_200.json";
	final static String TRAINING_DATA_FILE_200 = "C:/Users/Vijay/yelp-dataset/review_train_200.txt";

	// Set 3
	final static String OUTPUT_FILE_500 = "C:/Users/Vijay/yelp-dataset/review_500.json";
	final static String TRAINING_DATA_FILE_500 = "C:/Users/Vijay/yelp-dataset/review_train_500.txt";

	// Test model with new data
	final static String OUTPUT_TEST_FILE_500 = "C:/Users/Vijay/yelp-dataset/review_test_500.json";
	final static String TEST_DATA_FILE = "C:/Users/Vijay/yelp-dataset/review_test.txt";

	// Path to summary file
	final static String SYSTEM_RESULTS_FILE = "C:/Users/Vijay/yelp-dataset/system_results.txt";

	final static int DISPLAY_LIMIT = 20;

	public YelpUtil() {
	}

	public static void displayTopics(Map<Review, String> reviewToTopicMap, Map<Review, Integer> scores,
			int limitOutputLineNumber) {
		LOGGER.info("Display topic information...\n");
		int count = 0;
		for (Review review : scores.keySet()) {
			if (!reviewToTopicMap.containsKey(review)) {
				continue;
			}
			if (count > limitOutputLineNumber) {
				break;
			}
			System.out.println(review.text);
			System.out.println("TOPIC=" + reviewToTopicMap.get(review));
			count++;
		}

	}

	public static void displayWordFrequencyMap(Map<String, Integer> wordFrequencyMap, int DISPLAY_LIMIT) {
		LOGGER.info("Display word frequency map...\n");
		List<Integer> frequencies = new ArrayList<Integer>();
		for (String word : wordFrequencyMap.keySet()) {
			int count = wordFrequencyMap.get(word);
			frequencies.add(count);
			LOGGER.info(word + " " + count);
		}

		Collections.sort(frequencies);

		int wordLimit = frequencies.size() - 7;
		int index = frequencies.size() - 1;
		List<String> keys = new ArrayList<String>();
		while (index > wordLimit) {
			for (String key : wordFrequencyMap.keySet()) {
				int value = wordFrequencyMap.get(key);
				if (value == frequencies.get(index)) {
					// Found max word
					keys.add(key);
					// Increment index
					index--;
				}
			}
		}

		int counter = 0;
		for (String key : keys) {
			if (counter > DISPLAY_LIMIT) {
				break;
			}
			System.out.print(key + ", ");
			counter++;
		}
		System.out.println();
		for (String key : keys) {
			if (counter > DISPLAY_LIMIT) {
				break;
			}
			System.out.print(wordFrequencyMap.get(key) + ", ");
		}
	}

	/**
	 * Display parts of speech string array
	 * 
	 * @param reviewToEntityMap
	 * @param detailed
	 */
	public static void displayPartsOfSpeech(Map<Review, List<String>> reviewsOnlyNounsInTextMap, boolean b) {
		StringBuilder sb = new StringBuilder();
		sb.append("Running parts of speech tagger...\n");
		for (Review review : reviewsOnlyNounsInTextMap.keySet()) {
			sb.append("ID=" + review.id + "\n");
			for (int i = 0; i < reviewsOnlyNounsInTextMap.get(review).size(); i++) {
				sb.append(reviewsOnlyNounsInTextMap.get(review).get(i) + " ");
			}
			sb.append("\n");
		}

		LOGGER.info(sb.toString());
	}

	/**
	 * Display scores to standard out
	 * 
	 * @param scores map of review to score boolean detailed level of debug
	 */
	public static void displayScores(Map<Review, Integer> scores, boolean detailed, int DISPLAY_LIMIT) {
		StringBuilder sb = new StringBuilder();

		int correct = 0;
		int incorrect = 0;
		int counter = 0;
		for (Review r : scores.keySet()) {
			if (counter > DISPLAY_LIMIT) {
				break;
			}

			if (r.stars == scores.get(r).intValue()) {
				correct++;
			} else {
				incorrect++;
			}

			if (detailed == true) {
				sb.append(r.toString() + "\n");
				sb.append(" Actual=" + r.stars + " Predicted=" + scores.get(r).intValue());
				sb.append("\n");
			}
		}

		float accuracy = (float) correct / (correct + incorrect);
		sb.append("Overall Prediction Accuracy = " + accuracy + ", Correct(#) = " + correct + ", Incorrect(#) = "
				+ incorrect);

		System.out.println(sb.toString());
	}

	public static void displayReviewResults(List<ReviewResult> reviewResults, int DISPLAY_LIMIT) {
		int count = 0;
		for (ReviewResult result : reviewResults) {
			if (count > DISPLAY_LIMIT) {
				break;
			}
			// Display the review and the text
			System.out.println("Input " + result.getReview().toString() + "\n  Output " + result.getScore() + " - "
					+ result.getTopic());
			count++;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void createTestDataFile() throws IOException {
		Dataset dataset = new Dataset();
		dataset.streamFile(REVIEW_DATA_FILE, OUTPUT_TEST_FILE_500, 600);
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void createTrainingDataFile() throws IOException {
		Dataset dataset = new Dataset();
		dataset.streamFile(REVIEW_DATA_FILE, OUTPUT_FILE_200, 200);
		Map<Integer, Review> reviews = dataset.readJSON(OUTPUT_FILE_200);
		dataset.createTrainingDataFile(TRAINING_DATA_FILE_500, reviews);
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void writeReviewsToTestFile() throws IOException {
		Dataset dataset = new Dataset();
		dataset.streamFile(REVIEW_DATA_FILE, OUTPUT_FILE_200, 200);
		print(dataset.getReviewIdToReviewMap());
	}

	private void print(Map<Integer, Review> reviewIdToReviewTextMap) {
		for (Integer id : reviewIdToReviewTextMap.keySet()) {
			// PRINT ID
			System.out.println("ID " + id + "-");
			System.out.println(reviewIdToReviewTextMap.get(id).toString());
		}
	}

}
