package mo.umac.crawler;

import java.util.ArrayList;
import java.util.List;

import mo.umac.spatial.Circle;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;

import paint.PaintShapes;
import paint.WindowUtilities;

import com.vividsolutions.jts.geom.Coordinate;

public class AlgoDCDTTest {

	protected static Logger logger = Logger.getLogger(AlgoDCDTTest.class.getName());

	public static void main(String[] args) {
		boolean debug = true;
		PaintShapes.painting = true;
		MainYahoo.shutdownLogs(debug);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
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
//		TPoint p1 = new TPoint(10, 40);
//		TPoint p2 = new TPoint(50, 130);
//		TPoint p3 = new TPoint(90, 40);

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

	public static Coordinate trans(TPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

	public static Coordinate trans(TriangulationPoint p) {
		Coordinate c = new Coordinate(p.getX(), p.getY());
		return c;
	}

}
