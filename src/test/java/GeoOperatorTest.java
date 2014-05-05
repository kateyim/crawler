import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;

import com.vividsolutions.jts.geom.Coordinate;

import paint.PaintShapes;

import utils.GeoOperator;

public class GeoOperatorTest {
	protected static Logger logger = Logger.getLogger(GeoOperatorTest.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeoOperatorTest test = new GeoOperatorTest();
		test.testPointInTriangle();

	}

	public void testClone() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(0.0, 0.0);
		PolygonPoint p2 = new PolygonPoint(1000.0, 0.0);
		PolygonPoint p3 = new PolygonPoint(1000.0, 1000.0);
		PolygonPoint p4 = new PolygonPoint(0.0, 1000.0);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);

		Polygon polygon = new Polygon(points);

		Polygon clone = GeoOperator.clone(polygon);
		clone.addHole(hole1());

		System.out.println(GeoOperator.polygonToString(polygon));
		System.out.println(GeoOperator.polygonToString(clone));

	}

	public void testPointOnEdge() {
		// case 1
		// TriangulationPoint p1 = new TPoint(0.0, 0.0);
		// TriangulationPoint p2 = new TPoint(10.0, 0.0);
		// TriangulationPoint q = new TPoint(5.0, 0.0);
		// case 2:
		// TriangulationPoint p1 = new TPoint(0.0, 0.0);
		// TriangulationPoint p2 = new TPoint(0.0, 10.0);
		// TriangulationPoint q = new TPoint(0.0, 5.0);
		// case 3:
		// TriangulationPoint p1 = new TPoint(0.0, 0.0);
		// TriangulationPoint p2 = new TPoint(10.0, 10.0);
		// TriangulationPoint q = new TPoint(5.0, 5.0);
		// case 4:
		// TriangulationPoint p1 = new TPoint(0.0, 0.0);
		// TriangulationPoint p2 = new TPoint(10.0, 10.0);
		// TriangulationPoint q = new TPoint(0.0, 0.0);
		// case 5 error case
		TriangulationPoint p1 = new TPoint(-1.0, -1.0);
		TriangulationPoint p2 = new TPoint(5.0, 5.0);
		TriangulationPoint q = new TPoint(10.0, 10.0);
		boolean b = GeoOperator.pointOnLineSegment(p1, p2, q);
		System.out.println(b);
	}

	public void testEdgeOnEdge() {
		// case 1:
		// TriangulationPoint p1 = new TPoint(0, 0);
		// TriangulationPoint p2 = new TPoint(10, 0);
		// TriangulationPoint q1 = new TPoint(3, 0);
		// TriangulationPoint q2 = new TPoint(5, 0);
		// case 2:
		// TriangulationPoint p1 = new TPoint(0, 0);
		// TriangulationPoint p2 = new TPoint(10, 10);
		// TriangulationPoint q1 = new TPoint(2, 2);
		// TriangulationPoint q2 = new TPoint(5, 5);
		// case 3:
		// TriangulationPoint p1 = new TPoint(0, 0);
		// TriangulationPoint p2 = new TPoint(0, 10);
		// TriangulationPoint q1 = new TPoint(0, 2);
		// TriangulationPoint q2 = new TPoint(0, 5);
		// case 4:
		TriangulationPoint p1 = new TPoint(0, 0);
		TriangulationPoint p2 = new TPoint(10, 10);
		TriangulationPoint q1 = new TPoint(2, 0);
		TriangulationPoint q2 = new TPoint(5, 8);
		boolean b = GeoOperator.edgeOnEdge(p1, p2, q1, q2);
		System.out.println(b);
	}

	public void testPointInsidePolygon() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(0.0, 0.0);
		PolygonPoint p2 = new PolygonPoint(10.0, 0.0);
		PolygonPoint p3 = new PolygonPoint(10.0, 10.0);
		PolygonPoint p4 = new PolygonPoint(0.0, 10.0);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		Polygon polygon = new Polygon(points);
		//
		// Coordinate outerPoint = new Coordinate(-2, -1);
		// Coordinate p = new Coordinate(-1, 5);
		Coordinate outerPoint = new Coordinate(-1, -1);
		Coordinate p = new Coordinate(5, 5);
		//
		boolean b = GeoOperator.pointInsidePolygon(polygon, outerPoint, p);
		System.out.println(b);

	}

	public void testOutOfMinBoundPoint() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(0.0, 0.0);
		PolygonPoint p2 = new PolygonPoint(10.0, 0.0);
		PolygonPoint p3 = new PolygonPoint(10.0, 10.0);
		PolygonPoint p4 = new PolygonPoint(0.0, 10.0);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		Polygon polygon = new Polygon(points);

		Coordinate c = GeoOperator.outOfMinBoundPoint(polygon);
		System.out.println(c.toString());
	}

	public void testPointInTriangle(){
		Coordinate v1 = new Coordinate(0, 0);
		Coordinate v2 = new Coordinate(10, 0);
		Coordinate v3 = new Coordinate(5, 5);
//		Coordinate pt = new Coordinate(3, 3);
		Coordinate pt = new Coordinate(8, 10);

		boolean b = GeoOperator.pointInTriangle(pt, v1, v2, v3);
		System.out.println(b);
	}
	
	private Polygon hole1() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(428.7500421642064, 623.4085470087348);
		PolygonPoint p2 = new PolygonPoint(571.2499578357936, 623.4085470087348);
		PolygonPoint p3 = new PolygonPoint(642.4999156715872, 500.0);
		PolygonPoint p4 = new PolygonPoint(571.2499578357936, 376.5914529912652);
		PolygonPoint p5 = new PolygonPoint(428.7500421642064, 376.5914529912652);
		PolygonPoint p6 = new PolygonPoint(357.50008432841275, 500.0);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		points.add(p5);
		points.add(p6);

		Polygon polygon = new Polygon(points);
		if (logger.isDebugEnabled()) {
			logger.debug(GeoOperator.polygonToString(polygon));
		}

		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
			PaintShapes.paint.addPolygon(polygon);
			PaintShapes.paint.myRepaint();
		}

		return polygon;
	}

}
