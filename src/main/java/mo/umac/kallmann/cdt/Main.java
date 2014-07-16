package mo.umac.kallmann.cdt;

import java.util.ArrayList;

import org.apache.log4j.xml.DOMConfigurator;

import paint.PaintShapes;
import paint.WindowUtilities;

public class Main {

	public static String LOG_PROPERTY_PATH = "./log4j.xml";

	public static void main(String[] args) {
		DOMConfigurator.configure(LOG_PROPERTY_PATH);
		PaintShapes.painting = true;
		if (PaintShapes.painting) {
			WindowUtilities.openInJFrame(PaintShapes.paint, 1000, 1000);
		}

		Main main = new Main();
		// main.testMesh();
		main.testSimpleCase();

	}

	public void testSimpleCase() {
		// [0, 1000] * [0, 1000]
		Vector2d a = new Vector2d(0, 0);
		Vector2d b = new Vector2d(1000, 0);
		Vector2d c = new Vector2d(1000, 1000);
		Vector2d d = new Vector2d(0, 1000);
		//
		Mesh mesh = new Mesh(a, b, c, d);
		// constrains
		ArrayList<Vector2d> pointList = new ArrayList<Vector2d>();

		Vector2d aa = new Vector2d(200, 10);
		Vector2d bb = new Vector2d(300, 100);
		Vector2d cc = new Vector2d(250, 400);
		Vector2d dd = new Vector2d(150, 300);
		pointList.add(aa);
		pointList.add(bb);
		pointList.add(cc);
		pointList.add(dd);

		// draw
		if (PaintShapes.painting) {
			PaintShapes.paint.addLine(a, b);
			PaintShapes.paint.addLine(b, c);
			PaintShapes.paint.addLine(c, d);
			PaintShapes.paint.addLine(d, a);
			// PaintShapes.paint.addLine(aa, bb);
			// PaintShapes.paint.addLine(bb, cc);
			// PaintShapes.paint.addLine(cc, dd);
			// PaintShapes.paint.addLine(dd, aa);
			PaintShapes.paint.myRepaint();
		}

		Constraints constraint = new Constraints(pointList);
		mesh.insertConstraint(constraint);

	}

	/*********************** For Testing *******************************/

	private Vector2d v1 = new Vector2d(-2, -2);
	private Vector2d v2 = new Vector2d(2, -2);
	private Vector2d v3 = new Vector2d(2, 2);
	private Vector2d v4 = new Vector2d(-2, 2);

	private Vector2d v5 = new Vector2d(0, 3.464);

	public void testMesh() {
		Mesh mesh = new Mesh(v1, v2, v3, v4);
	}

	public Constraints constructConstrains() {
		ArrayList<Vector2d> pointList = new ArrayList<Vector2d>();
		Constraints c = new Constraints(pointList);
		return c;
	}

	public void testTriArea() {
		Vector2d a = new Vector2d(0, 0);
		Vector2d b = new Vector2d(1, 0);
		Vector2d c = new Vector2d(0.5, 1);
		Mesh mesh = new Mesh();
		double triArea = mesh.triArea(a, b, c);
		System.out.println(triArea);
	}

	// public void testInCircle() {
	// Vector2d a = new Vector2d(0, 0);
	// Vector2d b = new Vector2d(1, 0);
	// Vector2d c = new Vector2d(0.5, 1);
	// // Vector2d d = new Vector2d(0.7, 1);
	// Vector2d d = new Vector2d(0.7, 0.2);
	// Mesh mesh = new Mesh();
	// boolean bb = mesh.inCircle(a, b, c, d);
	// System.out.println(bb);
	// }

}
