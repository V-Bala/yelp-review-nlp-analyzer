package main.algorithm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import main.model.Dataset;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

/**
 * 
 * @author vijay.bala
 *
 */
public class NLPMediator {

	private final Dataset dataset;
	private final SentimentNLP sentimentNLP;
	private final TopicNLP topicNLP;
	
	public NLPMediator(Dataset dataset) {
		this.dataset = dataset;
		this.sentimentNLP = new SentimentNLP();
		this.topicNLP = new TopicNLP();
	}
	
	/**
	 * Primary point of computation in this application. Given a 
	 * validated dataset, this method will utilize the SentimentNLP and 
	 * TopicNLP helper classes to classify customer reviews based
	 * on their sentiment towards a topic. The data will then be 
	 * aggregated and averaged across all reviews for a particular
	 * restaurant to produce valuable feedback.
	 * 
	 * @return restaurantToNLPScore map of restaurant to customer feedback
	 * @throws IOException
	 */
	public Map<Object, Object> solve() throws IOException {
		
		// 1) Read in raw review text from dataset
		Map<Long, Object> reviewIdToReviewTextMap = 
				dataset.getReviewIdToReviewTextMap();
		
		// 2) Tag parts of speech (nouns and adjectives to start)
		
		// 3) Create frequency distribution to identify 
		//    most frequently occurring nouns (will initially
		//    represent our topic set).
		
		// 3) Named entity recognition
		// 3A) Categorize review by highest association with 'topic'
		//     also known as highest frequency nouns which appear in 
		//     text
		computeTopicWeights(reviewIdToReviewTextMap);
		
		// 4) Deep syntactic parsing
		// 4A) Conduct sentiment analysis using OpenNLP (DoccatModel)
		//     and Naive Bayes Classifier algorithm to identify
		//     the magnitude of the review towards the associated topic
		//     (positive, negative, or neutral)
		classifyText(reviewIdToReviewTextMap);
		
		// 5) Annotated structured text
		// 5A) Tag each review with its' score (sentiment strength
		//     and topic)
		
		// 6) Aggregate scores and average results across each individual restaurant
		Map<Object, Object> restaurantToNLPScore = matchReviewsToRestaurant();
		return restaurantToNLPScore;
	}

	/**
	 * Classify the sentiment of the review text by using the OpenNLP 
	 * tool suite. We must first construct a model using the raw review text 
	 * 
	 * Currently this method is simplified to show initial implementation.
	 * The review magnitude will ultimately be stored for later analysis but 
	 * this example just logs the result to the console.
	 * 
	 * @throws IOException 
	 */
	private void classifyText(Map<Long, Object> reviewIdToReviewTextMap) 
			throws IOException {
		
		for (Long reviewId : reviewIdToReviewTextMap.keySet())
		{
			// Extract review text
			Object review = reviewIdToReviewTextMap.get(reviewId);
			String reviewText = review.toString();
			
			// Implement OpenNLP sentence analyzers
			InputStream inputStream = new FileInputStream(reviewText);
			DoccatModel doccatModel = new DoccatModel(inputStream);
			DocumentCategorizerME categorizer = new DocumentCategorizerME(doccatModel);

			// Retrieve the categorization results
			double[] outcomes = categorizer.categorize(new String[]{reviewText});
			String category = categorizer.getBestCategory(outcomes);

			// Log to console
			if (category.equalsIgnoreCase("1")){
				System.out.print("The text is positive");
			} else if (category.contentEquals("0")){
				System.out.print("The text is negative");
			}
			else if (category.equalsIgnoreCase("-1")) {
				System.out.print("The text is neural");
			}
		}
	}

	/**
	 * Average results across each individual restaurant and
	 * store sentiment score and topic match. Aggregate results
	 * to determine: "What  people like or dislike about the 
	 * restaurant?"
	 * 
	 * @return restaurantToNLPScore map containing the restaurant and
	 * a summary of the sentiment analysis 
	 */
	private Map<Object, Object> matchReviewsToRestaurant() { 		
		// TODO Key will be the restaurant, value is the score which contains details
		// from the customer feedback analysis for the restaurant
		Map<Object, Object> restaurantToNLPScore = new HashMap<Object, Object>();
		return restaurantToNLPScore;
	}
	
	
	private void computeTopicWeights(Map<Long, Object> reviewIdToReviewTextMap) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the dataset
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * @return the sentimentNLP
	 */
	public SentimentNLP getSentimentNLP() {
		return sentimentNLP;
	}

	/**
	 * @return the topicNLP
	 */
	public TopicNLP getTopicNLP() {
		return topicNLP;
	}
	
	
}
