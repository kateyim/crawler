package mo.umac.crawler;

import java.util.ArrayList;
import java.util.List;

import utils.DefaultValues;

import mo.umac.spatial.Circle;

public class ResultSet {
	protected List<APOI> pois;

	private int totalResultsAvailable = DefaultValues.INIT_INT;

	private int totalResultsReturned = DefaultValues.INIT_INT;

	private int firstResultPosition = DefaultValues.INIT_INT;

	/**
	 * one circle represents the scope of one query.
	 */
	private List<Circle> circles = new ArrayList<Circle>();

	public void addACircle(Circle aCircle) {
		circles.add(aCircle);
	}

	public List<Circle> getCircles() {
		return circles;
	}

	public List<APOI> getPOIs() {
		return pois;
	}

	public void setPOIs(List<APOI> pois) {
		this.pois = pois;
	}

	public int getTotalResultsAvailable() {
		return totalResultsAvailable;
	}

	public int getTotalResultsReturned() {
		return totalResultsReturned;
	}

	public int getFirstResultPosition() {
		return firstResultPosition;
	}

	public void setTotalResultsAvailable(int totalResultsAvailable) {
		this.totalResultsAvailable = totalResultsAvailable;
	}

	public void setTotalResultsReturned(int totalResultsReturned) {
		this.totalResultsReturned = totalResultsReturned;
	}

	public void setFirstResultPosition(int firstResultPosition) {
		this.firstResultPosition = firstResultPosition;
	}

}
