package main.app;
/**
 * 
 */

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import main.algorithm.NLPHelper;
import main.model.Dataset;
import main.model.Review;
import main.model.ReviewResult;
import opennlp.tools.doccat.DocumentCategorizer;

/**
 * YelpNLP application
 * 
 * @author vbala
 *
 */
public class YelpNLP {

	private final Logger LOGGER = Logger.getLogger(YelpNLP.class.getSimpleName());

	private Dataset dataset;
	private NLPHelper nlpHelper;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public YelpNLP() {
		// Initialize core objects
		dataset = new Dataset();
		nlpHelper = new NLPHelper();
	}

	/**
	 * Primary point of computation in this application. Given a validated dataset,
	 * this method will utilize the NLPHelper to classify customer reviews based on
	 * their sentiment towards a topic. The data will then be aggregated and
	 * averaged across all reviews for a particular restaurant to produce valuable
	 * feedback.
	 * 
	 * @throws IOException
	 */
	private void run() throws IOException {
		// Step 1 - Initialize model and begin training with reviews in training file
		// Using a NaiveBayes classifer
		LOGGER.info("-----------------------INITIALIZE MODEL AND TRAIN-------------------------" + "\n");
		DocumentCategorizer categorizer = nlpHelper.trainTextClassificationModel(YelpUtil.TRAINING_DATA_FILE_500);

		// Step 2 - Read reviews in test file into internal objects with unique ID
		Map<Integer, Review> reviewsMap = dataset.readJSON(YelpUtil.OUTPUT_TEST_FILE_500);

		// Step 3 - Evaluate learned model against test reviews data
		LOGGER.info("-----------------------EVALUATE MODEL FOR SENTIMENT SCORE-------------------------" + "\n");
		Map<Review, Integer> scores = nlpHelper.testTextClassificationModel(categorizer, reviewsMap);
		YelpUtil.displayScores(scores, false, YelpUtil.DISPLAY_LIMIT);

		// Step 4 - Tag parts of speech and pull out all nouns to classify topic
		LOGGER.info("-----------------------RUN TOPIC CATEGORIZATION-------------------------" + "\n");
		String POS_MODEL = "C:/Users/Vijay/photon-BU622/YelpNLP/models/en-pos-maxent.bin";
		List<Review> reviewNounsList = nlpHelper.tagNounsInReviewList(reviewsMap, POS_MODEL);

		// Step 5 - Determine the topic using the important part of the review text
		Map<String, Integer> wordFrequencyMap = nlpHelper.computeWordFrequencyMatrix(reviewNounsList);
		YelpUtil.displayWordFrequencyMap(wordFrequencyMap, YelpUtil.DISPLAY_LIMIT);

		// Step 6 - Use word frequency map and review text to select topic
		Map<Review, String> reviewToTopicMap = nlpHelper.runTopicCategorization(reviewNounsList, wordFrequencyMap);
		YelpUtil.displayTopics(reviewToTopicMap, scores, YelpUtil.DISPLAY_LIMIT);

//		// Step 6 - Aggregate data (score, topic, and business_id of review) for conclusions
		LOGGER.info("-----------------------SUMMARIZING RESULTS AND DISPLAYING SUBSET-------------------------" + "\n");
		List<ReviewResult> reviewResults = nlpHelper.summarizeResults(reviewToTopicMap, scores);
		YelpUtil.displayReviewResults(reviewResults, YelpUtil.DISPLAY_LIMIT);
	}

	/**
	 * Launch application.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		YelpNLP app = new YelpNLP();
		app.run();
	}

}
