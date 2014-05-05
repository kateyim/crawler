package utils;

import java.util.ArrayList;
import java.util.List;

import mo.umac.crawler.Strategy;
import mo.umac.spatial.Circle;
import mo.umac.spatial.ECEFLLA;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geomgraph.Position;

/**
 * //FIXME all operations should be compared to postgresql operations in order to check the correctness of CRS
 * 
 * @author kate
 */
public class GeoOperator {

	protected static Logger logger = Logger.getLogger(GeoOperator.class.getName());
	public final static double EPSILON_EQUAL = 1e-12;
	public final static double EPSILON_LITTLE = 1;

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
	 * {@link http://paulbourke.net/geometry/circlesphere/} Calculate the intersection of a ray and a sphere
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
		if (Math.abs(a) < Strategy.EPSILON || bb4ac < 0) {
			return null;
		} else {
			double mu1 = (-b - Math.sqrt(bb4ac)) / (2 * a);
			double mu2 = (-b + Math.sqrt(bb4ac)) / (2 * a);
			logger.debug("mu1 = " + mu1);
			logger.debug("mu2 = " + mu2);
			/**
			 * Line segment doesn't intersect and on outside of sphere, in which case both values of u will either be less than 0 or greater than 1.
			 **/
			// if ((mu1 < 0 || mu1 > 1) && (mu2 < 0 || mu2 > 1)) {
			// return null;
			// }
			/**
			 * Line segment doesn't intersect and is inside sphere, in which case one value of u will be negative and the other greater than 1.
			 */
			// if ((mu1 < 0 || mu2 > 1) || (mu2 < 0 || mu1 > 1)) {
			// return null;
			// }
			/**
			 * Line segment intersects at one point, in which case one value of u will be between 0 and 1 and the other not.
			 */
			if ((mu1 >= 0 && mu1 <= 1)) {
				Coordinate p11 = new Coordinate();
				p11.x = p1.x + mu1 * (p2.x - p1.x);
				p11.y = p1.y + mu1 * (p2.y - p1.y);
				logger.debug("p11 = " + p11);
				list.add(p11);
			}
			/**
			 * Line segment intersects at two points, in which case both values of u will be between 0 and 1.
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
			 * Line segment is tangential to the sphere, in which case both values of u will be the same and between 0 and 1.
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
	 * {@link http ://gis.stackexchange.com/questions/36841/line-intersection-with -circle-on-a-sphere-globe-or-earth}
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

	/**
	 * Find the intersect point, then sort the order by the distance to the coordinate p1
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
			// if (logger.isDebugEnabled()) {
			// logger.debug("p1.x!=p2.x");
			// }
			double k = (p2.y - p1.y) / (p2.x - p1.x);
			double c = p1.y - k * p1.x;
			double delt = 4 * Math.pow(k * c - point.x - k * point.y, 2) - 4 * (1 + k * k) * ((c - point.y) * (c - point.y) + point.x * point.x - radius * radius);
			// if (logger.isDebugEnabled()) {
			// logger.debug("delt=" + delt + "  k=" + k);
			// }

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
		if (intersect.size() == 2) {
			Coordinate c1 = intersect.get(0);
			Coordinate c2 = intersect.get(1);
			ArrayList<Coordinate> intersect2 = new ArrayList<Coordinate>();
			// wrong order
			if (p1.distance(c1) > p1.distance(c2)) {
				intersect2.add(c2);
				intersect2.add(c1);
				return intersect2;
			}
		}

		return intersect;
	}

	/**
	 * only preserve the basic latitude and longitude informations
	 * 
	 * @param p
	 * @return
	 */
	public static Polygon clone(Polygon p) {
		// check done
		List<TriangulationPoint> points = p.getPoints();
		List<PolygonPoint> pointsc = new ArrayList<PolygonPoint>();
		for (int i = 0; i < points.size(); i++) {
			TriangulationPoint pi = points.get(i);
			double x = pi.getX();
			double y = pi.getY();
			PolygonPoint pic = new PolygonPoint(x, y);
			pointsc.add(pic);

		}
		// TODO clone holes
		Polygon c = new Polygon(pointsc);
		return c;
	}

	public static boolean pointOnLine(TriangulationPoint p1, TriangulationPoint p2, TriangulationPoint q) {
		// check done
		double delta = Math.abs((p2.getX() - p1.getX()) * (q.getY() - p1.getY()) - (p2.getY() - p1.getY()) * (q.getX() - p1.getX()));
		if (delta < EPSILON_EQUAL) {
			return true;
		}
		return false;
	}

	/**
	 * check whether point q lies on the edge constructing by p1 and p2. Including the end points. {@link http
	 * ://stackoverflow.com/questions/328107/how-can-you-determine-a-point-is-between-two-other-points-on-a-line-segment}
	 * 
	 * @param p1
	 * @param p2
	 * @param q
	 * @return
	 */
	public static boolean pointOnLineSegment(TriangulationPoint p1, TriangulationPoint p2, TriangulationPoint q) {
		// check done
		double minY = p1.getY();
		double maxY = p1.getY();
		double minX = p1.getX();
		double maxX = p1.getX();
		// Y
		if (p1.getY() > p2.getY()) {
			minY = p2.getY();
			maxY = p1.getY();
		} else {
			minY = p1.getY();
			maxY = p2.getY();
		}
		// X
		if (p1.getX() > p2.getX()) {
			minX = p2.getX();
			maxX = p1.getX();
		} else {
			minX = p1.getX();
			maxX = p2.getX();
		}

		if (q.getX() >= minX && q.getX() <= maxX && q.getY() >= minY && q.getY() <= maxY) {
			double delta = Math.abs((p2.getX() - p1.getX()) * (q.getY() - p1.getY()) - (p2.getY() - p1.getY()) * (q.getX() - p1.getX()));
			if (delta < EPSILON_EQUAL) {
				return true;
			}
		}

		return false;
	}

	// Given three collinear points p, q, r, the function checks if point q lies on line segment 'pr'
	public static boolean onSegment(Coordinate p, Coordinate q, Coordinate r) {
		if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
			return true;

		return false;
	}

	/**
	 * check whether an edge contains another edge, exclude the case in which one point in an edge is located on the other edge
	 * 
	 * @param p1
	 * @param p2
	 * @param q1
	 * @param q2
	 * @return
	 */
	public static boolean edgeOnEdge(TriangulationPoint p1, TriangulationPoint p2, TriangulationPoint q1, TriangulationPoint q2) {
		// check done
		// find the order of points of each edge;
		double minY1 = p1.getY();
		double maxY1 = p2.getY();
		double minY2 = q1.getY();
		double maxY2 = q2.getY();
		double minX1 = p1.getX();
		double maxX1 = p2.getX();
		double minX2 = q1.getX();
		double maxX2 = q2.getX();
		// Y
		if (p1.getY() > p2.getY()) {
			minY1 = p2.getY();
			maxY1 = p1.getY();
		} else {
			minY1 = p1.getY();
			maxY1 = p2.getY();
		}
		if (q1.getY() > q2.getY()) {
			minY2 = q2.getY();
			maxY2 = q1.getY();
		} else {
			minY2 = q1.getY();
			maxY2 = q2.getY();
		}
		// X
		if (p1.getX() > p2.getX()) {
			minX1 = p2.getX();
			maxX1 = p1.getX();
		} else {
			minX1 = p1.getX();
			maxX1 = p2.getX();
		}
		if (q1.getX() > q2.getX()) {
			minX2 = q2.getX();
			maxX2 = q1.getX();
		} else {
			minX2 = q1.getX();
			maxX2 = q2.getX();
		}

		if (Math.abs(p2.getX() - p1.getX()) < EPSILON_EQUAL) {
			// k_p == 0
			if (Math.abs(q2.getX() - q1.getX()) < EPSILON_EQUAL) {
				// k_q == 0
				if (minY1 < minY2) {
					if (maxY1 > maxY2) {
						return true;
					} else {
						return false;
					}
				} else {
					// minY1 > minY2, cannot be equal, else they has already been reported as intersect.
					if (maxY1 < maxY2) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		} else if (Math.abs(p1.getY() - p2.getY()) < EPSILON_EQUAL) {
			if (Math.abs(q1.getY() - q2.getY()) < EPSILON_EQUAL) {
				if (minX1 < minX2) {
					if (maxX1 > maxX2) {
						return true;
					} else {
						return false;
					}
				} else {
					if (maxX1 < maxX2) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				return false;
			}

		} else {
			double slope1 = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
			double slope2 = (q2.getY() - q1.getY()) / (q2.getX() - q1.getX());
			if (Math.abs(slope2 - slope1) < EPSILON_EQUAL) {
				double b1 = p1.getY() * p2.getX() - p2.getY() * p1.getX();
				double b2 = q1.getY() * q2.getX() - q2.getY() * q1.getX();
				if (Math.abs(b2 - b1) < EPSILON_EQUAL) {
					if (minY1 < minY2) {
						if (maxY1 > maxY2) {
							return true;
						} else {
							return false;
						}
					} else {
						// minY1 > minY2, cannot be equal, else they has already been reported as intersect.
						if (maxY1 < maxY2) {
							return true;
						} else {
							return false;
						}
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * check whether point p lies on/inside the polygon
	 * XXX the line between p and outPoint gets through the corner of two lines, then the number of intersection points will be count twice.
	 * 
	 * @param polygon
	 * @param p
	 * @return
	 */
	public static boolean pointInsidePolygon(Polygon polygon, Coordinate outerPoint, Coordinate p) {
		// check done
		int intersection = 0;
		List<TriangulationPoint> polygonPoints = polygon.getPoints();
		int numCornerPoints = 0;
		for (int i = 0; i < polygonPoints.size(); i++) {
			TriangulationPoint aPoint = polygonPoints.get(i);
			TriangulationPoint nextPoint;
			if (i != polygonPoints.size() - 1) {
				nextPoint = polygonPoints.get(i + 1);
			} else {
				nextPoint = polygonPoints.get(0);
			}
			int num = intersectLineLine(p, outerPoint, trans(aPoint), trans(nextPoint));
			if (num == 1) {
				intersection++;
			} else if (num == 2) {
				intersection++;
				numCornerPoints++;
			}
		}
		intersection -= numCornerPoints / 2;
		/*
		 * If the number of intersections is odd, then the point is inside the polygon
		 */
		if (intersection % 2 == 1) {
			return true;
		}
		return false;
	}

	/**
	 * check whether two line segments intersects {@link http://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/} one lineSegment is p1,q1, the
	 * other is p2, q2
	 * 
	 * @return 0: not intersection; 1: intersect; 2: intersection at the corner point
	 */
	public static int intersectLineLine(Coordinate p1, Coordinate q1, Coordinate p2, Coordinate q2) {
		// check done
		// add by kate 2014-5-5
		if (p1.distance(p2) < EPSILON_EQUAL || p1.distance(q2) < EPSILON_EQUAL || q1.distance(p2) < EPSILON_EQUAL || q1.distance(q2) < EPSILON_EQUAL) {
			return 2;
		}
		if (pointOnLineSegment(trans(p1), trans(q1), trans(p2))) {
			return 2;
		}
		if (pointOnLineSegment(trans(p1), trans(q1), trans(q2))) {
			return 2;
		}
		if (pointOnLineSegment(trans(p2), trans(q2), trans(p1))) {
			return 2;
		}
		if (pointOnLineSegment(trans(p2), trans(q2), trans(q1))) {
			return 2;
		}

		// Find the four orientations needed for general and special cases
		int o1 = orientation(p1, q1, p2);
		int o2 = orientation(p1, q1, q2);
		int o3 = orientation(p2, q2, p1);
		int o4 = orientation(p2, q2, q1);

		// General case
		if (o1 != o2 && o3 != o4)
			return 1;

		// Special Cases
		// p1, q1 and p2 are colinear and p2 lies on segment p1q1
		if (o1 == 0 && onSegment(p1, p2, q1))
			return 1;

		// p1, q1 and p2 are colinear and q2 lies on segment p1q1
		if (o2 == 0 && onSegment(p1, q2, q1))
			return 1;

		// p2, q2 and p1 are colinear and p1 lies on segment p2q2
		if (o3 == 0 && onSegment(p2, p1, q2))
			return 1;

		// p2, q2 and q1 are colinear and q1 lies on segment p2q2
		if (o4 == 0 && onSegment(p2, q1, q2))
			return 1;

		return 0; // Doesn't fall in any of the above cases
	}

	// To find orientation of ordered triplet (p, q, r).
	// The function returns following values
	// 0 --> p, q and r are colinear
	// 1 --> Clockwise
	// 2 --> Counterclockwise
	public static int orientation(Coordinate p, Coordinate q, Coordinate r) {
		// See 10th slides from following link for derivation of the formula
		// http://www.dcs.gla.ac.uk/~pat/52233/slides/Geometry1x1.pdf
		double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

		if (Math.abs(val) < EPSILON_EQUAL)
			return 0; // colinear

		return (val > 0) ? 1 : 2; // clock or counterclock wise
	}

	/**
	 * Find a point out of the polygon, which is smaller than the point with the minX and minY among all points.
	 * 
	 * @param polygon
	 * @return
	 */
	public static Coordinate outOfMinBoundPoint(Polygon polygon) {
		// check done
		List<TriangulationPoint> polygonPoints = polygon.getPoints();
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		for (int i = 0; i < polygonPoints.size(); i++) {
			TriangulationPoint aPoint = polygonPoints.get(i);
			if (aPoint.getX() < minX) {
				minX = aPoint.getX();
			}
			if (aPoint.getY() < minY) {
				minY = aPoint.getY();
			}
		}
		return new Coordinate(minX - EPSILON_LITTLE, minY - EPSILON_LITTLE);
	}

	/**
	 * {@link http://stackoverflow.com/a/2049593/952022}
	 * 
	 * @param pt
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return
	 */
	public static boolean pointInTriangle(Coordinate pt, Coordinate v1, Coordinate v2, Coordinate v3) {
		// check done
		boolean b1, b2, b3;
		b1 = sign(pt, v1, v2) < 0.0f;
		b2 = sign(pt, v2, v3) < 0.0f;
		b3 = sign(pt, v3, v1) < 0.0f;
		return ((b1 == b2) && (b2 == b3));
	}

	public static double sign(Coordinate p1, Coordinate p2, Coordinate p3) {
		return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
	}

	public static void logCoordinate(Coordinate coordinate) {
		logger.info("coordinate: " + coordinate.x + ", " + coordinate.y);
	}

	public static boolean equalPoint(TriangulationPoint pp, TriangulationPoint tp) {
		if (Math.abs(pp.getX() - tp.getX()) < EPSILON_EQUAL && Math.abs(pp.getY() - tp.getY()) < EPSILON_EQUAL) {
			return true;
		}
		return false;
	}

	public static String polygonToString(Polygon inner) {
		StringBuffer sb = new StringBuffer("Polygon: ");
		List<TriangulationPoint> list = inner.getPoints();
		for (int i = 0; i < list.size(); i++) {
			TriangulationPoint p = list.get(i);
			sb.append("[" + p.getX() + ", " + p.getY() + "]; ");
		}
		// TODO add information for holes
		return sb.toString();
	}

	public static String triangleToString(DelaunayTriangle dt) {
		StringBuffer sb = new StringBuffer("triangle: ");
		TriangulationPoint[] tp = dt.points;
		for (int i = 0; i < tp.length; i++) {
			TriangulationPoint p = tp[i];
			sb.append("[" + p.getX() + ", " + p.getY() + "]; ");
		}
		return sb.toString();
	}

	public static PolygonPoint trans(Coordinate c) {
		PolygonPoint p = new PolygonPoint(c.x, c.y);
		return p;
	}

	public static Coordinate trans(TriangulationPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

	public static Coordinate trans(PolygonPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

}
