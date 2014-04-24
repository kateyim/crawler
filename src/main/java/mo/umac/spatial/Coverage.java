/**
 * 
 */
package mo.umac.spatial;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * It deals with the general coverage problems.
 * 
 * @author Kate Yim
 * 
 */
public class Coverage {

	/**
	 * Compute the distance (in miles) between two points.
	 * 
	 * @param p1
	 *            A start coordinate
	 * @param p2
	 *            An end coordinate
	 * 
	 */
	public static double coordinateToMiles(Coordinate p1, Coordinate p2) {
		// FIXME check coordinateToMiles
		double mile = 0.0;
		GeodeticCalculator calculator = new GeodeticCalculator();
		calculator.setStartingGeographicPoint(p1.x, p1.y);
		calculator.setDestinationGeographicPoint(p2.x, p2.y);
		double meter = calculator.getOrthodromicDistance();
		mile = meter * 0.000621371190;
		return mile;
	}

	/**
	 * Compute the coordinates represent a distance of a mile.
	 * 
	 * @param p1
	 *            A starting coordinate
	 * @param distance
	 *            the distance (in miles) between two coordinates.
	 */
	public static Coordinate mileToCoordinates(Coordinate p1, double miles, double azimuth) {
		double meter = miles * 1609.34;
		GeodeticCalculator calculator = new GeodeticCalculator();
		// TODO check mileToCoordinates
		calculator.setDirection(azimuth, meter);
		Point2D point = calculator.getDestinationGeographicPoint();
		Coordinate p2 = new Coordinate();
		p2.x = point.getX();
		p2.y = point.getY();
		return p2;
	}

	/**
	 * Divide a MBR into four small rectangles.
	 * 
	 * @param region
	 *            the rectangle
	 * @return A list of envelopes containing 4 small rectangles
	 */
	public static ArrayList<Envelope> divideEnvelope(Envelope region) {
		ArrayList<Envelope> list = new ArrayList<Envelope>();
		double minX = region.getMinX();
		double maxX = region.getMaxX();
		double minY = region.getMinY();
		double maxY = region.getMaxY();
		double halfX = (maxX - minX) / 2;
		double halfY = (maxY - minY) / 2;
		double x2;
		double y2;
		for (double x1 = minX; x1 < maxX; x1 = x1 + halfX) {
			x2 = x1 + halfX;
			for (double y1 = minY; y1 < maxY; y1 = y1 + halfY) {
				y2 = y1 + halfY;
				// FIXME out of memory. How many levels does it drill down? -
				// The reason is the radius.
				Envelope small = new Envelope(x1, x2, y1, y2);
				list.add(small);
			}
		}
		return list;
	}

	/**
	 * Compute the unit rectangle from the region which is represented by a big
	 * rectangle. The simplest implementation is to compute the inscribed square
	 * in the circle. The complex implementation is to consider the inscribed
	 * rectangle.
	 * 
	 * @param rectangle
	 *            this is the given region.
	 * @param maxR
	 *            the maximum radius of the circle
	 * @return A unit rectangle
	 * 
	 */
	public static Envelope computeUnit(Envelope envelope, double maxR) {
		double x = maxR / Math.sqrt(2);
		Envelope unit = new Envelope(0, x, 0, x);
		return unit;
	}

	/**
	 * Compute the circumcircle of the envelope
	 * 
	 * @param envelope
	 * @return the circumcircle
	 */
	public static Circle computeCircle(Envelope envelope) {
		// double height = envelope.getHeight();
		// double width = envelope.getWidth();
		// double radius = Math.sqrt(height * height + width * width) / 2;
		Coordinate center = new Coordinate(envelope.centre().x, envelope.centre().y, 0);
		Coordinate boardPoint = new Coordinate(envelope.getMinX(), envelope.getMinY());
		// FIXME miles should be transfer when issue the query to the website!
		double miles = coordinateToMiles(center, boardPoint);
		Circle circle = new Circle(center, miles);
		return circle;
	}

	/**
	 * In order to cover a line, compute basic information for the query
	 * 
	 * @param line
	 * @return
	 */
	public static Circle computeCircle(LineSegment line) {
		// TODO check
		Coordinate center = line.midPoint();
		// TODO what is the unit of length?
		double length = line.getLength();
		Circle circle = new Circle(center, length / 2);
		return circle;
	}

	/**
	 * The number of sub-regions by dividing the <code>region</code> by
	 * <code>unit</code>
	 * 
	 * @param region
	 * @param unit
	 * @return
	 */
	public static int numsSubRegions(Envelope region, Envelope unit) {
		return (int) (Math.ceil(region.getWidth() / unit.getWidth()) * Math.ceil(region.getHeight() / unit.getHeight()));
	}

}
