/**
 * 
 */
package mo.umac.spatial;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author kate
 * @deprecated
 */
public class MyEnvelope {
	/**
	 * the minimum x-coordinate
	 */
	private double minx;

	/**
	 * the maximum x-coordinate
	 */
	private double maxx;

	/**
	 * the minimum y-coordinate
	 */
	private double miny;

	/**
	 * Creates an <code>Envelope</code> for a region defined by maximum and minimum values.
	 * 
	 * @param x1
	 *            the first x-value
	 * @param x2
	 *            the second x-value
	 * @param y1
	 *            the first y-value
	 * @param y2
	 *            the second y-value
	 */
	public MyEnvelope(double x1, double x2, double y1, double y2) {
		init(x1, x2, y1, y2);
	}

	/**
	 * the maximum y-coordinate
	 */
	private double maxy;

	public MyEnvelope(Coordinate p1, Coordinate p2) {
		init(p1.x, p2.x, p1.y, p2.y);
	}

	/**
	 * Creates an <code>Envelope</code> for a region defined by a single Coordinate.
	 * 
	 * @param p1
	 *            the Coordinate
	 */
	public MyEnvelope(Coordinate p) {
		init(p.x, p.x, p.y, p.y);
	}

	/**
	 * Initialize an <code>Envelope</code> for a region defined by maximum and minimum values.
	 * 
	 * @param x1
	 *            the first x-value
	 * @param x2
	 *            the second x-value
	 * @param y1
	 *            the first y-value
	 * @param y2
	 *            the second y-value
	 */
	public void init(double x1, double x2, double y1, double y2) {
		if (x1 < x2) {
			minx = x1;
			maxx = x2;
		} else {
			minx = x2;
			maxx = x1;
		}
		if (y1 < y2) {
			miny = y1;
			maxy = y2;
		} else {
			miny = y2;
			maxy = y1;
		}
	}

	/**
	 * Initialize an <code>Envelope</code> to a region defined by two Coordinates.
	 * 
	 * @param p1
	 *            the first Coordinate
	 * @param p2
	 *            the second Coordinate
	 */
	public void init(Coordinate p1, Coordinate p2) {
		init(p1.x, p2.x, p1.y, p2.y);
	}

	/**
	 * Returns <code>true</code> if this <code>Envelope</code> is a "null" envelope.
	 * 
	 * @return <code>true</code> if this <code>Envelope</code> is uninitialized or is the envelope of the empty geometry.
	 */
	public boolean isNull() {
		return maxx < minx;
	}

	/**
	 * Returns the difference between the maximum and minimum x values.
	 * 
	 * @return max x - min x, or 0 if this is a null <code>Envelope</code>
	 */
	public double getWidth() {
		if (isNull()) {
			return 0;
		}
		return maxx - minx;
	}

	/**
	 * Returns the difference between the maximum and minimum y values.
	 * 
	 * @return max y - min y, or 0 if this is a null <code>Envelope</code>
	 */
	public double getHeight() {
		if (isNull()) {
			return 0;
		}
		return maxy - miny;
	}

	/**
	 * Returns the <code>Envelope</code>s minimum x-value. min x > max x indicates that this is a null <code>Envelope</code>.
	 * 
	 * @return the minimum x-coordinate
	 */
	public double getMinX() {
		return minx;
	}

	/**
	 * Returns the <code>Envelope</code>s maximum x-value. min x > max x indicates that this is a null <code>Envelope</code>.
	 * 
	 * @return the maximum x-coordinate
	 */
	public double getMaxX() {
		return maxx;
	}

	/**
	 * Returns the <code>Envelope</code>s minimum y-value. min y > max y indicates that this is a null <code>Envelope</code>.
	 * 
	 * @return the minimum y-coordinate
	 */
	public double getMinY() {
		return miny;
	}

	/**
	 * Returns the <code>Envelope</code>s maximum y-value. min y > max y indicates that this is a null <code>Envelope</code>.
	 * 
	 * @return the maximum y-coordinate
	 */
	public double getMaxY() {
		return maxy;
	}

	@Override
	public String toString() {
		return "MyEnv[" + minx + " : " + maxx + " , " + miny + " : " + maxy + "]";
	}
}
