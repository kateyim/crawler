package mo.umac.crawler;

import java.util.ArrayList;
import java.util.List;

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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

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
		if (logger.isDebugEnabled()) {
			logger.debug(polygonToString(polygon));
			logger.error("--------------------");
			logger.error(polygonToString(polygon));

			for (int i = 0; i < holeList.size(); i++) {
				logger.debug(polygonToString(holeList.get(i)));
				logger.error(polygonToString(holeList.get(i)));
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
			if (logger.isDebugEnabled()) {
				logger.debug("List<DelaunayTriangle> size = " + list.size());
				logger.debug("maxArea = " + maxArea);
				logger.debug("maxIndex = " + maxIndex);
				for (int i = 0; i < list.size(); i++) {
					DelaunayTriangle tri = list.get(i);
					logger.debug(triangleToString(tri));
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
			//
			Polygon inner = intersect(aCircle, triangle);
			if (logger.isDebugEnabled() && PaintShapes.painting) {
				// logger.debug(polygonToString(inner));
				logger.error(polygonToString(inner));
				PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
				PaintShapes.paint.addPolygon(inner);
				PaintShapes.paint.myRepaint();
			}

			holeList.add(inner);
			polygon = boundary(envelope);
			addHoles(polygon, holeList);
			// add at 2014-4-17
			if (logger.isDebugEnabled()) {
				logger.debug(polygonToString(polygon));
				logger.error("--------------------");
				logger.error(polygonToString(polygon));

				for (int i = 0; i < holeList.size(); i++) {
					logger.debug(polygonToString(holeList.get(i)));
					logger.error(polygonToString(holeList.get(i)));

				}
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

	private void addHoles(Polygon polygon, ArrayList<Polygon> holeList) {
		for (int i = 0; i < holeList.size(); i++) {
			Polygon hole = clone(holeList.get(i));
			polygon.addHole(hole);
		}
	}

	/**
	 * only preserve the basic latitude and longitude informations
	 * 
	 * @param p
	 * @return
	 */
	public static Polygon clone(Polygon p) {
		List<TriangulationPoint> points = p.getPoints();
		List<PolygonPoint> pointsc = new ArrayList<PolygonPoint>();
		for (int i = 0; i < points.size(); i++) {
			TriangulationPoint pi = points.get(i);
			double x = pi.getX();
			double y = pi.getY();
			PolygonPoint pic = new PolygonPoint(x, y);
			pointsc.add(pic);

		}
		Polygon c = new Polygon(pointsc);
		return c;
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

	private Polygon boundary(Envelope envelope) {
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
			Coordinate p = trans(tp[i]);
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
		Coordinate outerPoint = trans(tp[numOutside]);// new Coordinate(tp[numOutside].getX(), tp[numOutside].getY());
		Coordinate innerPoint1 = trans(tp[(numOutside + 1) % 3]);// new Coordinate(tp[(numOutside + 1) % 3].getX(), tp[(numOutside + 1) % 3].getY());
		Coordinate innerPoint2 = trans(tp[(numOutside + 2) % 3]);// new Coordinate(tp[(numOutside + 2) % 3].getX(), tp[(numOutside + 2) % 3].getY());
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
		Coordinate innerPoint = trans(tp[numInner]);// new Coordinate(tp[numInner].getX(), tp[numInner].getY());
		Coordinate outerPoint1 = trans(tp[(numInner + 1) % 3]);// new Coordinate(tp[(numInner + 1) % 3].getX(), tp[(numInner + 1) % 3].getY());
		Coordinate outerPoint2 = trans(tp[(numInner + 2) % 3]);// new Coordinate(tp[(numInner + 2) % 3].getX(), tp[(numInner + 2) % 3].getY());
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

		Coordinate p1 = trans(tp[0]);// new Coordinate(tp[0].getX(), tp[0].getY());
		Coordinate p2 = trans(tp[1]);// new Coordinate(tp[1].getX(), tp[1].getY());
		Coordinate p3 = trans(tp[2]);// new Coordinate(tp[2].getX(), tp[2].getY());
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
				PolygonPoint pp1 = trans(intersectPoints12.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
				PolygonPoint pp2 = trans(intersectPoints12.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
				PolygonPoint pp3 = trans(intersectPoints13.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
				PolygonPoint pp4 = trans(intersectPoints13.get(0));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
				points.add(pp1);
				points.add(pp2);
				points.add(pp3);
				points.add(pp4);
			} else if (intersectPoints12.size() == 2 && intersectPoints23.size() == 2) {
				PolygonPoint pp1 = trans(intersectPoints12.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
				PolygonPoint pp2 = trans(intersectPoints12.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
				PolygonPoint pp3 = trans(intersectPoints23.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
				PolygonPoint pp4 = trans(intersectPoints23.get(0));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
				points.add(pp1);
				points.add(pp2);
				points.add(pp3);
				points.add(pp4);
			} else if (intersectPoints13.size() == 2 && intersectPoints23.size() == 2) {
				PolygonPoint pp1 = trans(intersectPoints13.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
				PolygonPoint pp2 = trans(intersectPoints13.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
				PolygonPoint pp3 = trans(intersectPoints23.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
				PolygonPoint pp4 = trans(intersectPoints23.get(0));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
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
			PolygonPoint pp1 = trans(intersectPoints12.get(0));// new PolygonPoint(intersectPoints12.get(0).x, intersectPoints12.get(0).y);
			PolygonPoint pp2 = trans(intersectPoints12.get(1));// new PolygonPoint(intersectPoints12.get(1).x, intersectPoints12.get(1).y);
			PolygonPoint pp3 = trans(intersectPoints23.get(0));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
			PolygonPoint pp4 = trans(intersectPoints23.get(1));// new PolygonPoint(intersectPoints13.get(0).x, intersectPoints13.get(0).y);
			PolygonPoint pp5 = trans(intersectPoints13.get(1));// new PolygonPoint(intersectPoints13.get(1).x, intersectPoints13.get(1).y);
			PolygonPoint pp6 = trans(intersectPoints13.get(0));
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
			if (PointInTriangle(circle.getCenter(), p1, p2, p3)) {
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

	/**
	 * {@link http://stackoverflow.com/a/2049593/952022}
	 * 
	 * @param pt
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return
	 */
	public static boolean PointInTriangle(Coordinate pt, Coordinate v1, Coordinate v2, Coordinate v3) {
		boolean b1, b2, b3;

		b1 = sign(pt, v1, v2) < 0.0f;
		b2 = sign(pt, v2, v3) < 0.0f;
		b3 = sign(pt, v3, v1) < 0.0f;

		return ((b1 == b2) && (b2 == b3));
	}

	public static double sign(Coordinate p1, Coordinate p2, Coordinate p3) {
		return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
	}

	private Polygon case43a(Circle circle, ArrayList<Coordinate> intersectPoints, Coordinate v1, Coordinate v2, Coordinate v3) {
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
		LineSegment l12 = new LineSegment(trans(p1), trans(p2));
		LineSegment l23 = new LineSegment(trans(p2), trans(p3));
		LineSegment l34 = new LineSegment(trans(p3), trans(p4));
		LineSegment l45 = new LineSegment(trans(p4), trans(p5));
		LineSegment l56 = new LineSegment(trans(p5), trans(p6));
		LineSegment l61 = new LineSegment(trans(p6), trans(p1));
		lines.add(l12);
		lines.add(l23);
		lines.add(l34);
		lines.add(l45);
		lines.add(l56);
		lines.add(l61);
		//
		ArrayList<Coordinate> intersects = new ArrayList<Coordinate>();

		for (int i = 0; i < lines.size(); i++) {
			LineSegment l = lines.get(i);
			Coordinate inter = l.intersection(l0);
			if (inter != null) {
				intersects.add(inter);
			}
		}
		if (intersects.size() > 2) {
			logger.error("intersects.size()> 2");
		}
		if (PointInTriangle(trans(p1), v1, v2, v3)) {
			points.add(p1);
		}
		if (PointInTriangle(trans(p2), v1, v2, v3)) {
			points.add(p2);
		}
		if (PointInTriangle(trans(p3), v1, v2, v3)) {
			points.add(p3);
		}
		if (PointInTriangle(trans(p4), v1, v2, v3)) {
			points.add(p4);
		}
		if (PointInTriangle(trans(p5), v1, v2, v3)) {
			points.add(p5);
		}
		if (PointInTriangle(trans(p6), v1, v2, v3)) {
			points.add(p6);
		}
		for (int i = 0; i < intersects.size(); i++) {
			if (PointInTriangle(intersects.get(i), v1, v2, v3)) {
				points.add(trans(intersects.get(i)));
			}
		}
		Polygon polygon = new Polygon(points);
		return polygon;
	}

	public static Coordinate trans(PolygonPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

	public static Coordinate trans(TriangulationPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

	public static PolygonPoint trans(Coordinate c) {
		PolygonPoint p = new PolygonPoint(c.x, c.y);
		return p;
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
		if (PointInTriangle(m1, v1, v2, v3)) {
			mPrime = m1;
		} else if (PointInTriangle(m2, v1, v2, v3)) {
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

	private String triangleToString(DelaunayTriangle dt) {
		StringBuffer sb = new StringBuffer("triangle: ");
		TriangulationPoint[] tp = dt.points;
		for (int i = 0; i < tp.length; i++) {
			TriangulationPoint p = tp[i];
			sb.append("[" + p.getX() + ", " + p.getY() + "]; ");
		}
		return sb.toString();
	}

	public static String polygonToString(Polygon inner) {
		StringBuffer sb = new StringBuffer("Polygon: ");
		List<TriangulationPoint> list = inner.getPoints();
		for (int i = 0; i < list.size(); i++) {
			TriangulationPoint p = list.get(i);
			sb.append("[" + p.getX() + ", " + p.getY() + "]; ");
		}
		return sb.toString();
	}

}
