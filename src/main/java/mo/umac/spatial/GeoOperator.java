package mo.umac.spatial;

import java.util.ArrayList;
import java.util.List;

import mo.umac.main.CrawlerStrategy;
import mo.umac.metadata.yahoolocal.APOI;

import org.apache.log4j.Logger;
import org.apache.xerces.dom3.DOMConfiguration;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geomgraph.Position;

/**
 * //FIXME all operations should be compared to postgresql operations in order
 * to check the correctness of CRS
 * 
 * 
 * @author kate
 * 
 */
public class GeoOperator {

	protected static Logger logger = Logger.getLogger(GeoOperator.class.getName());

	public final static double RADIUS = 6371007.2;// authalic earth radius of

	// 6371007.2 meters

	public static Envelope lla2ecef(Envelope envelope) {
		// converting the envelope
		double minX = envelope.getMinX();
		double maxX = envelope.getMaxX();
		double minY = envelope.getMinY();
		double maxY = envelope.getMaxY();

		Coordinate p1lla = new Coordinate(minX, minY);
		Coordinate p2lla = new Coordinate(maxX, maxY);

		Coordinate p1ecef = ECEFLLA.lla2ecef(p1lla);
		Coordinate p2ecef = ECEFLLA.lla2ecef(p2lla);

		Envelope envelopeEcef = new Envelope(p1ecef, p2ecef);
		return envelopeEcef;

	}

	public void initCRS() {
		CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
		try {
			CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:4326");
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		System.setProperty("org.geotools.referencing.forceXY", "true");
	}

	/**
	 * Determine the the position of the point to the line
	 * 
	 * @param line
	 * @param point
	 * @return
	 */
	public static int findPosition(LineSegment line, Coordinate point) {
		Coordinate p0 = line.p0;
		if (point.x < p0.x) {
			return Position.LEFT;
		} else if (point.x > p0.x) {
			return Position.RIGHT;
		} else {
			return Position.ON;
		}
	}

	/**
	 * {@link http://paulbourke.net/geometry/circlesphere/}
	 * 
	 * Calculate the intersection of a ray and a sphere
	 * <p>
	 * The line segment is defined from p1 to p2
	 * <p>
	 * The sphere is of radius r and centered at sc
	 * <p>
	 * There are potentially two points of intersection given by
	 * <p>
	 * p = p1 - mu1 (p2 - p1)
	 * <p>
	 * p = p1 + mu2 (p2 - p1)
	 * <p>
	 * Return FALSE if the ray doesn't intersect the sphere.
	 * 
	 * 
	 * @param circle
	 * @param lineSeg
	 * @return
	 */
	public static List intersect(Circle circle, LineSegment lineSeg) {
		logger.debug("--------------intersect------------");
		List<Coordinate> list = new ArrayList<Coordinate>();
		double a, b, c;
		double bb4ac;
		Coordinate p1 = lineSeg.p0;
		Coordinate p2 = lineSeg.p1;
		Coordinate sc = circle.getCenter();
		double r = circle.getRadius();

		Coordinate dp = new Coordinate();
		dp.x = p2.x - p1.x;
		dp.y = p2.y - p1.y;
		a = dp.x * dp.x + dp.y * dp.y;
		b = 2 * (dp.x * (p1.x - sc.x) + dp.y * (p1.y - sc.y));
		c = sc.x * sc.x + sc.y * sc.y;
		c += p1.x * p1.x + p1.y * p1.y;
		c -= 2 * (sc.x * p1.x + sc.y * p1.y);
		c -= r * r;
		bb4ac = b * b - 4 * a * c;
		// logger.debug("a = " + a);
		// logger.debug("b = " + b);
		// logger.debug("c = " + c);
		// logger.debug("bb4ac = " + bb4ac);
		// for line segment
		if (Math.abs(a) < CrawlerStrategy.EPSILON || bb4ac < 0) {
			return null;
		} else {
			double mu1 = (-b - Math.sqrt(bb4ac)) / (2 * a);
			double mu2 = (-b + Math.sqrt(bb4ac)) / (2 * a);
			logger.debug("mu1 = " + mu1);
			logger.debug("mu2 = " + mu2);
			/**
			 * Line segment doesn't intersect and on outside of sphere, in which
			 * case both values of u will either be less than 0 or greater than
			 * 1.
			 **/
			// if ((mu1 < 0 || mu1 > 1) && (mu2 < 0 || mu2 > 1)) {
			// return null;
			// }
			/**
			 * Line segment doesn't intersect and is inside sphere, in which
			 * case one value of u will be negative and the other greater than
			 * 1.
			 */
			// if ((mu1 < 0 || mu2 > 1) || (mu2 < 0 || mu1 > 1)) {
			// return null;
			// }
			/**
			 * Line segment intersects at one point, in which case one value of
			 * u will be between 0 and 1 and the other not.
			 */
			if ((mu1 >= 0 && mu1 <= 1)) {
				Coordinate p11 = new Coordinate();
				p11.x = p1.x + mu1 * (p2.x - p1.x);
				p11.y = p1.y + mu1 * (p2.y - p1.y);
				logger.debug("p11 = " + p11);
				list.add(p11);
			}
			/**
			 * Line segment intersects at two points, in which case both values
			 * of u will be between 0 and 1.
			 */
			if ((mu2 >= 0 && mu2 <= 1)) {
				if (mu2 != mu1) {
					Coordinate p12 = new Coordinate();
					p12.x = p1.x + mu2 * (p2.x - p1.x);
					p12.y = p1.y + mu2 * (p2.y - p1.y);
					logger.debug("p12 = " + p12);
					list.add(p12);
				} else {
					logger.debug("mu2 == mu1");
				}
			}
			/**
			 * Line segment is tangential to the sphere, in which case both
			 * values of u will be the same and between 0 and 1.
			 */
		}
		return list;

		// for line
		// if (Math.abs(a) < CrawlerStrategy.EPSILON || bb4ac < 0) {
		// return null;
		// } else if (Math.abs(bb4ac - 0) < CrawlerStrategy.EPSILON) {
		// double mu = (-b) / (2 * a);
		// Coordinate p = new Coordinate();
		// p.x = p1.x + mu * (p2.x - p1.x);
		// p.y = p1.y + mu * (p2.y - p1.y);
		// logger.debug("p = " + p);
		// list.add(p);
		// return list;
		// } else if (bb4ac > 0) {
		// double mu1 = (-b - Math.sqrt(bb4ac)) / (2 * a);
		// double mu2 = (-b + Math.sqrt(bb4ac)) / (2 * a);
		// Coordinate p11 = new Coordinate();
		// p11.x = p1.x + mu1 * (p2.x - p1.x);
		// p11.y = p1.y + mu1 * (p2.y - p1.y);
		// logger.debug("p11 = " + p11);
		// list.add(p11);
		// Coordinate p12 = new Coordinate();
		// p12.x = p1.x + mu2 * (p2.x - p1.x);
		// p12.y = p1.y + mu2 * (p2.y - p1.y);
		// logger.debug("p12 = " + p12);
		// list.add(p12);
		// return list;
		// }
	}

	/**
	 * {@link http
	 * ://gis.stackexchange.com/questions/36841/line-intersection-with
	 * -circle-on-a-sphere-globe-or-earth}
	 * 
	 * @param circle
	 * @param lineSeg
	 * @return
	 */
	public static List intersectOnEarth(Circle circle, LineSegment lineSeg) {
		List<Coordinate> list = new ArrayList<Coordinate>();
		double pi = Math.PI;// 3.141593;
		double degree = 2 * pi / 360;
		double radian = 1 / degree;
		double radius = RADIUS;
		// input
		Coordinate a0 = new Coordinate(lineSeg.p0.x * degree, lineSeg.p0.y * degree);
		Coordinate b0 = new Coordinate(lineSeg.p1.x * degree, lineSeg.p1.y * degree);
		Coordinate c0 = new Coordinate(circle.getCenter().x * degree, circle.getCenter().y * degree);
		double r = circle.getRadius();
		// projection project (lon, lat) to (R*cos(lat0) * lon, R*lat)
		Coordinate a = new Coordinate(a0.x * Math.cos(c0.x) * radius, a0.y * 1 * radius);
		Coordinate b = new Coordinate(b0.x * Math.cos(c0.x) * radius, b0.y * 1 * radius);
		Coordinate c = new Coordinate(c0.x * Math.cos(c0.x) * radius, c0.y * 1 * radius);
		// Compute coefficients of the quadratic equation
		Coordinate v = new Coordinate(a.x - c.x, a.y - c.y);
		Coordinate u = new Coordinate(b.x - a.x, b.y - a.y);
		double alpha = u.x * u.x + u.y * u.y;
		double beta = u.x * v.x + u.y * v.y;
		double gamma = v.x * v.x + v.y * v.y - r * r;
		// judge the number of intersected points
		// solve the equation
		double delta = beta * beta - alpha * gamma;
		if (delta < 0) {
			return null;
		} else if (Math.abs(delta - 0) < 0.001) {
			// only one result
			double t = -beta / alpha;
			Coordinate x = new Coordinate(a0.x + (b0.x - a0.x) * radian, a0.y + (b0.y - a0.y) * t * radian);
			list.add(x);
		} else {
			double t1 = (-beta - Math.sqrt(delta)) / alpha;
			double t2 = (-beta + Math.sqrt(delta)) / alpha;
			Coordinate x1 = new Coordinate(a0.x + (b0.x - a0.x) * radian, a0.y + (b0.y - a0.y) * t1 * radian);
			Coordinate x2 = new Coordinate(a0.x + (b0.x - a0.x) * radian, a0.y + (b0.y - a0.y) * t2 * radian);
			list.add(x1);
			list.add(x2);
		}

		return list;
	}

	public void geoPoint() {

	}

	public double distance() {
		return 0.0;
	}

	public static void logCoordinate(Coordinate coordinate) {
		logger.info("coordinate: " + coordinate.x + ", " + coordinate.y);
	}

	public static void logLineSegment(LineSegment lineSegment) {
		// logger.info("lineSegment: " + coordinate.x + ", " + coordinate.y);
	}

}
