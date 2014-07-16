package paint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import mo.umac.kallmann.cdt.Triangle;
import mo.umac.kallmann.cdt.Vector2d;
import mo.umac.spatial.Circle;

import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

public class PaintShapes extends JPanel {

	public static boolean painting = false;

	public static List<Shape> arrDraw = new ArrayList<Shape>();
	public static List<Shape> arrFill = new ArrayList<Shape>();
	// for polygon
	public static List<Shape> arrFillConstraintPoly = new ArrayList<Shape>();
	public static List<Shape> arrDrawTempTriangles = new ArrayList<Shape>();

	public static PaintShapes paint = new PaintShapes();

	public Color redTranslucence = new Color(255, 0, 0, 50);
	public Color greenTranslucence = new Color(0, 255, 0, 50);
	public Color blueTranslucence = new Color(0, 0, 255, 150);
	public Color blackTranslucence = Color.BLACK;

	public static Color color;

	public void paintComponent(Graphics g) {
		// XXX clear() only used testing AlgoDCDT
		// clear(g);
		g.setColor(color);
		for (Shape i : arrDraw) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.draw(i);
		}
		for (Shape i : arrFill) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.fill(i);
		}
		// PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
		for (Shape i : arrDrawTempTriangles) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.draw(i);
		}
		// PaintShapes.paint.color = PaintShapes.paint.blueTranslucence;
		for (Shape i : arrFillConstraintPoly) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.fill(i);
		}
		arrDraw.clear();
		arrFill.clear();
		arrDrawTempTriangles.clear();
		//
		arrFillConstraintPoly.clear();
	}

	/**
	 * super.paintComponent clears offscreen pixmap, since we're using double
	 * buffering by default.
	 * 
	 * @param g
	 */
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}

	public static void myRepaint() {
		paint.repaint();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void addCircle(Circle circle) {
		arrFill.add(getCircle(circle));
	}

	public static void addRectangle(Envelope envelope) {
		arrFill.add(getRectangle(envelope));
	}

	public static void addPoint(Coordinate p) {
		arrFill.add(getPoint(p));
	}

	public static void addLine(LineSegment line) {
		arrDraw.add(getLine(line));
	}

	
	public static void addLine(Vector2d v1, Vector2d v2) {
		arrDraw.add(getLine(v1, v2));
	}
	
	public static Line2D.Double getLine(Vector2d v1, Vector2d v2) {
		double x1 = v1.x;
		double y1 = v1.y;
		double x2 = v2.x;
		double y2 = v2.y;
		Line2D.Double shape = new Line2D.Double(x1, y1, x2, y2);
		return shape;
	}

	// public static void addConstraintPolygon(ArrayList<Coordinate> points) {
	// int npoints = points.size();
	// int[] xpoints = new int[npoints];
	// int[] ypoints = new int[npoints];
	// for (int i = 0; i < points.size(); i++) {
	// xpoints[i] = (int) points.get(i).x;
	// ypoints[i] = (int) points.get(i).y;
	// }
	// Polygon polygon = new Polygon(xpoints, ypoints, npoints);
	// arrFill.add(polygon);
	// }
	//
	// public static void addPolygon(ArrayList<Coordinate> points) {
	// int npoints = points.size();
	// int[] xpoints = new int[npoints];
	// int[] ypoints = new int[npoints];
	// for (int i = 0; i < points.size(); i++) {
	// xpoints[i] = (int) points.get(i).x;
	// ypoints[i] = (int) points.get(i).y;
	// }
	// Polygon polygon = new Polygon(xpoints, ypoints, npoints);
	// arrDraw.add(polygon);
	// }

	public static void addTriangle(DelaunayTriangle dt) {
		int npoints = dt.points.length;
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < dt.points.length; i++) {
			xpoints[i] = (int) dt.points[i].getX();
			ypoints[i] = (int) dt.points[i].getY();
		}
		Polygon polygon = new Polygon(xpoints, ypoints, npoints);
		arrDrawTempTriangles.add(polygon);
	}
	
	public static void addTriangle(Triangle dt) {
		int npoints = dt.points.length;
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < dt.points.length; i++) {
			xpoints[i] = (int) dt.points[i].getX();
			ypoints[i] = (int) dt.points[i].getY();
		}
		Polygon polygon = new Polygon(xpoints, ypoints, npoints);
		arrDrawTempTriangles.add(polygon);
	}

	public static void addPolygon(org.poly2tri.geometry.polygon.Polygon p) {
		List<TriangulationPoint> list = p.getPoints();
		int npoints = list.size();
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < list.size(); i++) {
			TriangulationPoint tp = list.get(i);
			xpoints[i] = (int) tp.getX();
			ypoints[i] = (int) tp.getY();
		}
		Polygon polygon = new Polygon(xpoints, ypoints, npoints);
		arrFillConstraintPoly.add(polygon);
	}
	
	public static Ellipse2D.Double getCircle(Circle circle) {
		Coordinate center = circle.getCenter();
		double radius = circle.getRadius();
		double diameter = 2 * radius;
		Ellipse2D.Double shape = new Ellipse2D.Double(center.x - radius, center.y - radius, diameter, diameter);
		return shape;
	}

	public static Rectangle2D.Double getRectangle(Envelope envelope) {
		double width = envelope.getWidth();
		double length = envelope.getHeight();
		// test
		double minX = envelope.getMinX();
		double maxX = envelope.getMaxX();
		double minY = envelope.getMinY();
		double maxY = envelope.getMaxY();
		Rectangle2D.Double shape = new Rectangle2D.Double(minX, minY, width, length);
		return shape;
	}

	public static Rectangle2D.Double getPoint(Coordinate p) {
		Rectangle2D.Double shape = new Rectangle2D.Double(p.x - 2.5, p.y - 2.5, 5, 5);
		return shape;
	}

	public static Line2D.Double getLine(LineSegment line) {
		double x1 = line.p0.x;
		double y1 = line.p0.y;
		double x2 = line.p1.x;
		double y2 = line.p1.y;
		Line2D.Double shape = new Line2D.Double(x1, y1, x2, y2);
		return shape;
	}

}
