package mo.umac.kallmann.cdt;

public class Vector2d {
	public double x;
	public double y;

	public Vector2d() {
		x = 0;
		y = 0;
	}

	public Vector2d(double a, double b) {
		x = a;
		y = b;
	}

	public Vector2d(Vector2d v) {
		this.x = v.x;
		this.y = v.y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double norm() {
		return Math.sqrt(x * x + y * y);
	}

	public double normalize() {
		double len = norm();
		if (len == 0) {
			System.out.println("Vector2d::normalize: Division by 0");
		} else {
			x /= len;
			y /= len;
		}
		return len;
	}

	public static boolean equals(Vector2d v1, Vector2d v2, double epsilon) {
		return (minus(v1, v2).norm() <= epsilon);
	}
	
	/**
	 * ==
	 * 
	 * @param v
	 * @param epsilon
	 * @return
	 */
	public boolean equals(Vector2d v, double epsilon) {
		return (minus(v).norm() <= epsilon);
	}
	
	public static Vector2d add(Vector2d v1, Vector2d v2) {
		return new Vector2d(v1.x + v2.x, v1.y + v2.y);
	}
	
	public Vector2d add(Vector2d v) {
		return new Vector2d(x + v.x, y + v.y);
	}

	public static Vector2d minus(Vector2d v1, Vector2d v2) {
		return new Vector2d(v1.x - v2.x, v1.y - v2.y);
	}

	/**
	 * -
	 * 
	 * @param v
	 * @return
	 */
	public Vector2d minus(Vector2d v) {
		return new Vector2d(x - v.x, y - v.y);
	}
	
	/**
	 * Returns TRUE if point is on the line (actually, on the EPS-slab around the line).
	 * 
	 * @param line
	 * @param epsilon
	 * @return
	 */
	public static boolean onLine(Vector2d v, Line line, double epsilon) {
		double tmp = line.eval(v);
		return (Math.abs(tmp) <= epsilon);
	}
	
	public boolean onLine(Line line, double epsilon) {
		double tmp = line.eval(this);
		return (Math.abs(tmp) <= epsilon);
	}

	
	/**
	 * dot product
	 * 
	 * @param v
	 * @return
	 */
	public static double dotProduct(Vector2d v1, Vector2d v2) {
		return v1.x * v2.x + v1.y * v2.y;

	}
	
	public double dotProduct(Vector2d v) {
		return x * v.x + y * v.y;

	}

	public static Vector2d multiply(Vector2d v, double c) {
		return new Vector2d(c * v.x, c * v.y);
	}
	

	public Vector2d multiply(double c) {
		return new Vector2d(c * x, c * y);
	}

	public static Vector2d divide(Vector2d v, double c) {
		return new Vector2d(v.x / c, v.y / c);
	}
	
	public Vector2d divide(double c) {
		return new Vector2d(x / c, y / c);
	}

	/**
	 * Returns TRUE if point is to the left of the line (left to the EPS-slab around the line).
	 * 
	 * @param line
	 * @return
	 */
	public static boolean smallerThan(Vector2d v, Line line, double epsilon) {
		return (line.eval(v) < -epsilon);

	}
	
	/**
	 * Returns TRUE if point is to the left of the line (left to the EPS-slab around the line).
	 * 
	 * @param line
	 * @return
	 */
	public boolean smallerThan(Line line, double epsilon) {
		return (line.eval(this) < -epsilon);

	}

	public String toString(){
		return "(" + + x + ", " + y + ")"; 
	}


}
