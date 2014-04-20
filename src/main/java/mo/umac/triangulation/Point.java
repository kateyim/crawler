package mo.umac.triangulation;

import java.util.ArrayList;

public class Point {

	private double _x;
	private double _y;
	private double _z;

	protected Point _next;
	protected Point _previous;

	// change to edges
	private ArrayList<DTSweepConstraint> edges;

	@Override
	public String toString() {
		return "[" + getX() + "," + getY() + "]";
	}

	public ArrayList<DTSweepConstraint> getEdges() {
		return edges;
	}

	public void addEdge(DTSweepConstraint e) {
		if (edges == null) {
			edges = new ArrayList<DTSweepConstraint>();
		}
		edges.add(e);
	}

	/**
	 * @param p
	 *            - edge destination point
	 * @return the edge from this point to given point
	 */
	public DTSweepConstraint getEdge(Point p) {
		for (DTSweepConstraint c : edges) {
			if (c.p == p) {
				return c;
			}
		}
		return null;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Point) {
			Point p = (Point) obj;
			return getX() == p.getX() && getY() == p.getY();
		}
		return super.equals(obj);
	}

	public int hashCode() {
		long bits = java.lang.Double.doubleToLongBits(getX());
		bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	public boolean hasEdges() {
		return edges != null;
	}

	public Point(double x, double y) {
		_x = x;
		_y = y;
	}

	public Point(double x, double y, double z) {
		_x = x;
		_y = y;
		_z = z;
	}

	public double getX() {
		return _x;
	}

	public double getY() {
		return _y;
	}

	public double getZ() {
		return _z;
	}

	public float getXf() {
		return (float) _x;
	}

	public float getYf() {
		return (float) _y;
	}

	public float getZf() {
		return (float) _z;
	}

	public void setPrevious(Point p) {
		_previous = p;
	}

	public void setNext(Point p) {
		_next = p;
	}

	public Point getNext() {
		return _next;
	}

	public Point getPrevious() {
		return _previous;
	}
}
