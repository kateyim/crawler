/**
 * 
 */
package mo.umac.spatial;

import java.util.ArrayList;
import java.util.LinkedList;

import mo.umac.crawler.Strategy;

import org.apache.log4j.Logger;

import utils.GeoOperator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This Circle represents a query which is also an area covered.
 * 
 * @author Kate Yim
 */
public class Circle {

	protected static Logger logger = Logger.getLogger(Circle.class.getName());

	private Coordinate center = null;
	/* The unit of radius is 'm' in the map. */
	private double radius = 0.0;

	public Circle(Coordinate center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public Coordinate getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}

	/**
	 * Test whether a circle contains a point
	 * 
	 * @param circle
	 * @param p
	 * @return
	 */
	public boolean inner(Coordinate p) {
		if (center.distance(p) < radius) {
			return true;
		}
		return false;
	}

	public Coordinate intersectOneOuter(Coordinate interPoint, Coordinate outerPoint) {
		// FIXME yanhui check
//		// for testing
//		logger.info("circle: " + this.toString());
//		logger.info("innerPoint1: " + interPoint.toString());
//		logger.info("outerPoint: " + outerPoint.toString());

		ArrayList<Coordinate> list = GeoOperator.line_intersect_Circle(center, radius, interPoint, outerPoint);
		if (list.size() != 1) {
			logger.error("error in intersectOneOuter!");
		}
		return list.get(0);
	}

	public ArrayList<Coordinate> intersectTwoOuter(Coordinate p1, Coordinate p2) {
		return GeoOperator.line_intersect_Circle(center, radius, p1, p2);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("center: " + center.toString() + ", radius = " + radius);
		return sb.toString();
	}

}
