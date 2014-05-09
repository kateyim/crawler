package mo.umac.crawler;

import java.util.ArrayList;
import java.util.List;

import mo.umac.spatial.Circle;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;

import paint.PaintShapes;
import paint.WindowUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class AlgoDCDTTest extends AlgoDCDT {

	protected static Logger logger = Logger.getLogger(AlgoDCDTTest.class.getName());

	public static void main(String[] args) {
		boolean debug = true;
		PaintShapes.painting = true;
		MainYahoo.shutdownLogs(debug);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);

		// testEightCases();
		testTriangulation184();
	}

	public static void testEightCases() {
		// test different cases
		Coordinate center = new Coordinate(100, 100);
		Circle circle = new Circle(center, 50);
		// case 1
		// TPoint p1 = new TPoint(70, 70);
		// TPoint p2 = new TPoint(120, 70);
		// TPoint p3 = new TPoint(100, 120);
		// case 2
		// TPoint p1 = new TPoint(70, 70);
		// TPoint p2 = new TPoint(200, 70);
		// TPoint p3 = new TPoint(100, 120);
		// case 3.1
		// TPoint p1 = new TPoint(70, 70);
		// TPoint p2 = new TPoint(200, 110);
		// TPoint p3 = new TPoint(170, 230);
		// case 3.2
		// TPoint p1 = new TPoint(70, 70);
		// TPoint p2 = new TPoint(160, 100);
		// TPoint p3 = new TPoint(100, 160);
		// case 4.1
		// TPoint p1 = new TPoint(0, 0);
		// TPoint p2 = new TPoint(300, 0);
		// TPoint p3 = new TPoint(100, 300);
		// case 4.2
		// TPoint p1 = new TPoint(30, 70);
		// TPoint p2 = new TPoint(200, 80);
		// TPoint p3 = new TPoint(190, 170);
		// case 4.2: 3 edges
		// TPoint p1 = new TPoint(30, 70);
		// TPoint p2 = new TPoint(160, 100);
		// TPoint p3 = new TPoint(100, 160);
		// case: 4.3a
		// TPoint p1 = new TPoint(0, 0);
		// TPoint p2 = new TPoint(200, 60);
		// TPoint p3 = new TPoint(100, 180);
		// case 4.3a-2
		TPoint p1 = new TPoint(60, 0);
		TPoint p2 = new TPoint(60, 200);
		TPoint p3 = new TPoint(300, 100);
		// case: 4.3b
		// TPoint p1 = new TPoint(10, 40);
		// TPoint p2 = new TPoint(50, 130);
		// TPoint p3 = new TPoint(90, 40);

		DelaunayTriangle triangle = new DelaunayTriangle(p1, p2, p3);
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			WindowUtilities.openInJFrame(PaintShapes.paint, 500, 500);
			// points
			PaintShapes.paint.color = PaintShapes.paint.color.BLACK;
			PaintShapes.paint.addPoint(trans(p1));
			PaintShapes.paint.addPoint(trans(p2));
			PaintShapes.paint.addPoint(trans(p3));
			PaintShapes.paint.myRepaint();
			// circle
			PaintShapes.paint.color = PaintShapes.paint.redTranslucence;
			PaintShapes.paint.addCircle(circle);
			PaintShapes.paint.myRepaint();
		}
		AlgoDCDT algo = new AlgoDCDT();
		Polygon p = algo.intersect(circle, triangle);
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
			PaintShapes.paint.addPolygon(p);
			PaintShapes.paint.myRepaint();
		}
	}

	public static void testTriangulation184() {
		AlgoDCDT dcdt = new AlgoDCDT();
		Envelope envelope = new Envelope(0, 1000, 0, 1000);
		Polygon polygon = dcdt.boundary(envelope);
		ArrayList<Polygon> holeList = new ArrayList<Polygon>();
		//
		List<PolygonPoint> points2 = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(892.6075600452351, 126.0891160523272);
		PolygonPoint p2 = new PolygonPoint(860.4930868756413, 79.56016526467556);
		PolygonPoint p3 = new PolygonPoint(868.4565972949588, 41.85357425309269);
		PolygonPoint p4 = new PolygonPoint(945.1515524802576, 17.451301423924747);
		PolygonPoint p5 = new PolygonPoint(976.3584462974147, 49.77759227817387);
		PolygonPoint p6 = new PolygonPoint(938.1438823510828, 130.23884313922417);
		points2.add(p1);
		points2.add(p2);
		points2.add(p3);
		points2.add(p4);
		points2.add(p5);
		points2.add(p6);
		Polygon polygon2 = new Polygon(points2);
		//
		List<PolygonPoint> points3 = new ArrayList<PolygonPoint>();
		PolygonPoint p31 = new PolygonPoint(868.4565971949588, 41.85357415309269);
		PolygonPoint p32 = new PolygonPoint(811.304352505001, 1.4330045448045564E-7);
		PolygonPoint p33 = new PolygonPoint(945.1515520802576, 17.451301523924748);
		points3.add(p31);
		points3.add(p32);
		points3.add(p33);
		Polygon polygon3 = new Polygon(points3);
		//
		List<PolygonPoint> points1 = new ArrayList<PolygonPoint>();
		PolygonPoint p11 = new PolygonPoint(847.4414004501781, 3.500799716158478E-8);
		PolygonPoint p12 = new PolygonPoint(990.1958694706581, 2.2497779525756566E-9);
		PolygonPoint p13 = new PolygonPoint(990.3794633037343, 3.0609961327716633);
		// PolygonPoint p14 = new PolygonPoint(945.1515524802576, 17.451301423924747);
		PolygonPoint p14 = new PolygonPoint(945.1515523802576, 17.451301323924746);
		PolygonPoint p15 = new PolygonPoint(847.2137489947179, 4.6819485781309425);
		points1.add(p11);
		points1.add(p12);
		points1.add(p13);
		points1.add(p14);
		points1.add(p15);
		Polygon inner = new Polygon(points1);
		//
		holeList.add(polygon2);
		holeList.add(polygon3);
		// dcdt.disturb(polygon, holeList, inner);
		holeList.add(inner);
		dcdt.addHoles(polygon, holeList);
		Poly2Tri.triangulate(polygon);

	}

	public static Coordinate trans(TPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

	public static Coordinate trans(TriangulationPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

}
