package main.algorithm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import main.model.Dataset;
import main.model.Review;
import main.model.ReviewResult;
import main.model.Topic;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

/**
 * 
 * @author vijay.bala
 *
 */
public class NLPHelper {
	
	private final Logger LOGGER = Logger.getLogger(NLPHelper.class.getSimpleName());

	private final Dataset dataset;
	
	public NLPHelper(Dataset dataset) {
		this.dataset = dataset;
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
	public Map<Review, Integer> testTextClassificationModel(
			DocumentCategorizer categorizer,
			Map<Integer, Review> reviews) 
			throws IOException 
	{	
		Map<Review, Integer> reviewToCategoryMap = new HashMap<Review, Integer>();
		
		// Retrieve the categorization results
		for (Integer id : reviews.keySet())
		{
			String text = reviews.get(id).text;
			double[] outcomes = categorizer.categorize(text.split(" "));
			String category = categorizer.getBestCategory(outcomes);
			reviewToCategoryMap.put(reviews.get(id), Integer.parseInt(category));
		}
		
		return reviewToCategoryMap;
	}
	
	/**
	 * Read, and process training file. Then load the {@link DoccatModel}
	 * and train against all records in pathToTrainingFile.
	 * 
	 * Returns a DocumentCategorizer object which stores the learned model.
	 * 
	 * @param pathToTrainingFile path to the file containing training records
	 * @return categorizer stores the trained model object
	 * @throws IOException 
	 */
	public DocumentCategorizer trainTextClassificationModel(String pathToTrainingFile) 
			throws IOException 
	{
		InputStreamFactory dataIn = new MarkableFileInputStreamFactory(new File(pathToTrainingFile));
		ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
		ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
		TrainingParameters params = new TrainingParameters();
		params.put(TrainingParameters.ITERATIONS_PARAM, 20+"");
		params.put(TrainingParameters.CUTOFF_PARAM, 0+"");
		DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory());
		BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(new File("C:/Users/Vijay/yelp-dataset/review-train_500.bin")));
		model.serialize(modelOut);
		DocumentCategorizer categorizer = new DocumentCategorizerME(model);
		return categorizer;
	}	

	/**
	 * Tag parts of speech in a review using parts of speech tagger and chunking
	 * 
	 * @param scores
	 * @param entityModelPath
	 * @return
	 * @throws IOException
	 */
	public Map<Review, List<String>> tagPartsOfSpeech(
			Map<Review, Integer> reviewToScoreMap, 
			String entityModelPath) throws IOException 
	{
		// Initialize return object which stores the type of model to the list of span objects
		// containing references to named entities
		Map<Review, List<String>> reviewToPartsOfSpeechMap = new HashMap<Review, List<String>>();

		// Load entity models
		File file = new File(entityModelPath);
		InputStream model = new FileInputStream(file);
	    POSModel posModel = new POSModel(model);
	    POSTaggerME tagger = new POSTaggerME(posModel);

	    for (Review review : reviewToScoreMap.keySet())
	    {
	    	String[] tokens = getTokens(review);
	    	String[] tagged = tagger.tag(tokens);
	    	List<String> reviewTextNounsList = new ArrayList<String>();

	    	for (int i = 0; i < tagged.length; i++)
	    	{
	    		if (tagged[i].equalsIgnoreCase("nn"))
	    		{
	    			// ONLY SAVE NOUNS
		    		reviewTextNounsList.add(tokens[i]);
	    		}
	    	}

	    	reviewToPartsOfSpeechMap.put(review, reviewTextNounsList);
	    }

	    return reviewToPartsOfSpeechMap;
	}
	
	/**
	 * Determine the entities associated with a review using tokenization
	 * and named entity recognition.
	 * 
	 * NAMED ENTITY RECOGNITION - Find entities including people, locations,
	 * and organizations
	 * 
	 * @param reviewsToScoreMap map of reviews to the sentiment score
	 * @throws IOException 
	 */
	public Map<Review, List<List<Span>>> runEntityRecognition(
			Map<Review, List<String>> reviewToTextStringArrayMap,
			List<String> entityModelsToLoad
			) throws IOException 
	{
		// Initialize return object which stores the type of model to the list of span objects
		// containing references to named entities
		Map<Review, List<List<Span>>> reviewToEntityMap = new HashMap<Review, List<List<Span>>>();
		
		int count = 0;
		for (Review review : reviewToTextStringArrayMap.keySet())
		{
			if (count > 10)
			{
				break;
			}
			System.out.println("Review " + count);
			// Load entity models
			for (String pathToModelFile : entityModelsToLoad)
			{
				File file = new File(pathToModelFile);
				TokenNameFinderModel model = new TokenNameFinderModel(file);
				NameFinderME nameFinderME = new NameFinderME(model);

				// For each sentence in the review text find if it refers to a
				// location, or organization
				String [] tokens = getTokens(review);
				List<Span> spans = Arrays.asList(nameFinderME.find(tokens));
				if (spans.isEmpty())
				{
					continue;
				}

				// Store in map for analysis
				if (!reviewToEntityMap.containsKey(review) || reviewToEntityMap.get(review).isEmpty())
				{
					List<List<Span>> entitySpansList = new ArrayList<List<Span>>();
					entitySpansList.add(spans);
					reviewToEntityMap.put(review, entitySpansList);
				}
				else
				{
					List<List<Span>> entitySpansList = reviewToEntityMap.get(review);
					entitySpansList.add(spans);
					reviewToEntityMap.put(review, entitySpansList);
				}
			}
			
			count++;
		}
		
		return reviewToEntityMap;
	}

	/**
	 * 
	 * @param reviewToEntityMap
	 * @return
	 */
	public Map<String, Integer> computeWordFrequencyMatrix(Map<Review, List<List<Span>>> reviewToEntityMap) 
	{		
		Map<String, Integer> wordToFrequencyCount = new HashMap<String, Integer>();
		
		for (Review review : reviewToEntityMap.keySet())
		{
			double minScore = 0.8;
			String topic = "";
					
			List<List<Span>> spansList = reviewToEntityMap.get(review);
			for (int i = 0; i < spansList.size(); i++)
			{
				// Print span
				for (Span s : spansList.get(i))
				{
					if (s.getProb() >= minScore)
					{
						String finalText = getTokens(review)[s.getStart()];
						String[] chunks = finalText.split(" ");
						storeFrequencyOfWord(wordToFrequencyCount, chunks);
					}
				}
			}
		}
		
		return wordToFrequencyCount;
	}

	private void storeFrequencyOfWord(Map<String, Integer> wordToFrequencyCount, String[] chunks) 
	{
		for (int i = 0; i < chunks.length; i++)
		{
			if (wordToFrequencyCount.containsKey(chunks[i]))
			{
				int count = wordToFrequencyCount.get(chunks[i]);
				count++;
				wordToFrequencyCount.put(chunks[i], count);
			}
			else
			{
				wordToFrequencyCount.put(chunks[i], 0);
			}
		}
		
	}

	/**
	 * TOKENIZATION - Break down sentences from review text into parts
	 * 
	 * @param review
	 * @param reviewToTextStringArrayMap 
	 * @return
	 */
	public String[] getTokensFromMap(Review review, Map<Review, List<String>> reviewToTextStringArrayMap) 
	{
		List<String> listOfStrings = reviewToTextStringArrayMap.get(review);
		String joined = String.join(" ", listOfStrings);
		
		// Tokenize review text
		SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
		String[] tokens = tokenizer.tokenize(joined);
		return tokens;
	}

	/**
	 * TOKENIZATION - Break down sentences from review text into parts
	 * 
	 * @param review
	 * @return
	 */
	public String[] getTokens(Review review) 
	{
		// Tokenize review text
		SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
		String[] tokens = tokenizer.tokenize(review.text);
		return tokens;
	}

	/**
	 * 
	 * @param reviewIdToReviewTextMap
	 * @throws FileNotFoundException 
	 */
	public List<String[]> sentenceDetection(Map<Integer, Review> reviewIdToReviewTextMap) throws FileNotFoundException 
	{
		InputStream modelIn = new FileInputStream("C:/Users/Vijay/photon-BU622/YelpNLP/models/en-sent.bin");
		SentenceModel model = null;
		try {
		  model = new SentenceModel(modelIn);
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
		
		List<String[]> resultSentences = new ArrayList<String[]>();
		
		for (Integer id : reviewIdToReviewTextMap.keySet())
		{
			String sentences[] = sentenceDetector.sentDetect(reviewIdToReviewTextMap.get(id).text);
			resultSentences.add(sentences);
		}
		
		return resultSentences;

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
	public Map<Review, Integer> classifyText(String pathToTrainingFile, Map<Integer, Review> reviews) throws IOException 
	{
		Map<Review, Integer> reviewToCategoryMap = new HashMap<Review, Integer>();
		InputStreamFactory dataIn = new MarkableFileInputStreamFactory(new File(pathToTrainingFile));
		ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
		ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
		TrainingParameters params = new TrainingParameters();
		params.put(TrainingParameters.ITERATIONS_PARAM, 20+"");
		params.put(TrainingParameters.CUTOFF_PARAM, 0+"");
		DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory());
		BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(new File("C:/Users/Vijay/yelp-dataset/review-train_500.bin")));
		model.serialize(modelOut);
		DocumentCategorizer categorizer = new DocumentCategorizerME(model);
		
		// Retrieve the categorization results
		for (Integer id : reviews.keySet())
		{
			String text = reviews.get(id).text;
			double[] outcomes = categorizer.categorize(text.split(" "));
			String category = categorizer.getBestCategory(outcomes);
//			System.out.println(category);
			// Log to console
			if (category.equalsIgnoreCase("5")){
//				System.out.print("The text is positive!");
			} else if (category.contentEquals("1")){
//				System.out.print("The text is negative!");
			}
			else 
			{
//				System.out.print("The text is neutral");
			}
			
//			System.out.println();
			reviewToCategoryMap.put(reviews.get(id), Integer.parseInt(category));
		}
		
		return reviewToCategoryMap;
	}
	
	/**
	 * @return the dataset
	 */
	public Dataset getDataset() 
	{
		return dataset;
	}

	/**
	 * Tag nouns in the review text and return a list of Reviews with
	 * text set to the defined features (i.e. nouns)
	 * 
	 * @param reviewsMap map of reviews
	 * @param pathToPOSModelFile path to opennlp pos model file
	 * @return nounsReviewsList list of reviews with features as text
	 * @throws IOException
	 */
	public List<Review> tagNounsInReviewList(Map<Integer, Review> reviewsMap, String pathToPOSModelFile) throws IOException 
	{
		// Return object with review text cleaned
		List<Review> nounsReviewsList = new ArrayList<Review>();
		
		// Load entity model for parts-of-speech
		File file = new File(pathToPOSModelFile);
		InputStream model = new FileInputStream(file);
	    POSModel posModel = new POSModel(model);
	    POSTaggerME tagger = new POSTaggerME(posModel);
	    StringBuilder sb = new StringBuilder();
	    
	    for (Integer id : reviewsMap.keySet())
	    {
	    	Review review = reviewsMap.get(id);
	    	String[] tokens = getTokens(review);
	    	String[] tagged = tagger.tag(tokens);

	    	for (int i = 0; i < tagged.length; i++)
	    	{
	    		if (tagged[i].equalsIgnoreCase("nn"))
	    		{
	    			// Only retain nouns in the review text
	    			sb.append(tokens[i] + " ");
	    		}
	    	}
	    	
	    	// Review text becomes features for model
			review.text = sb.toString();
			
			// Clear StringBuilder for the next review
			sb.setLength(0);
	    	nounsReviewsList.add(review);
	    	
	    	if (nounsReviewsList.isEmpty())
	    	{
	    		LOGGER.info("Error no nouns found for any reviews in system!");
	    	}
	    }

	    return nounsReviewsList;

	}

	public Map<String, Integer> computeWordFrequencyMatrix(List<Review> reviewNounsList) 
	{
		Map<String, Integer> wordToFrequencyCount = new HashMap<String, Integer>();
		
		for (Review review : reviewNounsList)
		{
			String text = review.text;
			String [] tokens = text.split(" ");
			for (int i = 0; i < tokens.length; i++)
			{
				String word = tokens[i];
				if (wordToFrequencyCount.containsKey(word))
				{
					// Increment the count
					int count = wordToFrequencyCount.get(word);
					count++;
					wordToFrequencyCount.put(word, count);
				}
				else
				{
					wordToFrequencyCount.put(word, 0);
				}
			}			
		}
		
		return wordToFrequencyCount;
	}

	public Map<Review, String> runTopicCategorization(List<Review> reviewNounsList,
			Map<String, Integer> wordFrequencyMap) 
	{
		Map<Review, String> reviewToTopicMap = new HashMap<Review, String>();

		for (Review review : reviewNounsList)
		{
			String topic = null;
			String text = review.text;
			String[] words = text.split(" ");
			int maxCount = -1;
			for (String word : words)
			{
				if (Topic.TOPICS.contains(word))
				{
					// If this word is a HOT TOPIC then use it 
					topic = word;
					break;
				}
				// TODO instead of just grabbing one, grab the top 3 words for multiple topics
				if (wordFrequencyMap.containsKey(word))
				{
					int count = wordFrequencyMap.get(word);
					if (count > maxCount)
					{
						maxCount = count;
						topic = word;
					}
				}
			}

			if (topic == null)
			{
				// If no word matched, then just choose the first word in the review as the topic
				topic = words[0]; 
			}
			reviewToTopicMap.put(review, topic);
		}
		
		return reviewToTopicMap;
	}

	public List<ReviewResult> summarizeResults(Map<Review, String> reviewToTopicMap, Map<Review, Integer> scores) 
	{
		Map<String, List<Review>> businessToReviewListMap = new HashMap<String, List<Review>>();
		
		List<ReviewResult> reviewResults = new ArrayList<ReviewResult>();
		for (Review review : scores.keySet())
		{
			// Get the score
			// Get the topic
			if (!businessToReviewListMap.containsKey(review.businessId))
			{
				// Add new review
				List<Review> reviewList = new ArrayList<Review>();
				reviewList.add(review);
				businessToReviewListMap.put(review.businessId, reviewList);
			}
			else
			{
				// Map business ID to list of reviews
				businessToReviewListMap.get(review.businessId).add(review);
				
			}
		}
		
		// Get the business ID and the reviews for that business
		for (String businessId : businessToReviewListMap.keySet())
		{
			List<Review> reviewsForBusiness = businessToReviewListMap.get(businessId);
			for (Review review : reviewsForBusiness)
			{
				// Score and topic do not exist
				if (!scores.containsKey(review))
				{
					break;
				}
				
				 // Otherwise get the score and the topic
				ReviewResult result = new ReviewResult(review, scores.get(review), reviewToTopicMap.get(review));
				reviewResults.add(result);
			}
		}
		
		return reviewResults;
	}
	
}
