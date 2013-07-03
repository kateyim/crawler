/**
 * 
 */
package mo.umac.crawler;

import com.vividsolutions.jts.geom.Envelope;

import mo.umac.parser.ResultSet;

/**
 * The one dimensional crawler.
 * 
 * @author Kate
 * 
 */
public class OneDimensionalCrawler {

	/**
	 * All points are located in a line
	 * 
	 * @deprecated
	 */
	private ResultSet strictOneDimensional() {
		return null;
	}

	/**
	 * All points are located in 2 dimensional space, but the queries are issued
	 * in a line.
	 * 
	 * @return a set of issued points
	 */
	public static ResultSet extendOneDimensional(double longitude) {
		// TODO extendOneDimensional
		return null;
	}

	public static ResultSet extendOneDimensional(String state, int category,
			String query, Envelope envelopeState, double middleLine) {

		return null;
	}
}