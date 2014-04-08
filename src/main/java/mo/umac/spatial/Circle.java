/**
 * 
 */
package mo.umac.spatial;

import java.util.ArrayList;
import java.util.LinkedList;

import mo.umac.crawler.Strategy;

import org.apache.log4j.Logger;

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
		ArrayList<Coordinate> list = line_intersect_Circle(center, radius, interPoint, outerPoint);
		if (list.size() != 1) {
			logger.error("error in intersectOneOuter!");
		}
		return list.get(0);
	}

	public ArrayList<Coordinate> intersectTwoOuter(Coordinate p1, Coordinate p2) {
		return line_intersect_Circle(center, radius, p1, p2);
	}

	/**
	 *
	 * @author Li Honglin
	 * @param point
	 * @param radius
	 * @param p1
	 * @param p2
	 * @return FIXME return in order
	 */
	public static ArrayList<Coordinate> line_intersect_Circle(Coordinate point, double radius, Coordinate p1, Coordinate p2) {
		ArrayList<Coordinate> intersect = new ArrayList<Coordinate>();
		if (Math.abs(p2.x - p1.x) < 1e-6) {
			double x = p1.x;
			double d = point.y * point.y + (x - point.x) * (x - point.x) - radius * radius;
			double delt1 = 4 * point.y * point.y - 4 * d;
			if (delt1 >= 0) {
				double y3 = (2 * point.y + Math.sqrt(delt1)) / 2;
				Coordinate q3 = new Coordinate(x, y3);
				double y4 = (2 * point.y - Math.sqrt(delt1)) / 2;
				Coordinate q4 = new Coordinate(x, y4);
				double v3 = (q3.x - p1.x) * (p2.x - q3.x) + (q3.y - p1.y) * (p2.y - q3.y);
				double v4 = (q4.x - p1.x) * (p2.x - q4.x) + (q4.y - p1.y) * (p2.y - q4.y);
				if (v3 > 0) {
					intersect.add(q3);
				}
				if (v4 > 0) {
					intersect.add(q4);
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("p1.x!=p2.x");
			}
			double k = (p2.y - p1.y) / (p2.x - p1.x);
			double c = p1.y - k * p1.x;
			double delt = 4 * Math.pow(k * c - point.x - k * point.y, 2) - 4 * (1 + k * k) * ((c - point.y) * (c - point.y) + point.x * point.x - radius * radius);
			if (logger.isDebugEnabled()) {
				logger.debug("delt=" + delt + "  k=" + k);
			}

			if (delt >= 0) {
				double x1 = (2 * (point.x + k * point.y - k * c) + Math.sqrt(delt)) / (2 * (1 + k * k));
				double y1 = k * x1 + c;
				Coordinate q1 = new Coordinate(x1, y1);
				double x2 = (2 * (point.x + k * point.y - k * c) - Math.sqrt(delt)) / (2 * (1 + k * k));
				double y2 = k * x2 + c;
				Coordinate q2 = new Coordinate(x2, y2);
				double v1 = (q1.x - p1.x) * (p2.x - q1.x) + (q1.y - p1.y) * (p2.y - q1.y);
				double v2 = (q2.x - p1.x) * (p2.x - q2.x) + (q2.y - p1.y) * (p2.y - q2.y);
				if (v1 > 0) {
					intersect.add(q1);
				}
				if (v2 > 0) {
					intersect.add(q2);
				}
			}
		}
		// in an order
		if(intersect.size() == 2){
			Coordinate c1 = intersect.get(0);
			Coordinate c2 = intersect.get(1);
			ArrayList<Coordinate> intersect2 = new ArrayList<Coordinate>();
			// wrong order
			if(p1.distance(c1) > p1.distance(c2)){
				intersect2.add(c2);
				intersect2.add(c1);
			}
			return intersect2;
		}
		
		return intersect;
	}

}
