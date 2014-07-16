package mo.umac.kallmann.cdt;

public class Line {

	private double a;
	private double b;
	private double c;

	public Line() {

	}

	public Line(Vector2d p, Vector2d q) {
		Vector2d t = Vector2d.minus(q, p);
		double len = t.norm();

		a = t.y / len;
		b = -t.x / len;
		// c = -(a*p[X] + b*p[Y]);

		// less efficient, but more robust -- seth.
		c = -0.5 * ((a * p.x + b * p.y) + (a * q.x + b * q.y));
	}

	/**
	 * Compute the distance from the point p to the line
	 * 
	 * @param vector2d
	 * @return
	 */
	public static double eval(Line l, Vector2d v) {
		return (l.a * v.x + l.b * v.y + l.c);
	}
	
	/**
	 * Plugs point p into the line equation.
	 * 
	 * @param v
	 * @return
	 */
	public double eval(Vector2d v) {
		return (a * v.x + b * v.y + c);
	}

	/**
	 * Returns the intersection of the line with the segment (p1,p2)
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public Vector2d intersect(Vector2d p1, Vector2d p2) {
		// assumes that segment (p1,p2) crosses the line
		Vector2d d = Vector2d.minus(p2, p1);
		double t = -eval(p1) / (a * d.x + b * d.y);
		return Vector2d.add(p1, Vector2d.multiply(d, t));
	}

}
