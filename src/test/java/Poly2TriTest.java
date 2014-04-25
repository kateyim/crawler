import java.util.ArrayList;
import java.util.List;

import mo.umac.crawler.MainYahoo;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import paint.PaintShapes;
import paint.WindowUtilities;
import utils.GeoOperator;

public class Poly2TriTest {

	protected static Logger logger = Logger.getLogger(Poly2TriTest.class.getName());

	public double EPSILON = 1e-6/* 1e-12 */;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean debug = true;
		PaintShapes.painting = true;
		MainYahoo.shutdownLogs(debug);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
		if (PaintShapes.painting) {
			WindowUtilities.openInJFrame(PaintShapes.paint, 1000, 1000);
		}
		Poly2TriTest test = new Poly2TriTest();
		test.testShrink1();
	}

	/**
	 * It's ok
	 */
	public void testShrink1() {
		Polygon boundary = boundary();
		boundary.addHole(hole1());
		boundary.addHole(hole2());
		boundary.addHole(hole3());
		// boundary.addHole(hole13());
		boundary.addHole(hole4());
		boundary.addHole(hole5());
		boundary.addHole(hole6());
		// boundary.addHole(hole136());

		Poly2Tri.triangulate(boundary);

		List<DelaunayTriangle> list = boundary.getTriangles();
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle dt = list.get(i);
				PaintShapes.paint.addTriangle(dt);
			}
			PaintShapes.paint.myRepaint();
		}
	}

	public void testShrink2() {
		Polygon boundary = boundary();
		boundary.addHole(hole1());
		Poly2Tri.triangulate(boundary);
		List<DelaunayTriangle> list = boundary.getTriangles();
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle dt = list.get(i);
				PaintShapes.paint.addTriangle(dt);
			}
			PaintShapes.paint.myRepaint();
		}
		//
		boundary = boundary();
		boundary.addHole(hole1());
		boundary.addHole(hole2());
		Poly2Tri.triangulate(boundary);
		list = boundary.getTriangles();
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle dt = list.get(i);
				PaintShapes.paint.addTriangle(dt);
			}
			PaintShapes.paint.myRepaint();
		}
		//
		boundary = boundary();
		boundary.addHole(hole1());
		boundary.addHole(hole2());
		boundary.addHole(hole3());
		Poly2Tri.triangulate(boundary);
		list = boundary.getTriangles();
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle dt = list.get(i);
				PaintShapes.paint.addTriangle(dt);
			}
			PaintShapes.paint.myRepaint();
		}
		//
		boundary = boundary();
		boundary.addHole(hole1());
		boundary.addHole(hole2());
		boundary.addHole(hole3());
		boundary.addHole(hole4());
		Poly2Tri.triangulate(boundary);

		list = boundary.getTriangles();
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle dt = list.get(i);
				PaintShapes.paint.addTriangle(dt);
			}
			PaintShapes.paint.myRepaint();
		}
		//
		boundary = boundary();
		boundary.addHole(hole1());
		boundary.addHole(hole2());
		boundary.addHole(hole3());
		boundary.addHole(hole4());
		boundary.addHole(hole5());

		Poly2Tri.triangulate(boundary);

		list = boundary.getTriangles();
		if (logger.isDebugEnabled() && PaintShapes.painting) {
			PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
			for (int i = 0; i < list.size(); i++) {
				DelaunayTriangle dt = list.get(i);
				PaintShapes.paint.addTriangle(dt);
			}
			PaintShapes.paint.myRepaint();
		}
	}

	private Polygon boundary() {
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
		if (logger.isDebugEnabled()) {
			logger.debug(GeoOperator.polygonToString(polygon));
		}

		// if (logger.isDebugEnabled() && PaintShapes.painting) {
		// PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
		// PaintShapes.paint.addRectangle(new Envelope(0.0, 1000, 0, 1000));
		// PaintShapes.paint.myRepaint();
		// }
		return polygon;
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

	private Polygon hole2() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(244.8906951930522, 784.9009127526291);
		PolygonPoint p2 = new PolygonPoint(417.27284706091024, 633.4895106359271);
		PolygonPoint p3 = new PolygonPoint(437.80056771258273, 629.3750254650489);
		PolygonPoint p4 = new PolygonPoint(716.6601402050248, 813.2107179582445);
		PolygonPoint p5 = new PolygonPoint(690.2401226027848, 1000.0);
		PolygonPoint p6 = new PolygonPoint(262.25990550668615, 1000.0);
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

	private Polygon hole3() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(245.22551393964045, 0.0);
		PolygonPoint p2 = new PolygonPoint(802.2744579508885, 0.0);
		PolygonPoint p3 = new PolygonPoint(828.2006326705192, 150.89951487587393);
		PolygonPoint p4 = new PolygonPoint(571.2499578357936+ EPSILON, 376.5914529912652- EPSILON);
		PolygonPoint p5 = new PolygonPoint(218.81850471369796, 144.25415267198147);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		points.add(p5);

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

	private Polygon hole13() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(571.2499578357936, 376.5914529912652);
		PolygonPoint p2 = new PolygonPoint(428.7500421642064, 376.5914529912652);
		PolygonPoint p3 = new PolygonPoint(357.50008432841275, 500.0);
		PolygonPoint p4 = new PolygonPoint(428.7500421642064, 623.4085470087348);
		PolygonPoint p5 = new PolygonPoint(571.2499578357936, 623.4085470087348);
		PolygonPoint p6 = new PolygonPoint(642.4999156715872, 500.0);

		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		points.add(p5);
		points.add(p6);

		// maybe add an epsilon
		PolygonPoint p7 = new PolygonPoint(571.2499578357936, 376.5914529912652);
		PolygonPoint p8 = new PolygonPoint(828.2006326705192, 150.89951487587393);
		PolygonPoint p9 = new PolygonPoint(802.2744579508885, 0.0);
		PolygonPoint p10 = new PolygonPoint(245.22551393964045, 0.0);
		PolygonPoint p11 = new PolygonPoint(218.81850471369796, 144.25415267198147);
		points.add(p7);
		points.add(p8);
		points.add(p9);
		points.add(p10);
		points.add(p11);

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

	private Polygon hole4() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(222.1335485651847, 711.9618198819196);
		PolygonPoint p2 = new PolygonPoint(130.67652299544616, 418.8322553194972);
		PolygonPoint p3 = new PolygonPoint(0.0, 431.3653755917636);
		PolygonPoint p4 = new PolygonPoint(0.0, 758.5685662433226);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);

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

	private Polygon hole5() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(804.0154547910238, 725.8958056231706);
		PolygonPoint p2 = new PolygonPoint(651.085267205592, 512.0074818305749);
		PolygonPoint p3 = new PolygonPoint(874.7995664886258, 381.2098933497282);
		PolygonPoint p4 = new PolygonPoint(936.5031243837082, 686.1733036172373);
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);

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

	private Polygon hole6() {
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(0.0+ EPSILON, 0.0+ EPSILON);
		PolygonPoint p2 = new PolygonPoint(218.81850471369796, 144.25415267198147);
		// PolygonPoint p3 = new PolygonPoint(218.81850471369796, 144.25415267198147);
		PolygonPoint p4 = new PolygonPoint(0.0+ EPSILON, 424.54094972544385);
		points.add(p1);
		points.add(p2);
		// points.add(p3);
		points.add(p4);

		Polygon polygon = new Polygon(points);
		if (logger.isDebugEnabled()) {
			logger.debug(GeoOperator.polygonToString(polygon));
		}

		if (logger.isDebugEnabled() && PaintShapes.painting) {
			// PaintShapes.paint. = PaintShapes.paint.blueTranslucence;
			PaintShapes.paint.addPolygon(polygon);
			PaintShapes.paint.myRepaint();
		}

		return polygon;
	}

	private Polygon hole136() {
		double epsilon = 0.0001;
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(571.2499578357936, 376.5914529912652);
		PolygonPoint p2 = new PolygonPoint(428.7500421642064, 376.5914529912652);
		PolygonPoint p3 = new PolygonPoint(357.50008432841275, 500.0);
		PolygonPoint p4 = new PolygonPoint(428.7500421642064, 623.4085470087348);
		PolygonPoint p5 = new PolygonPoint(571.2499578357936, 623.4085470087348);
		PolygonPoint p6 = new PolygonPoint(642.4999156715872, 500.0);

		// maybe add an epsilon
		PolygonPoint p7 = new PolygonPoint(571.2499578357936 + EPSILON, 376.5914529912652 + EPSILON);
		PolygonPoint p8 = new PolygonPoint(828.2006326705192, 150.89951487587393);
		PolygonPoint p9 = new PolygonPoint(802.2744579508885, 0.0 + EPSILON);
		PolygonPoint p10 = new PolygonPoint(245.22551393964045, 0.0 + EPSILON);
		PolygonPoint p11 = new PolygonPoint(218.81850471369796, 144.25415267198147);

		//
		PolygonPoint p12 = new PolygonPoint(0.0 + EPSILON, 0.0 + EPSILON);
		PolygonPoint p13 = new PolygonPoint(0.0 + EPSILON, 424.54094972544385);
		PolygonPoint p14 = new PolygonPoint(218.81850471369796 + EPSILON, 144.25415267198147 + EPSILON);

		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		points.add(p5);
		points.add(p6);
		points.add(p7);
		points.add(p8);
		points.add(p9);
		points.add(p10);
		points.add(p11);
		points.add(p12);
		points.add(p13);
		points.add(p14);

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
