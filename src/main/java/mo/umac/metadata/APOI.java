/**
 * 
 */
package mo.umac.metadata;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author kate
 */
public class APOI {
	private int id;
	private String title;
	private String city;
	private String state;
	private Coordinate coordinate = new Coordinate();
	private Rating rating = new Rating();

	// add at 2013-9-26
	private int numCrawled;

	/**
	 * The distance parsed by the returned web pages. The unit is mile.
	 */
	private double distanceInMiles;

	private List<Category> categories;

	public APOI() {

	}

	public APOI(int id, String title, String city, String state, Coordinate coordinate, Rating rating, double distanceInMiles, List<Category> categories,
			int numCrawled) {
		super();
		this.id = id;
		this.title = title;
		this.city = city;
		this.state = state;
		this.coordinate = coordinate;
		// this.rating = rating;
		this.distanceInMiles = distanceInMiles;
		this.categories = categories;
		this.numCrawled = numCrawled;
	}

	public APOI(int id, String title, String city, String state, Double longitude, Double latitude, Rating rating, double distance, List<Category> categories,
			int numCrawled) {
		super();
		this.id = id;
		this.title = title;
		this.city = city;
		this.state = state;
		this.coordinate.x = longitude;
		this.coordinate.y = latitude;
		// this.rating = rating;
		this.distanceInMiles = distance;
		this.categories = categories;
		this.numCrawled = numCrawled;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Result [id =" + id + ", title =" + title + ", city =" + city + ", state =" + state + ", longitude =" + coordinate.x + ", latitude ="
				+ coordinate.y + ", distance =" + distanceInMiles + ", numCrawled = " + numCrawled + "]");
		return sb.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public double getLongitude() {
		return coordinate.x;
	}

	public void setLongitude(double longitude) {
		this.coordinate.x = longitude;
	}

	public double getLatitude() {
		return coordinate.y;
	}

	public void setLatitude(double latitude) {
		this.coordinate.y = latitude;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public double getDistance() {
		return distanceInMiles;
	}

	public void setDistance(double distance) {
		this.distanceInMiles = distance;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public int getNumCrawled() {
		return numCrawled;
	}

	public void setNumCrawled(int numCrawled) {
		this.numCrawled = numCrawled;
	}

}
