package mo.umac.metadata;

public class Rating {
	/**
	 * Average rating may be NaN, cannot be converted to Integer directly
	 */
	private double AverageRating;
	private int TotalRatings;
	private int TotalReviews;
	private String LastReviewDate;
	private String LastReviewIntro;

	public static final String NO_AVERAGE_RATING_FLAG = "NaN";
	public static double noAverageRatingValue = -1;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Rating [AverageRating=" + AverageRating + ", TotalRatings=" + TotalRatings + ", TotalReviews=" + TotalReviews + ", LastReviewDate="
				+ LastReviewDate + ", LastReviewIntro=" + LastReviewIntro + "]");
		return sb.toString();
	}

	public double getAverageRating() {
		return AverageRating;
	}

	public void setAverageRating(double averageRating) {
		AverageRating = averageRating;
	}

	public int getTotalRatings() {
		return TotalRatings;
	}

	public void setTotalRatings(int totalRatings) {
		TotalRatings = totalRatings;
	}

	public int getTotalReviews() {
		return TotalReviews;
	}

	public void setTotalReviews(int totalReviews) {
		TotalReviews = totalReviews;
	}

	public String getLastReviewDate() {
		return LastReviewDate;
	}

	public void setLastReviewDate(String lastReviewDate) {
		LastReviewDate = lastReviewDate;
	}

	public String getLastReviewIntro() {
		return LastReviewIntro;
	}

	public void setLastReviewIntro(String lastReviewIntro) {
		LastReviewIntro = lastReviewIntro;
	}

}
