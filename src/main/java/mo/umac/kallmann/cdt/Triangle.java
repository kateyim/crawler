package mo.umac.kallmann.cdt;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.poly2tri.triangulation.point.TPoint;

public class Triangle {
	protected static Logger logger = Logger.getLogger(Triangle.class.getName());

	/** Neighbor pointers */
	public final Triangle[] neighbors = new Triangle[3];

	public final Vector2d[] points = new Vector2d[3];

	// public final QuadEdge[] edges = new QuadEdge[3];

	public Triangle() {

	}

	public Triangle(Vector2d p1, Vector2d p2, Vector2d p3) {
		points[0] = p1;
		points[1] = p2;
		points[2] = p3;
	}

	/**
	 * For testing
	 * 
	 * @param args
	 */
	// public static void main(String[] args) {
	// DOMConfigurator.configure(Main.LOG_PROPERTY_PATH);
	//
	// Triangle t = new Triangle(new Vector2d(1.0, 2.0), new Vector2d(1.0, 1.5), new Vector2d(0.7, 2.0));
	// Triangle after = t.sortTriangle();
	// Triangle b = new Triangle(new Vector2d(1.0, 2.0), new Vector2d(1.0, 1.5), new Vector2d(0.7, 2.0));
	// boolean flag = t.equals(b);
	// logger.debug(flag);
	// // logger.debug(after.toString());
	// }

	public Triangle sortTriangle() {
		Vector2d[] after = sortVectors(points);
		return new Triangle(after[0], after[1], after[2]);
	}

	/**
	 * Sort points according to x-axis and y-axis from small to large
	 * 
	 * @param vs
	 * @return
	 */
	private Vector2d[] sortVectors(Vector2d[] vs) {
		Vector2d[] newVectors = new Vector2d[3];
		// TODO == is not suitable for double
		if ((vs[0].x < vs[1].x && vs[1].x < vs[2].x) || (vs[0].x == vs[1].x && vs[0].y < vs[1].y && vs[1].x < vs[2].x)
				|| (vs[0].x < vs[1].x && vs[1].x == vs[2].x && vs[1].y < vs[2].y)) {
			newVectors[0] = vs[0];
			newVectors[1] = vs[1];
			newVectors[2] = vs[2];
			return newVectors;
		}
		if ((vs[0].x < vs[2].x && vs[2].x < vs[1].x) || (vs[0].x == vs[2].x && vs[0].y < vs[2].y && vs[2].x < vs[1].x)
				|| (vs[0].x < vs[2].x && vs[2].x == vs[1].x && vs[2].y < vs[1].y)) {
			newVectors[0] = vs[0];
			newVectors[1] = vs[2];
			newVectors[2] = vs[1];
			return newVectors;
		}
		if ((vs[1].x < vs[0].x && vs[0].x < vs[2].x) || (vs[1].x == vs[0].x && vs[1].y < vs[0].y && vs[0].x < vs[2].x)
				|| (vs[1].x < vs[0].x && vs[0].x == vs[2].x && vs[0].y < vs[2].y)) {
			newVectors[0] = vs[1];
			newVectors[1] = vs[0];
			newVectors[2] = vs[2];
			return newVectors;
		}
		if ((vs[1].x < vs[2].x && vs[2].x < vs[0].x) || (vs[1].x == vs[2].x && vs[1].y < vs[2].y && vs[2].x < vs[0].x)
				|| (vs[1].x < vs[2].x && vs[2].x == vs[0].x && vs[2].y < vs[0].y)) {
			newVectors[0] = vs[1];
			newVectors[1] = vs[2];
			newVectors[2] = vs[0];
			return newVectors;
		}
		if ((vs[2].x < vs[0].x && vs[0].x < vs[1].x) || (vs[2].x == vs[0].x && vs[2].y < vs[0].y && vs[0].x < vs[1].x)
				|| (vs[2].x < vs[0].x && vs[0].x == vs[1].x && vs[0].y < vs[1].y)) {
			newVectors[0] = vs[2];
			newVectors[1] = vs[0];
			newVectors[2] = vs[1];
			return newVectors;
		}
		if ((vs[2].x < vs[1].x && vs[1].x < vs[0].x) || (vs[2].x == vs[1].x && vs[2].y < vs[1].y && vs[1].x < vs[0].x)
				|| (vs[2].x < vs[1].x && vs[1].x == vs[0].x && vs[1].y < vs[0].y)) {
			newVectors[0] = vs[2];
			newVectors[1] = vs[1];
			newVectors[2] = vs[0];
			return newVectors;
		}
		return null;
	}

	/**
	 * Returns the center of the circle through points a, b, c. From Graphics Gems I, p.22
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public Vector2d circumCenter(Vector2d a, Vector2d b, Vector2d c) {
		double d1, d2, d3, c1, c2, c3;

		d1 = (b.minus(a)).dotProduct(c.minus(a));
		d2 = (b.minus(c)).dotProduct(a.minus(c));
		d3 = (a.minus(b)).dotProduct(c.minus(b));
		c1 = d2 * d3;
		c2 = d3 * d1;
		c3 = d1 * d2;
		// return ((c2 + c3)*a + (c3 + c1)*c + (c1 + c2)*b) / (2*(c1 + c2 + c3));
		return (a.multiply(c2 + c3).add(c.multiply(c3 + c1)).add(b.multiply(c1 + c2))).divide(2 * (c1 + c2 + c3));
	}
	
	public Vector2d circumCenter() {
		Vector2d a = points[0];
		Vector2d b = points[1];
		Vector2d c = points[2];
		double d1, d2, d3, c1, c2, c3;

		d1 = (b.minus(a)).dotProduct(c.minus(a));
		d2 = (b.minus(c)).dotProduct(a.minus(c));
		d3 = (a.minus(b)).dotProduct(c.minus(b));
		c1 = d2 * d3;
		c2 = d3 * d1;
		c3 = d1 * d2;
		// return ((c2 + c3)*a + (c3 + c1)*c + (c1 + c2)*b) / (2*(c1 + c2 + c3));
		return (a.multiply(c2 + c3).add(c.multiply(c3 + c1)).add(b.multiply(c1 + c2))).divide(2 * (c1 + c2 + c3));
	}

	public Vector2d centroid() {
		double cx = (points[0].getX() + points[1].getX() + points[2].getX()) / 3d;
		double cy = (points[0].getY() + points[1].getY() + points[2].getY()) / 3d;
		return new Vector2d(cx, cy);
	}

	public double area() {
		double a = (points[0].getX() - points[2].getX()) * (points[1].getY() - points[0].getY());
		double b = (points[0].getX() - points[1].getX()) * (points[2].getY() - points[0].getY());

		return 0.5 * Math.abs(a - b);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Triangle: ");
		for (int i = 0; i < points.length; i++) {
			sb.append(points[i].toString());
			sb.append("; ");
		}
		return sb.toString();
	}

	// Depends only on account number
	@Override
	public int hashCode() {
		Vector2d[] sorted = sortVectors(points);
		double[] value = { sorted[0].x, sorted[0].y, sorted[1].x, sorted[1].y, sorted[2].x, sorted[2].y };
		return Hash.hash(value);
	}

	// Compare only account numbers
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Triangle other = (Triangle) obj;
		if (hashCode() != other.hashCode())
			return false;
		return true;
	}
}
