# Analysis of Restaurant Reviews using NLP

## Overview
Java and Maven App that analyzes restaurant reviews on Yelp to determine the main topics for positive and negative customer feedback. The application consumes the restaurant review data and runs it through a textual analytics pipeline which internally uses Natural Language Processing and a Naïve Bayes Classifier.  

The system generates two main pieces of information for each review:  

•	Sentiment – An integer between 0 and 5. The higher the number the more positive the emotion in the review is.  
•	Topic – A string which represents the subject of the review.  

## Dataset
The dataset used in the development of this application is provided by Kaggle and it contains customer review data for hundreds of businesses across a wide array of metropolitan areas.

## Example Analysis
Here are some inferences that can be made based on the system results:  
•	“Emerald Chinese Restaurant received extremely positive feedback related to their food.”  
•	“Sushi Sushi received positive feedback related to their food.”  
•	“Paramount Fine Foods received extremely positive feedback related to their service.”  
•	“Impressionz received extremely negative feedback related to their service.”  

## Build and Run
```
// Maven build
mvn clean install
// Run app
java -r target/YelpNLP-0.0.1-SNAPSHOT.jar
