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
import utils.GeoOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * @author kate
 */
public class AlgoDCDTTest extends AlgoDCDT {

	protected static Logger logger = Logger.getLogger(AlgoDCDTTest.class.getName());

	public static void main(String[] args) {
		boolean debug = true;
		PaintShapes.painting = true;
		MainYahoo.shutdownLogs(debug);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);

		// testEightCases();
		// testTriangulation184();
		// testTriangulation53();
		// testTriangulation104();
		testShrink();
		// AlgoDCDTTest test = new AlgoDCDTTest();
		// test.testIntersect();
	}

	public void testIntersect() {
		Circle aCircle = new Circle(new Coordinate(415.0752119518988, 4.875326097704483E-5), 436.97110832295834);
		DelaunayTriangle triangle = new DelaunayTriangle(new TPoint(1000.0, 0.0), new TPoint(245.22563585569634, 1.4625978293113449E-4), new TPoint(0.0, 0.0));
		Polygon inner = intersect(aCircle, triangle);
		System.out.println(GeoOperator.polygonToString(inner));

	}

	public static void testShrink() {
		AlgoDCDT algo = new AlgoDCDT();
		TriangulationPoint p1 = new TPoint(0.0, 0.0);
		TriangulationPoint p2 = new TPoint(1000.0, 0.0);
		TriangulationPoint point = new TPoint(852.0463202748545, 0.0);
		int j = 2;
		//
		// polygon: Polygon: [245.22563585569634, 1.4625978293113449E-4]; [1.222368850126051E-4, 3.6452837008118994E-11]; [852.0463202748545, 0.0];
		// [852.0463202748568, 2.867038695066859E-5];
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		PolygonPoint pp1 = new PolygonPoint(245.22563585569634, 1.4625978293113449E-4);
		PolygonPoint pp2 = new PolygonPoint(1.222368850126051E-4, 3.6452837008118994E-11);
		PolygonPoint pp3 = new PolygonPoint(852.0463202748545, 0.0);
		PolygonPoint pp4 = new PolygonPoint(852.0463202748568, 2.867038695066859E-5);
		points.add(pp1);
		points.add(pp2);
		points.add(pp3);
		points.add(pp4);
		Polygon polygon = new Polygon(points);

		//
		algo.shrink(p1, p2, polygon, point, j);
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

	public static void testTriangulation53() {
		AlgoDCDT dcdt = new AlgoDCDT();
		Envelope envelope = new Envelope(0, 1000, 0, 1000);
		Polygon polygon = dcdt.boundary(envelope);
		ArrayList<Polygon> holeList = new ArrayList<Polygon>();
		//
		List<PolygonPoint> points2 = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(473.9764980004496, 907.3133020915193);
		PolygonPoint p2 = new PolygonPoint(542.7123548771788, 907.3133020915193);
		PolygonPoint p3 = new PolygonPoint(577.0802833155433, 847.7863038853806);
		PolygonPoint p4 = new PolygonPoint(542.7123548771788, 788.2593056792418);
		PolygonPoint p5 = new PolygonPoint(473.9764980004496, 788.2593056792418);
		PolygonPoint p6 = new PolygonPoint(439.6085695620851, 847.7863038853806);
		points2.add(p1);
		points2.add(p2);
		points2.add(p3);
		points2.add(p4);
		points2.add(p5);
		points2.add(p6);
		Polygon polygon2 = new Polygon(points2);
		//
		List<PolygonPoint> points1 = new ArrayList<PolygonPoint>();
		PolygonPoint p11 = new PolygonPoint(663.5680739795832, 930.1993595301177);
		PolygonPoint p12 = new PolygonPoint(550.0762334028996, 908.7077759994335);
		PolygonPoint p13 = new PolygonPoint(549.2125921259866, 896.0545609153323);
		// before:
		// PolygonPoint p14 = new PolygonPoint(577.0802833155433, 847.7863038853806);
		// after:
		PolygonPoint p14 = new PolygonPoint(577.3046963701703, 848.7607982039443);
		//
		PolygonPoint p15 = new PolygonPoint(671.0988796509514, 911.3538247593535);
		points1.add(p11);
		points1.add(p12);
		points1.add(p13);
		points1.add(p14);
		points1.add(p15);
		Polygon inner = new Polygon(points1);
		//
		holeList.add(polygon2);
		dcdt.disturb(polygon, holeList, inner);
		holeList.add(inner);
		dcdt.addHoles(polygon, holeList);
		Poly2Tri.triangulate(polygon);

	}

	/**
	 * 150.44001290181097, 202.13688451867293, 171.16421007401388, 253.5935661601067, 75.92640500959905, 257.6599268414036, 75.8100313513383, 257.3263045750901
	 * </p>
	 * 177.43153552861548, 248.8875730819433, 231.41458078222445, 248.8875730819433, 258.40610340902896, 202.13688451867293, 231.41458078222445,
	 * 155.38619595540257, 177.43153552861548, 155.38619595540257, 150.44001290181097, 202.13688451867293
	 * </p>
	 * 208.94644387641603, 372.13666670393786, 105.3811680097843, 328.2088154281505, 105.47620280969268, 309.39599156220834, 171.86647225884107,
	 * 253.56732598520665, 179.99175660710608, 254.2648583309549, 226.77384572209147, 352.5222470500054
	 * </p>
	 * 105.64722800113273, 308.41061399616524, 75.32411574825697, 257.68564273615186, 171.16421007401388, 253.5935661601067, 170.35601614683887,
	 * 253.76346654127897
	 */
	public static void testTriangulation104() {
		AlgoDCDT dcdt = new AlgoDCDT();
		Envelope envelope = new Envelope(0, 1000, 0, 1000);
		Polygon polygon = dcdt.boundary(envelope);
		ArrayList<Polygon> holeList = new ArrayList<Polygon>();
		//
		List<PolygonPoint> points2 = new ArrayList<PolygonPoint>();
		PolygonPoint p1 = new PolygonPoint(177.43153552861548, 248.8875730819433);
		PolygonPoint p2 = new PolygonPoint(231.41458078222445, 248.8875730819433);
		PolygonPoint p3 = new PolygonPoint(258.40610340902896, 202.13688451867293);
		PolygonPoint p4 = new PolygonPoint(231.41458078222445, 155.38619595540257);
		PolygonPoint p5 = new PolygonPoint(177.43153552861548, 155.38619595540257);
		PolygonPoint p6 = new PolygonPoint(150.44001290181097, 202.13688451867293);
		points2.add(p1);
		points2.add(p2);
		points2.add(p3);
		points2.add(p4);
		points2.add(p5);
		points2.add(p6);
		Polygon polygon2 = new Polygon(points2);
		//
		List<PolygonPoint> points3 = new ArrayList<PolygonPoint>();
		PolygonPoint p31 = new PolygonPoint(208.94644387641603, 372.13666670393786);
		PolygonPoint p32 = new PolygonPoint(105.3811680097843, 328.2088154281505);
		PolygonPoint p33 = new PolygonPoint(105.47620280969268, 309.39599156220834);
		PolygonPoint p34 = new PolygonPoint(171.86647225884107, 253.56732598520665);
		PolygonPoint p35 = new PolygonPoint(179.99175660710608, 254.2648583309549);
		PolygonPoint p36 = new PolygonPoint(226.77384572209147, 352.5222470500054);
		points3.add(p31);
		points3.add(p32);
		points3.add(p33);
		points3.add(p34);
		points3.add(p35);
		points3.add(p36);
		Polygon polygon3 = new Polygon(points3);
		//
		List<PolygonPoint> points4 = new ArrayList<PolygonPoint>();
		PolygonPoint p41 = new PolygonPoint(105.64722800113273, 308.41061399616524);
		PolygonPoint p42 = new PolygonPoint(75.32411574825697, 257.68564273615186);
		PolygonPoint p43 = new PolygonPoint(171.16421007401388, 253.5935661601067);
		PolygonPoint p44 = new PolygonPoint(170.35601614683887, 253.76346654127897);
		points4.add(p41);
		points4.add(p42);
		points4.add(p43);
		points4.add(p44);
		Polygon polygon4 = new Polygon(points4);
		//
		List<PolygonPoint> points1 = new ArrayList<PolygonPoint>();
		PolygonPoint p11 = new PolygonPoint(150.44001290181097, 202.13688451867293);
		PolygonPoint p12 = new PolygonPoint(171.16421007401388, 253.5935661601067);
		PolygonPoint p13 = new PolygonPoint(75.92640500959905, 257.6599268414036);
		PolygonPoint p14 = new PolygonPoint(75.8100313513383, 257.3263045750901);
		points1.add(p11);
		points1.add(p12);
		points1.add(p13);
		points1.add(p14);
		Polygon inner = new Polygon(points1);
		//
		holeList.add(polygon2);
		holeList.add(polygon3);
		holeList.add(polygon4);
		dcdt.disturb(polygon, holeList, inner);
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
