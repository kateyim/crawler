package mo.umac.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import mo.umac.kallmann.cdt.Constraints;
import mo.umac.kallmann.cdt.Mesh;
import mo.umac.kallmann.cdt.Triangle;
import mo.umac.kallmann.cdt.Vector2d;
import mo.umac.metadata.AQuery;
import mo.umac.metadata.ResultSetD2;
import mo.umac.spatial.Circle;

import org.apache.log4j.Logger;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import paint.PaintShapes;
import utils.GeoOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * With chew triangulation
 * 
 * @author Kate
 * 
 */
public class AlgoDCDT extends Strategy {

	protected static Logger logger = Logger.getLogger(AlgoDCDT.class.getName());

	public AlgoDCDT() {
		super();
		logger.info("------------DCDT Crawler------------");
	}

	@Override
	public void crawl(String state, int category, String query, Envelope envelope) {
		if (logger.isDebugEnabled()) {
			logger.info("------------crawling---------");
			logger.info(envelope.toString());
		}
		// initialize the mesh
		Vector2d a = new Vector2d(envelope.getMinX(), envelope.getMinY());
		Vector2d b = new Vector2d(envelope.getMaxX(), envelope.getMinY());
		Vector2d c = new Vector2d(envelope.getMaxX(), envelope.getMaxY());
		Vector2d d = new Vector2d(envelope.getMinX(), envelope.getMaxY());
		Mesh mesh = new Mesh(a, b, c, d);
		//
		Polygon polygonHexagon = issueFirstHexagon(state, category, query, envelope);
		if (PaintShapes.painting) {
			// PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			// PaintShapes.paint.myRepaint();
			PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
			PaintShapes.paint.addPolygon(polygonHexagon);
			PaintShapes.paint.myRepaint();
		}
		// represents the constraint and the hole
		Constraints constraint = new Constraints(polygonHexagon.getPoints());
		mesh.insertConstraint(constraint);

		mesh.printTriangles();

		boolean finished = false;
		while (!finished) {
			Triangle triangle = mesh.getBiggestTriangle();
			if (triangle == null) {
				finished = true;
				break;
			}
			Circle aCircle = issueCircleLoop(state, category, query, triangle);
			Polygon inner = issueInnerPolygon(aCircle, triangle);
			//
			if (logger.isDebugEnabled()) {
				logger.debug("max triangle = " + triangle.toString());
				logger.debug("aCircle = " + aCircle.toString());
				logger.debug("inner polygon = " + GeoOperator.polygonToString(inner));
			}
			//
			constraint = new Constraints(inner.getPoints());
			mesh.insertConstraint(constraint);
			// mesh.printTriangles();
			if (PaintShapes.painting) {
				//
				PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
				Iterator it = mesh.getTriangles().entrySet().iterator();
				while (it.hasNext()) {
					Entry pairs = (Entry) it.next();
					Triangle t = (Triangle) pairs.getKey();
					PaintShapes.paint.addTriangle(t);
					logger.debug(t.toString());
				}
				//
				// PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
				PaintShapes.paint.addTriangle(triangle);
				// PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
				// PaintShapes.paint.addCircle(aCircle);
				// PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
				PaintShapes.paint.addPolygon(inner);
				PaintShapes.paint.myRepaint();
			}
			logger.debug("finished one iteration");

		}

	}

	private Polygon issueFirstHexagon(String state, int category, String query, Envelope envelope) {
		Coordinate center = envelope.centre();
		AQuery aQuery = new AQuery(center, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		ResultSetD2 resultSet = query(aQuery);
		Coordinate farthestCoordinate = CrawlerD1.farthest(resultSet);
		if (farthestCoordinate == null) {
			logger.error("farestest point is null");
		}
		double radius = center.distance(farthestCoordinate);
		Circle aCircle = new Circle(center, radius);
		resultSet.addACircle(aCircle);
		Polygon polygonHexagon = findInnerHexagon(aCircle);
		return polygonHexagon;
	}

	private Circle issueCircleLoop(String state, int category, String query, Triangle triangle) {
		Vector2d centroid = triangle.centroid();
		Coordinate center = new Coordinate(centroid.getX(), centroid.getY());
		AQuery aQuery = new AQuery(center, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		ResultSetD2 resultSet = query(aQuery);
		Coordinate farthestCoordinate = CrawlerD1.farthest(resultSet);
		if (farthestCoordinate == null) {
			logger.error("farestest point is null");
		}
		double radius = center.distance(farthestCoordinate);
		Circle aCircle = new Circle(center, radius);
		resultSet.addACircle(aCircle);
		return aCircle;
	}

	private Polygon issueInnerPolygon(Circle aCircle, Triangle triangle) {
		Polygon inner = intersect(aCircle, triangle);
		return inner;
	}

	/**
	 * @return
	 */
	// private boolean checkShrinkingTri(Triangle triangle) {
	// // TODO checking
	// Vector2d[] tp = triangle.points;
	// for (int i = 0; i < tp.length; i++) {
	// DoubleWrapper dw = new DoubleWrapper(tp[i].getX(), tp[i].getY());
	//
	// // this point is shrink from another point
	// Vector2d originPoint = pertPointMap.get(dw);
	// if (originPoint != null) {
	// for (int j = 0; j < tp.length; j++) {
	// if (j != i) {
	// Vector2d q = tp[j];
	// if (GeoOperator.equalPointForShrink(originPoint, q)) {
	// // find
	// return true;
	// }
	//
	// }
	// }
	// }
	// Vector2d[] originPoints = pertEdgeMap.get(dw);
	// if (originPoints != null) {
	// Vector2d q1 = null;// = GeoOperator.trans(tp[j]);
	// Vector2d q2 = null;// =
	// for (int j = 0; j < tp.length; j++) {
	// if (j != i) {
	// if (q1 == null) {
	// q1 = tp[j];
	// } else {
	// q2 = tp[j];
	// }
	// }
	// }
	// if (GeoOperator.equalPointForShrink(originPoints[0], q1) && GeoOperator.equalPointForShrink(originPoints[1], q2)) {
	// // find
	// return true;
	// } else if (GeoOperator.equalPointForShrink(originPoints[0], q2) && GeoOperator.equalPointForShrink(originPoints[1], q1)) {
	// // find
	// return true;
	// }
	// }
	//
	// }
	//
	// return false;
	// }

	/**
	 * Hexagon inscribed in a circle
	 * 
	 * @param circle
	 * @return
	 */
	private Polygon findInnerHexagon(Circle circle) {
		Coordinate center = circle.getCenter();
		double x = center.x;
		double y = center.y;
		double r = circle.getRadius();
		PolygonPoint p1 = new PolygonPoint(x - r / 2, y + Math.sqrt(3) / 2 * r);
		PolygonPoint p2 = new PolygonPoint(x + r / 2, y + Math.sqrt(3) / 2 * r);
		PolygonPoint p3 = new PolygonPoint(x + r, y);
		PolygonPoint p4 = new PolygonPoint(x + r / 2, y - Math.sqrt(3) / 2 * r);
		PolygonPoint p5 = new PolygonPoint(x - r / 2, y - Math.sqrt(3) / 2 * r);
		PolygonPoint p6 = new PolygonPoint(x - r, y);
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		points.add(p5);
		points.add(p6);
		Polygon polygon = new Polygon(points);
		return polygon;
	}

	public Polygon intersect(Circle circle, Triangle triangle) {
		Vector2d[] tp = triangle.points;
		boolean[] verticesInsideCircle = { false, false, false };
		int numVerticesInsideCircle = 0;
		for (int i = 0; i < tp.length; i++) {
			Coordinate p = GeoOperator.trans(tp[i]);
			// new Coordinate(tp[i].getX(), tp[i].getY());
			if (circle.inner(p)) {
				verticesInsideCircle[i] = true;
				numVerticesInsideCircle++;
			}
		}
		if (numVerticesInsideCircle == 3) {
			// case 1
			List<PolygonPoint> points = new ArrayList<PolygonPoint>();
			for (int i = 0; i < tp.length; i++) {
				PolygonPoint p1 = new PolygonPoint(tp[i].getX(), tp[i].getY());
				points.add(p1);
			}
			Polygon p = new Polygon(points);
			return p;
		} else if (numVerticesInsideCircle == 2) {
			return case2(circle, triangle, verticesInsideCircle);
		} else if (numVerticesInsideCircle == 1) {
			return case3(circle, triangle, verticesInsideCircle);
		} else if (numVerticesInsideCircle == 0) {
			return case4(circle, triangle);
		}
		return null;
	}

	private Polygon case2(Circle circle, Triangle triangle, boolean[] verticesInsideCircle) {
		int numOutside = -1;
		for (int i = 0; i < verticesInsideCircle.length; i++) {
			if (verticesInsideCircle[i] == false) {
				numOutside = i;
				break;
			}
		}
		Vector2d[] tp = triangle.points;
		Coordinate outerPoint = GeoOperator.trans(tp[numOutside]);// new Coordinate(tp[numOutside].getX(), tp[numOutside].getY());
		Coordinate innerPoint1 = GeoOperator.trans(tp[(numOutside + 1) % 3]);// new Coordinate(tp[(numOutside + 1) % 3].getX(), tp[(numOutside + 1) %
																				// 3].getY());
		Coordinate innerPoint2 = GeoOperator.trans(tp[(numOutside + 2) % 3]);// new Coordinate(tp[(numOutside + 2) % 3].getX(), tp[(numOutside + 2) %
																				// 3].getY());
		Coordinate intersectPoint1 = circle.intersectOneOuter(innerPoint1, outerPoint);
		Coordinate intersectPoint2 = circle.intersectOneOuter(innerPoint2, outerPoint);
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(innerPoint1.x, innerPoint1.y);
		PolygonPoint p2 = new PolygonPoint(innerPoint2.x, innerPoint2.y);
		PolygonPoint p3 = new PolygonPoint(intersectPoint2.x, intersectPoint2.y);
		PolygonPoint p4 = new PolygonPoint(intersectPoint1.x, intersectPoint1.y);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		Polygon p = new Polygon(points);
		return p;
	}

	private Polygon case3(Circle circle, Triangle triangle, boolean[] verticesInsideCircle) {
		int numInner = -1;
		for (int i = 0; i < verticesInsideCircle.length; i++) {
			if (verticesInsideCircle[i] == true) {
				numInner = i;
				break;
			}
		}
		Vector2d[] tp = triangle.points;
		Coordinate innerPoint = GeoOperator.trans(tp[numInner]);// new Coordinate(tp[numInner].getX(), tp[numInner].getY());
		Coordinate outerPoint1 = GeoOperator.trans(tp[(numInner + 1) % 3]);// new Coordinate(tp[(numInner + 1) % 3].getX(), tp[(numInner + 1) % 3].getY());
		Coordinate outerPoint2 = GeoOperator.trans(tp[(numInner + 2) % 3]);// new Coordinate(tp[(numInner + 2) % 3].getX(), tp[(numInner + 2) % 3].getY());
		// b, c
		Coordinate intersectPoint1 = circle.intersectOneOuter(innerPoint, outerPoint1);
		Coordinate intersectPoint2 = circle.intersectOneOuter(innerPoint, outerPoint2);
		// k1, k2
		ArrayList<Coordinate> intersectPoints = circle.intersectTwoOuter(outerPoint1, outerPoint2);

		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		for (int i = 0; i < intersectPoints.size(); i++) {
			Coordinate c = intersectPoints.get(i);
			PolygonPoint p = new PolygonPoint(c.x, c.y);
			points.add(p);
		}
		PolygonPoint p1 = new PolygonPoint(intersectPoint2.x, intersectPoint2.y);
		PolygonPoint p2 = new PolygonPoint(innerPoint.x, innerPoint.y);
		PolygonPoint p3 = new PolygonPoint(intersectPoint1.x, intersectPoint1.y);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		Polygon p = new Polygon(points);
		return p;
	}

	private Polygon case4(Circle circle, Triangle triangle) {
		Vector2d[] tp = triangle.points;

		Coordinate p1 = GeoOperator.trans(tp[0]);// new Coordinate(tp[0].getX(), tp[0].getY());
		Coordinate p2 = GeoOperator.trans(tp[1]);// new Coordinate(tp[1].getX(), tp[1].getY());
		Coordinate p3 = GeoOperator.trans(tp[2]);// new Coordinate(tp[2].getX(), tp[2].getY());
		ArrayList<Coordinate> intersectPoints12 = circle.intersectTwoOuter(p1, p2);
		ArrayList<Coordinate> intersectPoints13 = circle.intersectTwoOuter(p1, p3);
		ArrayList<Coordinate> intersectPoints23 = circle.intersectTwoOuter(p2, p3);
		int[] numIntersectPoints = { intersectPoints12.size(), intersectPoints13.size(), intersectPoints23.size() };
		int numEqualsTwo = 0;
		for (int i = 0; i < numIntersectPoints.length; i++) {
			if (numIntersectPoints[i] == 2) {
				numEqualsTwo++;
			}
		}
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		if (numEqualsTwo == 0) {
			// case 4.1
			return findInnerHexagon(circle);
		} else if (numEqualsTwo == 2) {
			// case 4.2
			if (intersectPoints12.size() == 2 && intersectPoints13.size() == 2) {
				PolygonPoint pp1 = GeoOperator.trans(intersectPoints12.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
				PolygonPoint pp2 = GeoOperator.trans(intersectPoints12.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
				PolygonPoint pp3 = GeoOperator.trans(intersectPoints13.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
				PolygonPoint pp4 = GeoOperator.trans(intersectPoints13.get(0));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
				points.add(pp1);
				points.add(pp2);
				points.add(pp3);
				points.add(pp4);
			} else if (intersectPoints12.size() == 2 && intersectPoints23.size() == 2) {
				PolygonPoint pp1 = GeoOperator.trans(intersectPoints12.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
				PolygonPoint pp2 = GeoOperator.trans(intersectPoints12.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
				// PolygonPoint pp3 = GeoOperator.trans(intersectPoints23.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
				// PolygonPoint pp4 = GeoOperator.trans(intersectPoints23.get(0));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
				// revised by kate 2014-5-6
				PolygonPoint pp3 = GeoOperator.trans(intersectPoints23.get(0));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
				PolygonPoint pp4 = GeoOperator.trans(intersectPoints23.get(1));
				points.add(pp1);
				points.add(pp2);
				points.add(pp3);
				points.add(pp4);
			} else if (intersectPoints13.size() == 2 && intersectPoints23.size() == 2) {
				PolygonPoint pp1 = GeoOperator.trans(intersectPoints13.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
				PolygonPoint pp2 = GeoOperator.trans(intersectPoints13.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
				PolygonPoint pp3 = GeoOperator.trans(intersectPoints23.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
				PolygonPoint pp4 = GeoOperator.trans(intersectPoints23.get(0));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
				points.add(pp1);
				points.add(pp2);
				points.add(pp3);
				points.add(pp4);
			}
			Polygon p = new Polygon(points);
			return p;

		} else if (numEqualsTwo == 3) {
			// case 4.2: 3 edges
			// TODO check order
			PolygonPoint pp1 = GeoOperator.trans(intersectPoints12.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
			PolygonPoint pp2 = GeoOperator.trans(intersectPoints12.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
			PolygonPoint pp3 = GeoOperator.trans(intersectPoints23.get(0));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
			PolygonPoint pp4 = GeoOperator.trans(intersectPoints23.get(1));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
			PolygonPoint pp5 = GeoOperator.trans(intersectPoints13.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
			PolygonPoint pp6 = GeoOperator.trans(intersectPoints13.get(0));
			points.add(pp1);
			points.add(pp2);
			points.add(pp3);
			points.add(pp4);
			points.add(pp5);
			points.add(pp6);
			Polygon p = new Polygon(points);
			return p;
		} else if (numEqualsTwo == 1) {
			// case 4.3
			if (GeoOperator.pointInTriangle(circle.getCenter(), p1, p2, p3)) {
				// case 4.3a
				if (intersectPoints12.size() == 2) {
					Polygon p = case43a(circle, intersectPoints12, p1, p2, p3);
					return p;
				} else if (intersectPoints13.size() == 2) {
					Polygon p = case43a(circle, intersectPoints13, p1, p2, p3);
					return p;
				} else if (intersectPoints23.size() == 2) {
					Polygon p = case43a(circle, intersectPoints23, p1, p2, p3);
					return p;
				}

			} else {
				// case 4.3b
				if (intersectPoints12.size() == 2) {
					Polygon p = case43b(circle, intersectPoints12, p1, p2, p3);
					return p;
				} else if (intersectPoints13.size() == 2) {
					Polygon p = case43b(circle, intersectPoints13, p1, p2, p3);
					return p;
				} else if (intersectPoints23.size() == 2) {
					Polygon p = case43b(circle, intersectPoints23, p1, p2, p3);
					return p;
				}
			}

		}

		return null;
	}

	// private Polygon case43a(Circle circle, ArrayList<Coordinate> intersectPoints, Coordinate v1, Coordinate v2, Coordinate v3) {
	// // assertion: intersectPoints.size() == 2;
	// Coordinate center = circle.getCenter();
	// double x = center.x;
	// double y = center.y;
	// double r = circle.getRadius();
	// //
	// PolygonPoint p1 = new PolygonPoint(x - r / 2, y + Math.sqrt(3) / 2 * r);
	// PolygonPoint p2 = new PolygonPoint(x + r / 2, y + Math.sqrt(3) / 2 * r);
	// PolygonPoint p3 = new PolygonPoint(x + r, y);
	// PolygonPoint p4 = new PolygonPoint(x + r / 2, y - Math.sqrt(3) / 2 * r);
	// PolygonPoint p5 = new PolygonPoint(x - r / 2, y - Math.sqrt(3) / 2 * r);
	// PolygonPoint p6 = new PolygonPoint(x - r, y);
	// List<PolygonPoint> points = new ArrayList<PolygonPoint>();
	//
	// LineSegment l0 = new LineSegment(intersectPoints.get(0), intersectPoints.get(1));
	// ArrayList<LineSegment> lines = new ArrayList<LineSegment>();
	// LineSegment l12 = new LineSegment(GeoOperator.trans(p1), GeoOperator.trans(p2));
	// LineSegment l23 = new LineSegment(GeoOperator.trans(p2), GeoOperator.trans(p3));
	// LineSegment l34 = new LineSegment(GeoOperator.trans(p3), GeoOperator.trans(p4));
	// LineSegment l45 = new LineSegment(GeoOperator.trans(p4), GeoOperator.trans(p5));
	// LineSegment l56 = new LineSegment(GeoOperator.trans(p5), GeoOperator.trans(p6));
	// LineSegment l61 = new LineSegment(GeoOperator.trans(p6), GeoOperator.trans(p1));
	// lines.add(l12);
	// lines.add(l23);
	// lines.add(l34);
	// lines.add(l45);
	// lines.add(l56);
	// lines.add(l61);
	// //
	// ArrayList<Coordinate> intersects = new ArrayList<Coordinate>();
	//
	// for (int i = 0; i < lines.size(); i++) {
	// LineSegment l = lines.get(i);
	// Coordinate inter = l.intersection(l0);
	// if (inter != null) {
	// intersects.add(inter);
	// }
	// }
	// if (intersects.size() > 2) {
	// logger.error("intersects.size()> 2");
	// }
	// if (GeoOperator.pointInTriangle(GeoOperator.trans(p1), v1, v2, v3)) {
	// points.add(p1);
	// }
	// if (GeoOperator.pointInTriangle(GeoOperator.trans(p2), v1, v2, v3)) {
	// points.add(p2);
	// }
	// if (GeoOperator.pointInTriangle(GeoOperator.trans(p3), v1, v2, v3)) {
	// points.add(p3);
	// }
	// if (GeoOperator.pointInTriangle(GeoOperator.trans(p4), v1, v2, v3)) {
	// points.add(p4);
	// }
	// if (GeoOperator.pointInTriangle(GeoOperator.trans(p5), v1, v2, v3)) {
	// points.add(p5);
	// }
	// if (GeoOperator.pointInTriangle(GeoOperator.trans(p6), v1, v2, v3)) {
	// points.add(p6);
	// }
	// for (int i = 0; i < intersects.size(); i++) {
	// if (GeoOperator.pointInTriangle(intersects.get(i), v1, v2, v3)) {
	// points.add(GeoOperator.trans(intersects.get(i)));
	// }
	// }
	// Polygon polygon = new Polygon(points);
	// return polygon;
	// }

	private Polygon case43a(Circle circle, ArrayList<Coordinate> intersectPoints, Coordinate v1, Coordinate v2, Coordinate v3) {
		// revised at 2014-5-6
		// assertion: intersectPoints.size() == 2;
		Coordinate center = circle.getCenter();
		double x = center.x;
		double y = center.y;
		double r = circle.getRadius();
		//
		PolygonPoint p1 = new PolygonPoint(x - r / 2, y + Math.sqrt(3) / 2 * r);
		PolygonPoint p2 = new PolygonPoint(x + r / 2, y + Math.sqrt(3) / 2 * r);
		PolygonPoint p3 = new PolygonPoint(x + r, y);
		PolygonPoint p4 = new PolygonPoint(x + r / 2, y - Math.sqrt(3) / 2 * r);
		PolygonPoint p5 = new PolygonPoint(x - r / 2, y - Math.sqrt(3) / 2 * r);
		PolygonPoint p6 = new PolygonPoint(x - r, y);
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();

		LineSegment l0 = new LineSegment(intersectPoints.get(0), intersectPoints.get(1));
		ArrayList<LineSegment> lines = new ArrayList<LineSegment>();
		LineSegment l12 = new LineSegment(GeoOperator.trans(p1), GeoOperator.trans(p2));
		LineSegment l23 = new LineSegment(GeoOperator.trans(p2), GeoOperator.trans(p3));
		LineSegment l34 = new LineSegment(GeoOperator.trans(p3), GeoOperator.trans(p4));
		LineSegment l45 = new LineSegment(GeoOperator.trans(p4), GeoOperator.trans(p5));
		LineSegment l56 = new LineSegment(GeoOperator.trans(p5), GeoOperator.trans(p6));
		LineSegment l61 = new LineSegment(GeoOperator.trans(p6), GeoOperator.trans(p1));
		lines.add(l12);
		lines.add(l23);
		lines.add(l34);
		lines.add(l45);
		lines.add(l56);
		lines.add(l61);
		//
		// The first line segment
		LineSegment l = lines.get(0);
		Coordinate inter = l.intersection(l0);

		// only one of the following 1st and 3rd conditions is true
		// 1st
		if (GeoOperator.pointInTriangle(l.p0, v1, v2, v3)) {
			points.add(GeoOperator.trans(l.p0));
		}
		if (inter != null) {
			points.add(GeoOperator.trans(inter));
		}
		// 3rd
		if (GeoOperator.pointInTriangle(l.p1, v1, v2, v3)) {
			points.add(GeoOperator.trans(l.p1));
		}
		// middle points
		for (int i = 1; i < lines.size() - 1; i++) {
			l = lines.get(i);
			inter = l.intersection(l0);
			if (inter != null) {
				points.add(GeoOperator.trans(inter));
			}
			if (GeoOperator.pointInTriangle(l.p1, v1, v2, v3)) {
				points.add(GeoOperator.trans(l.p1));
			}
		}
		// The last line segment
		l = lines.get(lines.size() - 1);
		inter = l.intersection(l0);
		if (inter != null) {
			points.add(GeoOperator.trans(inter));
		}

		Polygon polygon = new Polygon(points);
		return polygon;
	}

	private Polygon case43b(Circle circle, ArrayList<Coordinate> intersectPoints, Coordinate v1, Coordinate v2, Coordinate v3) {
		// assertion: intersectPoints.size() == 2;
		// FIXME
		Coordinate p1 = intersectPoints.get(0);
		Coordinate p2 = intersectPoints.get(1);
		Coordinate m = new Coordinate((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);

		Coordinate center = circle.getCenter();
		double r = circle.getRadius();
		double x1 = 0;
		double y1 = 0;
		double x2 = 0;
		double y2 = 0;
		if (p1.x == p2.x) {
			y1 = (p1.y + p2.y) / 2;
			y2 = y1;
			double delta2 = r * r - (y1 - center.y) * (y1 - center.y);
			if (delta2 > 0) {
				double delta = Math.sqrt(delta2);
				x1 = center.x - delta;
				x2 = center.x + delta;
			} else {
				logger.error("error in case 4.3b");
			}

		} else if (p1.y == p2.y) {
			x1 = (p1.x + p2.x) / 2;
			x2 = x1;
			double delta2 = r * r - (x1 - center.x) * (x1 - center.x);
			if (delta2 > 0) {
				double delta = Math.sqrt(delta2);
				y1 = center.y - delta;
				y2 = center.y + delta;
			} else {
				logger.error("error in case 4.3b");
			}
		} else {
			double k1 = (p2.y - p1.y) / (p2.x - p1.x);
			double k2 = -1 / k1;
			double a = 1 + k2 * k2;
			double b = -2 * center.x - 2 * k2 * k2 * m.x + 2 * k2 * (m.y - center.y);
			double c = center.x * center.x + k2 * k2 * m.x * m.x - 2 * k2 * m.x * (m.y - center.y) + (m.y - center.y) * (m.y - center.y) - r * r;
			double delta2 = b * b - 4 * a * c;
			// assert delta > 0;
			if (delta2 > 0) {
				double delta = Math.sqrt(delta2);
				x1 = (-b - delta) / (2 * a);
				x2 = (-b + delta) / (2 * a);
				y1 = k2 * (x1 - m.x) + m.y;
				y2 = k2 * (x2 - m.x) + m.y;

			} else {
				logger.error("error in case 4.3b");
			}
		}
		Coordinate m1 = new Coordinate(x1, y1);
		Coordinate m2 = new Coordinate(x2, y2);
		Coordinate mPrime = null;
		if (GeoOperator.pointInTriangle(m1, v1, v2, v3)) {
			mPrime = m1;
		} else if (GeoOperator.pointInTriangle(m2, v1, v2, v3)) {
			mPrime = m2;
		} else {
			logger.error("error in case 4.3b");
		}
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint pp1 = new PolygonPoint(p1.x, p1.y);
		PolygonPoint pp2 = new PolygonPoint(p2.x, p2.y);
		PolygonPoint ppmPrime = new PolygonPoint(mPrime.x, mPrime.y);
		points.add(pp1);
		points.add(pp2);
		points.add(ppmPrime);
		Polygon p = new Polygon(points);
		return p;
	}

}
