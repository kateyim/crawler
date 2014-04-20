package mo.umac.triangulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.log4j.Logger;

public class Polygon {
	protected static Logger logger = Logger.getLogger(Polygon.class.getName());

	protected ArrayList<Point> _points = new ArrayList<Point>();
	protected ArrayList<Polygon> _holes;

	protected List<DelaunayTriangle> m_triangles;

	protected Point _last;

	/**
	 * To create a polygon we need atleast 3 separate points
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public Polygon(Point p1, Point p2, Point p3) {
		p1._next = p2;
		p2._next = p3;
		p3._next = p1;
		p1._previous = p3;
		p2._previous = p1;
		p3._previous = p2;
		_points.add(p1);
		_points.add(p2);
		_points.add(p3);
	}

	/**
	 * Requires atleast 3 points
	 * 
	 * @param points
	 *            - ordered list of points forming the polygon.
	 *            No duplicates are allowed
	 */
	public Polygon(List<Point> points) {
		// Lets do one sanity check that first and last point hasn't got same
		// position
		// Its something that often happen when importing polygon data from
		// other formats
		if (points.get(0).equals(points.get(points.size() - 1))) {
			logger.warn("Removed duplicate point");
			points.remove(points.size() - 1);
		}
		_points.addAll(points);
	}

	/**
	 * Requires atleast 3 points
	 * 
	 * @param points
	 */
	public Polygon(Point[] points) {
		this(Arrays.asList(points));
	}

	/**
	 * Assumes: that given polygon is fully inside the current polygon
	 * 
	 * @param poly
	 *            - a subtraction polygon
	 */
	public void addHole(Polygon poly) {
		if (_holes == null) {
			_holes = new ArrayList<Polygon>();
		}
		_holes.add(poly);
		// XXX: tests could be made here to be sure it is fully inside
		// addSubtraction( poly.getPoints() );
	}

	/**
	 * Will insert a point in the polygon after given point
	 * 
	 * @param a
	 * @param b
	 * @param p
	 */
	public void insertPointAfter(Point a, Point newPoint) {
		// Validate that
		int index = _points.indexOf(a);
		if (index != -1) {
			newPoint.setNext(a.getNext());
			newPoint.setPrevious(a);
			a.getNext().setPrevious(newPoint);
			a.setNext(newPoint);
			_points.add(index + 1, newPoint);
		} else {
			throw new RuntimeException("Tried to insert a point into a Polygon after a point not belonging to the Polygon");
		}
	}

	public void addPoints(List<Point> list) {
		Point first;
		for (Point p : list) {
			p.setPrevious(_last);
			if (_last != null) {
				p.setNext(_last.getNext());
				_last.setNext(p);
			}
			_last = p;
			_points.add(p);
		}
		first = (Point) _points.get(0);
		_last.setNext(first);
		first.setPrevious(_last);
	}

	/**
	 * Will add a point after the last point added
	 * 
	 * @param p
	 */
	public void addPoint(Point p) {
		p.setPrevious(_last);
		p.setNext(_last.getNext());
		_last.setNext(p);
		_points.add(p);
	}

	public void removePoint(Point p) {
		Point next, prev;

		next = p.getNext();
		prev = p.getPrevious();
		prev.setNext(next);
		next.setPrevious(prev);
		_points.remove(p);
	}

	public Point getPoint() {
		return _last;
	}

	public List<Point> getPoints() {
		return _points;
	}

	public List<DelaunayTriangle> getTriangles() {
		return m_triangles;
	}

	public void addTriangle(DelaunayTriangle t) {
		m_triangles.add(t);
	}

	public void addTriangles(List<DelaunayTriangle> list) {
		m_triangles.addAll(list);
	}

	public void clearTriangulation() {
		if (m_triangles != null) {
			m_triangles.clear();
		}
	}

}
