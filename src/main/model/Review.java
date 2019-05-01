package main.model;

public class Review {

	public final String id;
	public String text;
	public final int stars;
	public final String businessId;
	
	public Review(String id, String text, int stars, String businessId)
	{
		this.id = id;
		this.text = text;
		this.stars = stars;
		this.businessId = businessId;
	}
	
	/**
	 * Get review by ID
	 */
	public Review getReviewById(String id)
	{
		if (!this.id.equals(id))
		{
			return null;
		}
		
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Review [id=").append(id).append(", text=").append(text).append(", stars=").append(stars)
				.append(", businessId=").append(businessId).append("]");
		return builder.toString();
	}
	
	
}
