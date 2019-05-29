package main.model;

public class ReviewResult {

	private Review review;
	private int score;
	private String topic;

	public ReviewResult(Review review, int score, String topic) {
		this.review = review;
		this.score = score;
		this.topic = topic;
	}

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return the review
	 */
	public Review getReview() {
		return review;
	}

	/**
	 * @param review the review to set
	 */
	public void setReview(Review review) {
		this.review = review;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReviewResult [review=").append(review.toString()).append(", SCORE=").append(score)
				.append(", TOPIC=").append(topic).append("]");
		return builder.toString();
	}

}
