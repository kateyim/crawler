package mo.umac.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mo.umac.metadata.AQuery;
import mo.umac.metadata.ResultSetD2;
import mo.umac.spatial.Circle;

import org.apache.log4j.Logger;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;

import paint.PaintShapes;
import utils.GeoOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

public class AlgoDCDT extends Strategy {

	protected static Logger logger = Logger.getLogger(AlgoDCDT.class.getName());

	public final double EPSILON_DISTURB = 1e-4;/* * 1000 */; // 1e-7
	public double epsilonMinArea;

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
		Coordinate center = envelope.centre();
		AQuery aQuery = new AQuery(center, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
		ResultSetD2 resultSet = query(aQuery);
		if (logger.isDebugEnabled()) {
			logger.debug("resultSet.getPOIs().size() = " + resultSet.getPOIs().size());
		}
		Coordinate farthestCoordinate = CrawlerD1.farthest(resultSet);
		if (farthestCoordinate == null) {
			logger.error("farestest point is null");
		}
		double radius = center.distance(farthestCoordinate);
		if (logger.isDebugEnabled()) {
			logger.debug("farthestCoordinate = " + farthestCoordinate.toString());
			logger.debug("radius = " + radius);
		}
		Circle aCircle = new Circle(center, radius);
		resultSet.addACircle(aCircle);
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			PaintShapes.paint.addCircle(aCircle);
			PaintShapes.paint.myRepaint();
		}
		//
		ArrayList<Polygon> holeList = new ArrayList<Polygon>();
		//
		Polygon polygonHexagon = findInnerHexagon(aCircle);
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
			PaintShapes.paint.addPolygon(polygonHexagon);
			PaintShapes.paint.myRepaint();
		}
		holeList.add(polygonHexagon);
		Polygon polygon = boundary(envelope);
		addHoles(polygon, holeList);
		//
		epsilonMinArea = EPSILON_DISTURB * Math.max(envelope.getHeight(), envelope.getWidth());
		//
		if (logger.isDebugEnabled()) {
			logger.debug(GeoOperator.polygonToString(polygon));
			logger.debug("EPSILON_MIN_AREA = " + epsilonMinArea);
			// logger.error("--------------------");
			// logger.error(GeoOperator.polygonToString(polygon));

			for (int i = 0; i < holeList.size(); i++) {
				logger.debug(GeoOperator.polygonToString(holeList.get(i)));
				// logger.error(GeoOperator.polygonToString(holeList.get(i)));
			}
		}
		Poly2Tri.triangulate(polygon);

		List<DelaunayTriangle> list = polygon.getTriangles();
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle dt = list.get(i);
				PaintShapes.paint.addTriangle(dt);
			}
			PaintShapes.paint.myRepaint();
		}

		while (list.size() != 0) {
			double maxArea = Double.MIN_VALUE;
			int maxIndex = 0;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle triangle = list.get(i);
				double area = triangle.area();
				if (area > maxArea) {
					maxArea = area;
					maxIndex = i;
				}
			}
			if (maxArea < epsilonMinArea) {
				break;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("List<DelaunayTriangle> size = " + list.size());
				logger.debug("maxArea = " + maxArea);
				logger.debug("maxIndex = " + maxIndex);
				for (int i = 0; i < list.size(); i++) {
					DelaunayTriangle tri = list.get(i);
					logger.debug(GeoOperator.triangleToString(tri));
				}
			}
			DelaunayTriangle triangle = list.get(maxIndex);
			list.clear();
			TPoint centroid = triangle.centroid();
			if (logger.isDebugEnabled()) {
				logger.debug("centroid = " + centroid.toString());
			}
			// issue the centroid query
			center = new Coordinate(centroid.getX(), centroid.getY());
			if (logger.isDebugEnabled() && PaintShapes.painting) {
				PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
				PaintShapes.paint.addTriangle(triangle);
				PaintShapes.paint.color = PaintShapes.paint.color.red;
				PaintShapes.paint.addPoint(center);
				PaintShapes.paint.myRepaint();
			}
			aQuery = new AQuery(center, state, category, query, MAX_TOTAL_RESULTS_RETURNED);
			resultSet = query(aQuery);
			farthestCoordinate = CrawlerD1.farthest(resultSet);
			if (farthestCoordinate == null) {
				logger.error("farestest point is null");
			}
			radius = center.distance(farthestCoordinate);
			if (logger.isDebugEnabled()) {
				logger.debug("radius = " + radius);
			}
			aCircle = new Circle(center, radius);
			resultSet.addACircle(aCircle);
			if (logger.isDebugEnabled() && PaintShapes.painting) {
				PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
				PaintShapes.paint.addCircle(aCircle);
				PaintShapes.paint.myRepaint();
			}
			// if (holeList.size() == 25) {
			// logger.debug("holeList.size() = 25");
			// }
			// if (holeList.size() == 26) {
			// logger.debug("holeList.size() = 26");
			// }
			//
			Polygon inner = intersect(aCircle, triangle);
			// for testing
			// if (logger.isDebugEnabled()) {
			// logger.debug("holeList.size() = " + holeList.size());
			// logger.debug("aCircle = " + aCircle.toString());
			// logger.debug("triangle = " + triangle.toString());
			// logger.debug("inner = " + GeoOperator.polygonToString(inner));
			// }
			// for testing print all triangles: whether this triangle overlapped with a previous one

			// add at 2014-4-17
			if (logger.isDebugEnabled()) {
				logger.debug("before disturb--------------------");
				logger.debug("inner: " + GeoOperator.polygonToString(inner));
				for (int i = 0; i < holeList.size(); i++) {
					logger.debug(i + ": " + GeoOperator.polygonToString(holeList.get(i)));
				}
			}

//			if (holeList.size() == 184) {
//				//
//				for (int i = 0; i < holeList.size(); i++) {
//					Polygon polygon2 = holeList.get(i);
//					PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
//					PaintShapes.paint.addPolygon(polygon2);
//				}
//				PaintShapes.paint.myRepaint();
//
//				logger.info("waiting");
//
//				PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
//				PaintShapes.paint.addTriangle(triangle);
//				PaintShapes.paint.myRepaint();
//
//				PaintShapes.paint.color = PaintShapes.paint.color.red;
//				PaintShapes.paint.addPoint(center);
//				PaintShapes.paint.myRepaint();
//
//				PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
//				PaintShapes.paint.addCircle(aCircle);
//				PaintShapes.paint.myRepaint();
//
//				PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
//				PaintShapes.paint.addPolygon(inner);
//				PaintShapes.paint.myRepaint();
//				//
//				// PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
//				// PaintShapes.paint.addPolygon(holeList.get(131));
//				// PaintShapes.paint.myRepaint();
//				//
//				// PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
//				// PaintShapes.paint.addPolygon(holeList.get(173));
//				// PaintShapes.paint.myRepaint();
//
//				//
//			}
			// end testing

			if (logger.isDebugEnabled() && PaintShapes.painting) {
				// logger.debug(polygonToString(inner));
				// logger.error(GeoOperator.polygonToString(inner));
				PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
				PaintShapes.paint.addPolygon(inner);
				PaintShapes.paint.myRepaint();
			}
			polygon = boundary(envelope);
			disturb(polygon, holeList, inner);
			holeList.add(inner);
			addHoles(polygon, holeList);

			if (logger.isDebugEnabled()) {
				logger.debug("after disturb, but before triangulate--------------------");
				for (int i = 0; i < holeList.size(); i++) {
					logger.debug(i + ": " + GeoOperator.polygonToString(holeList.get(i)));
				}
				logger.debug("--------------------");
			}

			Poly2Tri.triangulate(polygon);
			//
			list = polygon.getTriangles();
			if (logger.isDebugEnabled() && PaintShapes.painting) {
				PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
				for (int i = 0; i < list.size(); i++) {
					DelaunayTriangle dt = list.get(i);
					PaintShapes.paint.addTriangle(dt);
				}
				PaintShapes.paint.myRepaint();
			}
		}
	}

	/**
	 * Disturb the point if it lies on an point/edge exist before
	 * should change the inner, or one hole in the holeList
	 * 
	 * @param boundary
	 * @param holeList
	 * @param newPolygon
	 * @return
	 */
	protected void disturb(Polygon boundary, ArrayList<Polygon> holeList, Polygon newPolygon) {
		ArrayList<TriangulationPoint> newPoints = (ArrayList<TriangulationPoint>) newPolygon.getPoints();
		// 1st: avoid point intersecting with the boundary. If so, change the inner point
		List<TriangulationPoint> boundaryPoints = boundary.getPoints();
		for (int i = 0; i < boundaryPoints.size(); i++) {
			// get an edge of from this boundary
			TriangulationPoint boundaryPoint = boundaryPoints.get(i);
			TriangulationPoint nextBoundaryPoint;
			if (i != boundaryPoints.size() - 1) {
				nextBoundaryPoint = boundaryPoints.get(i + 1);
			} else {
				nextBoundaryPoint = boundaryPoints.get(0);
			}
			//
			for (int j = 0; j < newPoints.size(); j++) {
				TriangulationPoint newPoint = newPoints.get(j);
				if (GeoOperator.pointOnLineSegment(boundaryPoint, nextBoundaryPoint, newPoint)) {
					logger.debug("before shrink, newPoint = " + newPoint.toString());
					// TODO check change the newPolygon
					newPoint = shrink(boundaryPoint, nextBoundaryPoint, newPolygon, newPoint, j);
					logger.debug("after shrink, newPoint = " + newPoint.toString());
				}
			}
		}
		TriangulationPoint nextHolePoint;
		TriangulationPoint nextNewPoint;
		for (int i = 0; i < holeList.size(); i++) {
			Polygon hole = holeList.get(i);
			List<TriangulationPoint> aHolePoints = hole.getPoints();
			for (int j = 0; j < aHolePoints.size(); j++) {
				TriangulationPoint holePoint = aHolePoints.get(j);
				// get an edge of the hole
				if (j != aHolePoints.size() - 1) {
					nextHolePoint = aHolePoints.get(j + 1);
				} else {
					nextHolePoint = aHolePoints.get(0);
				}
				//
				for (int k = 0; k < newPoints.size(); k++) {
					TriangulationPoint newPoint = newPoints.get(k);
					// get an edge of the new polygon
					if (k != newPoints.size() - 1) {
						nextNewPoint = newPoints.get(k + 1);
					} else {
						nextNewPoint = newPoints.get(0);
					}
					// 2nd: avoid overlapping points

					// 3rd: avoid point intersecting with edges: if point on the edge, shrink point
					if (GeoOperator.pointOnLineSegment(holePoint, nextHolePoint, newPoint)) {
						logger.debug("before shrink, newPoint = " + newPoint.toString());
						newPoint = shrink(holePoint, nextHolePoint, newPolygon, newPoint, k);
						logger.debug("after shrink, newPoint = " + newPoint.toString());
					}
					if (GeoOperator.pointOnLineSegment(newPoint, nextNewPoint, holePoint)) {
						logger.debug("before shrink, holePoint = " + holePoint.toString());
						holePoint = shrink(newPoint, nextNewPoint, hole, holePoint, j);
						logger.debug("after shrink, newPoint = " + holePoint.toString());
					}
					// 4th: avoid edge intersecting with edges: shrink one point of the inner polygon
					if (GeoOperator.edgeOnEdge(holePoint, nextHolePoint, newPoint, nextNewPoint)) {
						logger.debug("edgeOnEdge:");
						// change the judgment in the shrink: done (by pointOnLine() function)
						shrink(holePoint, nextHolePoint, newPolygon, newPoint, k);
					}
				}
			}
		}
	}

	// /**
	// * shrink the j-th point point in the polygon
	// * check: should shrink to the inner direction!!! done
	// *
	// * @param polygon
	// * @param point
	// * : shrink this point
	// * @param j
	// * : point is the j-th point in the polygon
	// */
	// private TriangulationPoint shrink(TriangulationPoint p1, TriangulationPoint p2, Polygon polygon, TriangulationPoint point, int j) {
	// // check done
	// List<TriangulationPoint> points = polygon.getPoints();
	// // check this function: done
	// Coordinate outerPoint = GeoOperator.outOfMinBoundPoint(polygon);
	// // if (logger.isDebugEnabled()) {
	// // logger.debug("shrink...");
	// // logger.debug("points.size() = " + points.size() + ", j = " + j);
	// // }
	// double x = point.getX();
	// double y = point.getY();
	//
	// double xDis = x;
	// double yDis = y;
	//
	// boolean inside = false;
	// int factorC = 1;
	// TriangulationPoint disturbPoint = null;
	// while (!inside) {
	// double factor = EPSILON_DISTURB * factorC;
	// if (logger.isDebugEnabled()) {
	// logger.debug("factorC = " + factorC);
	// }
	// if (factorC >= 1000) {
	// logger.error("factorC >= 1000: factorC = " + factorC);
	// }
	// // duplicate when factor = 2 * EPSILON_DISTURB, i, k shouldn't equal to 0 anymore
	// for (double i = -1 * factor; i <= factor; i = i + EPSILON_DISTURB) {
	// if (logger.isDebugEnabled()) {
	// logger.debug("i = " + i);
	// }
	// if (!inside) {
	// if (Math.abs(i) < GeoOperator.EPSILON_EQUAL) {
	// // xDis == x
	// // for testing
	// logger.debug("i = " + 0);
	// continue;
	// }
	// xDis = x + i;
	// for (double k = -1 * factor; k <= factor; k = k + EPSILON_DISTURB) {
	// if (logger.isDebugEnabled()) {
	// logger.debug("k = " + k);
	// }
	// if (Math.abs(k) < GeoOperator.EPSILON_EQUAL) {
	// // yDis == y
	// // for testing
	// logger.debug("k = " + 0);
	// continue;
	// }
	// yDis = y + k;
	// Coordinate disturbCoor = new Coordinate(xDis, yDis);
	// disturbPoint = new TPoint(xDis, yDis);
	// // should not include the point on the edge!
	// if (GeoOperator.pointInsidePolygon(polygon, outerPoint, disturbCoor)) {
	// if (!GeoOperator.pointOnLine(p1, p2, disturbPoint)) {
	// inside = true;
	// // check whether jump out of the while loop: done
	// break;
	// }
	// }
	// }
	// } else {
	// break;
	// }
	// }
	// factorC++;
	// // factor *= factorC;
	// }
	// // check whether replaced it properly done
	// points.set(j, disturbPoint);
	// return disturbPoint;
	//
	// }

	/**
	 * shrink the j-th point point in the polygon
	 * check: should shrink to the inner direction!!! done
	 * 
	 * @param polygon
	 * @param point
	 *            : shrink this point
	 * @param j
	 *            : point is the j-th point in the polygon
	 */
	private TriangulationPoint shrink(TriangulationPoint p1, TriangulationPoint p2, Polygon polygon, TriangulationPoint point, int j) {
		// check done
		List<TriangulationPoint> points = polygon.getPoints();
		// XXX although I define the direction to inside the polygon before hand, but it may also exceed the boundaries of the polygon.
		// So it is still necessary to check the property of inside polygon. (this is the special case)
		// find the adjacent points
		TriangulationPoint pointPre;
		TriangulationPoint pointNext;
		if (j != points.size() - 1) {
			pointNext = points.get(j + 1);
		} else {
			pointNext = points.get(0);
		}
		if (j != 0) {
			pointPre = points.get(j - 1);
		} else {
			pointPre = points.get(points.size() - 1);
		}
		// The bisectric vector
		double[] e = GeoOperator.bisectric(pointPre.getX(), pointPre.getY(), pointNext.getX(), pointNext.getY(), point.getX(), point.getY());
//		double unit = GeoOperator.size(e[0], e[1]);
		if(logger.isDebugEnabled()) {
			logger.debug("e: " + e[0] +  ", " + e[1]);
//			logger.debug("unit = " + unit);
		}
		// check this function: done
		Coordinate outerPoint = GeoOperator.outOfMinBoundPoint(polygon);
		
		boolean inside = false;
		TriangulationPoint disturbPoint = null;
		double[] xy = new double[2]; 

		while (!inside) {
			// generate
			Random generator = new Random(System.currentTimeMillis());
			double random = generator.nextDouble();
			// with fixed precision
			double delta = EPSILON_DISTURB + random * EPSILON_DISTURB;
			//
			double distance = delta;
			xy = GeoOperator.locateByVector(point.getX(), point.getY(), e, distance);
			
			if(logger.isDebugEnabled()) {
				logger.debug("random = " + random);
				logger.debug("delta = " + delta);
				logger.debug("distance = " + distance);
				logger.debug("xy = " + xy[0] + ", " + xy[1]);
			}
			
			Coordinate disturbCoor = new Coordinate(xy[0], xy[1]);
			disturbPoint = new TPoint(xy[0], xy[1]);
			// should not include the point on the edge!
			if (GeoOperator.pointInsidePolygon(polygon, outerPoint, disturbCoor)) {
				if (!GeoOperator.pointOnLine(p1, p2, disturbPoint)) {
					inside = true;
					// check whether jump out of the while loop: done
					break;
				}
			}
		}
		// check whether replaced it properly done
		points.set(j, disturbPoint);
		return disturbPoint;

	}

	protected void addHoles(Polygon polygon, ArrayList<Polygon> holeList) {
		for (int i = 0; i < holeList.size(); i++) {
			Polygon hole = GeoOperator.clone(holeList.get(i));
			polygon.addHole(hole);
		}
	}

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

	protected Polygon boundary(Envelope envelope) {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(envelope.getMinX(), envelope.getMinY());
		PolygonPoint p2 = new PolygonPoint(envelope.getMaxX(), envelope.getMinY());
		PolygonPoint p3 = new PolygonPoint(envelope.getMaxX(), envelope.getMaxY());
		PolygonPoint p4 = new PolygonPoint(envelope.getMinX(), envelope.getMaxY());
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		Polygon polygon = new Polygon(points);
		return polygon;
	}

	public Polygon intersect(Circle circle, DelaunayTriangle triangle) {
		TriangulationPoint[] tp = triangle.points;
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

	private Polygon case2(Circle circle, DelaunayTriangle triangle, boolean[] verticesInsideCircle) {
		int numOutside = -1;
		for (int i = 0; i < verticesInsideCircle.length; i++) {
			if (verticesInsideCircle[i] == false) {
				numOutside = i;
				break;
			}
		}
		TriangulationPoint[] tp = triangle.points;
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

	private Polygon case3(Circle circle, DelaunayTriangle triangle, boolean[] verticesInsideCircle) {
		int numInner = -1;
		for (int i = 0; i < verticesInsideCircle.length; i++) {
			if (verticesInsideCircle[i] == true) {
				numInner = i;
				break;
			}
		}
		TriangulationPoint[] tp = triangle.points;
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

	private Polygon case4(Circle circle, DelaunayTriangle triangle) {
		TriangulationPoint[] tp = triangle.points;

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
