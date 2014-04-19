package mo.umac.triangulation;

public class PolygonPoint {

	private double _x;
	private double _y;
	private double _z;

	protected PolygonPoint _next;
	protected PolygonPoint _previous;

	public PolygonPoint(double x, double y) {
		_x = x;
		_y = y;
	}

	public PolygonPoint(double x, double y, double z) {
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

	public void setPrevious(PolygonPoint p) {
		_previous = p;
	}

	public void setNext(PolygonPoint p) {
		_next = p;
	}

	public PolygonPoint getNext() {
		return _next;
	}

	public PolygonPoint getPrevious() {
		return _previous;
	}
}
