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

import mo.umac.spatial.Circle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

public class PaintShapes extends JPanel {

	public static boolean painting = false;

	public static List<Shape> arrDraw = new ArrayList<Shape>();
	public static List<Shape> arrFill = new ArrayList<Shape>();

	public static PaintShapes paint = new PaintShapes();

	public Color redTranslucence = new Color(255, 0, 0, 50);
	public Color greenTranslucence = new Color(0, 255, 0, 50);
	public Color blueTranslucence = new Color(0, 0, 255, 150);
	public Color blackTranslucence = Color.BLACK;

	public static Color color;

	public void paintComponent(Graphics g) {
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
		arrDraw.clear();
		arrFill.clear();

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

	public static void addConstraintPolygon(ArrayList<Coordinate> points) {
		int npoints = points.size();
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < points.size(); i++) {
			xpoints[i] = (int) points.get(i).x;
			ypoints[i] = (int) points.get(i).y;
		}
		Polygon polygon = new Polygon(xpoints, ypoints, npoints);
		arrFill.add(polygon);
	}

	public static void addPolygon(ArrayList<Coordinate> points) {
		int npoints = points.size();
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < points.size(); i++) {
			xpoints[i] = (int) points.get(i).x;
			ypoints[i] = (int) points.get(i).y;
		}
		Polygon polygon = new Polygon(xpoints, ypoints, npoints);
		arrDraw.add(polygon);
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
