package mo.umac.kallmann.cdt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import mo.umac.crawler.AlgoDCDT;
import mo.umac.crawler.Strategy;

import org.apache.log4j.Logger;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;

import paint.PaintShapes;
import utils.GeoOperator;

import com.vividsolutions.jts.geom.Coordinate;

public class Mesh {
	protected static Logger logger = Logger.getLogger(Mesh.class.getName());

	public static final int INIT_VALUE = -1;

	public static double epsilon = 1e-10;

	/**
	 * store all edges
	 */
	private Llist<Edge> edges = new Llist<Edge>();

	private QuadEdge startingEdge;

	private boolean bruteFalseLocate = false;

	/**
	 * Only use the key: Triangle;
	 * 
	 * Only save outside hole triangles
	 * 
	 * 2014-7-18
	 * There are bugs in the way of constructing triangleMap
	 */
	private static HashMap<Triangle, String> triangleMap = new HashMap<Triangle, String>();

	/**
	 * The hole for the triangulation process
	 */
	private Polygon hole = null;

	/**
	 * @param c
	 */
	public void insertConstraint(Constraints c) {
		ArrayList<Vector2d> vConsList = c.pointList;
		// add new
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		for (int i = 0; i < vConsList.size(); i++) {
			Vector2d p = vConsList.get(i);
			PolygonPoint pp = new PolygonPoint(p.x, p.y);
			points.add(pp);
		}
		hole = new Polygon(points);
		logger.debug("inserting edges: ");
		int size = vConsList.size();
		for (int i = 0; i < size - 1; i++) {
			Vector2d v1 = vConsList.get(i);
			Vector2d v2 = vConsList.get(i + 1);
			insertEdge(v1, v2);
		}
		if (size >= 2) {
			insertEdge(vConsList.get(size - 1), vConsList.get(0));
		}

	}

	/************* Topological Operations for Delaunay Diagrams *****************/
	public Mesh() {

	}

	/**
	 * Initialize the mesh to the Delaunay triangulation of the quadrilateral defined by the points a, b, c, d. // NOTE: quadrilateral is assumed convex.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 */
	public Mesh(Vector2d a, Vector2d b, Vector2d c, Vector2d d) {
		Vector2d da = new Vector2d(a);
		Vector2d db = new Vector2d(b);
		Vector2d dc = new Vector2d(c);
		Vector2d dd = new Vector2d(d);

		QuadEdge ea = makeEdge(da, db, true);

		QuadEdge eb = makeEdge(db, dc, true);

		QuadEdge ec = makeEdge(dc, dd, true);

		QuadEdge ed = makeEdge(dd, da, true);

		splice(ea.sym(), eb);
		splice(eb.sym(), ec);
		splice(ec.sym(), ed);
		splice(ed.sym(), ea);

		// Split into two triangles:
		if (inCircle(c, d, a, b)) {
			// Connect d to b:
			startingEdge = connect(ec, eb);
		} else {
			// Connect c to a:
			startingEdge = connect(eb, ea);
		}
	}

	/**
	 * 
	 * Add a new edge e connecting the destination of a to the origin of b, in such a way that all three have the same left face after the connection is
	 * complete. Additionally, the data pointers of the new edge are set.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private QuadEdge connect(QuadEdge a, QuadEdge b) {
		// add at 2014-7-11
		if (coincide(a.orig(), b.dest())) {
			Triangle t = new Triangle(a.dest(), b.orig(), b.dest());
			addTriangle(t);
			// other influenced triangles
			if (coincide(a.lNext().dest(), b.lPrev().orig())) {
				Triangle t2 = new Triangle(a.dest(), b.orig(), a.lNext().dest());
				addTriangle(t2);
			}
		}
		QuadEdge e = makeEdge(a.dest(), b.orig(), false);
		splice(e, a.lNext());
		splice(e.sym(), b);
		//
		return e;
	}

	/**
	 * Essentially turns edge e counterclockwise inside its enclosing quadrilateral. The data pointers are modified accordingly.
	 * 
	 * @param e
	 * 
	 */
	private void swap(QuadEdge e) {
		logger.debug("swap: " + e.toString());
		// add at 2014-7-11 remove a triangle and add a new triangle
		Triangle old1 = new Triangle(e.orig(), e.dest(), e.oPrev().dest());
		Triangle old2 = new Triangle(e.orig(), e.dest(), e.sym().oPrev().dest());
		removeTriangle(old1);
		removeTriangle(old2);
		//
		QuadEdge a = e.oPrev();
		QuadEdge b = e.sym().oPrev();
		splice(e, a);
		splice(e.sym(), b);
		splice(e, a.lNext());
		splice(e.sym(), b.lNext());
		// add at 2014-7-21
		if (e.p != null) {
			edges.remove(edges.prev(e.p));
		} else {
			// why it is null
//			printEdges();
			logger.debug("e.p == null, remove nothing");
		}
		//
		e.endPoints(a.dest(), b.dest());
		// add at 2014-7-21
		e.p = edges.insert(edges.first(), e);
		// add new
		Triangle t1 = new Triangle(e.orig(), e.dest(), e.oPrev().dest());
		Triangle t2 = new Triangle(e.orig(), e.dest(), e.sym().oPrev().dest());
		addTriangle(t1);
		addTriangle(t2);

	}

	/**
	 * 
	 * 
	 * @param x
	 * @param a
	 * @param b
	 * @return
	 */
	private Vector2d snap(Vector2d x, Vector2d a, Vector2d b) {
		if (x.equals(a, epsilon))
			return a;
		if (x.equals(b, epsilon))
			return b;
		double t1 = x.minus(a).dotProduct(b.minus(a)); // (x-a) | (b-a);
		double t2 = x.minus(b).dotProduct(a.minus(b));// (x-b) | (a-b);

		double t = Math.max(t1, t2) / (t1 + t2);
		if (t1 > t2) {
			return (Vector2d) a.multiply(1 - t).add(b.multiply(t));
		} else {
			return (Vector2d) b.multiply(1 - t).add(a.multiply(t));
		}
	}

	/**
	 * Shorten edge e s.t. its destination becomes x. Connect x to the previous destination of e by a new edge. If e is constrained, x is snapped onto the edge,
	 * and the new edge is also marked as constrained.
	 * 
	 * @param e
	 * @param x
	 */
	private void splitEdge(QuadEdge e, Vector2d x) {
		logger.debug("splitEdge: " + e.toString() + " at " + x.toString());
		// add at 2014-7-19 remove a triangle and add a new triangle
		Triangle old1 = new Triangle(e.orig(), e.dest(), e.oPrev().dest());
		Triangle old2 = new Triangle(e.orig(), e.dest(), e.sym().oPrev().dest());
		removeTriangle(old1);
		removeTriangle(old2);
		//
		Vector2d dt;
		if (e.isConstrained()) {
			// snap the point to the edge before splitting:
			dt = new Vector2d(snap(x, e.orig(), e.dest()));
		} else
			dt = new Vector2d(x);
		QuadEdge t = e.lNext();
		splice(e.sym(), t);
		// add at 2014-7-21
		if (e.p != null) {
			edges.remove(edges.prev(e.p));
		} else {
			// why it is null
//			printEdges();
			logger.debug("e.p == null, remove nothing");
		}
		//
		e.endPoints(e.orig(), dt);
		// add at 2014-7-21
		e.p = edges.insert(edges.first(), e);
		QuadEdge ne = connect(e, t);
		if (e.isConstrained()) {
			ne.constrain();
		}

	}

	/*************** Geometric Predicates for Delaunay Diagrams *****************/
	/**
	 * Returns twice the area of the oriented triangle (a, b, c), i.e., the area is positive if the triangle is oriented counterclockwise.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public double triArea(Vector2d a, Vector2d b, Vector2d c) {
		return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
	}

	/**
	 * Returns TRUE if the point d is inside the circle defined by the points a, b, c. See Guibas and Stolfi (1985) p.107.
	 * 
	 * @param orig
	 * @param dest
	 * @param dest2
	 * @param x
	 * @return
	 */
	private boolean inCircle(Vector2d a, Vector2d b, Vector2d c, Vector2d d) {
		double az = a.dotProduct(a);
		double bz = b.dotProduct(b);
		double cz = c.dotProduct(c);
		double dz = d.dotProduct(d);

		double det = (az * triArea(b, c, d) - bz * triArea(a, c, d) + cz * triArea(a, b, d) - dz * triArea(a, b, c));

		return (det > 0);
	}

	/**
	 * Returns TRUE if the points a, b, c are in a counterclockwise order
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private boolean ccw(Vector2d a, Vector2d b, Vector2d c) {
		double det = triArea(a, b, c);
		return (det > 0);
	}

	/**
	 * Returns TRUE if the points a, b, c are in a clockwise order
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private boolean cw(Vector2d a, Vector2d b, Vector2d c) {
		double det = triArea(a, b, c);
		return (det < 0);
	}

	/**
	 * Returns TRUE if the point x is strictly to the right of e
	 * 
	 * @param x
	 * @param e
	 * @return
	 */
	private boolean RightOf(Vector2d x, QuadEdge e) {
		return ccw(x, (e.dest()), (e.orig()));
	}

	/**
	 * Returns TRUE if the point x is strictly to the left of e
	 * 
	 * @param x
	 * @param e
	 * @return
	 */
	private boolean LeftOf(Vector2d x, QuadEdge e) {
		return cw(x, (e.dest()), (e.orig()));
	}

	/**
	 * A predicate that determines if the point x is on the edge e. The point is considered on if it is in the EPS-neighborhood of the edge.
	 * 
	 * @param x
	 * @param e
	 * @return
	 */
	private boolean onEdge(Vector2d x, QuadEdge e) {
		Vector2d a = e.orig();
		Vector2d b = e.dest();
		double t1 = (x.minus(a)).norm();
		double t2 = (x.minus(b)).norm();
		double t3;
		if (t1 <= epsilon || t2 <= epsilon)
			return true;
		t3 = (a.minus(b)).norm();
		if (t1 > t3 || t2 > t3)
			return false;
		Line line = new Line(a, b);
		return (x.onLine(line, epsilon));
	}

	/**
	 * Returns the intersection point between e and l (assumes it exists).
	 * 
	 * @param e
	 * @param l
	 * @return
	 */
	private Vector2d Intersect(QuadEdge e, Line l) {
		return l.intersect(e.orig(), e.dest());
	}

	/************* An Incremental Algorithm for the Construction of Delaunay Diagrams *********************************/

	/**
	 * Returns TRUE if there is a triangular face to the left of e.
	 * 
	 * @param e
	 * @return
	 */
	private boolean hasLeftFace(QuadEdge e) {
		return (e.lPrev().orig().equals(e.lNext().dest(), epsilon) && LeftOf(e.lPrev().orig(), e));
	}

	/**
	 * Returns TRUE if there is a triangular face to the right of e.
	 * 
	 * @param e
	 * @return
	 */
	private boolean hasRightFace(QuadEdge e) {
		return hasLeftFace(e.sym());
	}

	/**
	 * A simple, but recursive way of doing things. Flips e, if necessary, in which case calls itself recursively on the two edges that were to the right of e
	 * before it was flipped, to propagate the change.
	 * 
	 * @param e
	 */
	private void fixEdge(QuadEdge e) {
		if (e.isConstrained())
			return;
		QuadEdge f = e.oPrev();
		QuadEdge g = e.dNext();
		if (inCircle((e.dest()), (e.oNext().dest()), (e.orig()), (f.dest()))) {
			swap(e);
			fixEdge(f);
			fixEdge(g);
		}
	}

	/**
	 * Triangulates the left face of first, which is assumed to be closed.
	 * It is also assumed that all the vertices of that face lie to the left of last (the edge preceeding first).
	 * This is NOT intended as a general simple polygon triangulation routine.
	 * It is called by InsertEdge in order to restore a triangulation after an edge has been inserted.
	 * The routine consists of two loops:
	 * the outer loop continues as long as the left face is not a triangle.
	 * The inner loop examines successive triplets of vertices, creating a triangle whenever the triplet is counterclockwise.
	 * 
	 * @param first
	 */
	private void triangulate(QuadEdge first) {
		QuadEdge a, b, e, t1, t2;
		QuadEdge last = first.lPrev();

		while (first.lNext().lNext() != last) {
			e = first.lNext();
			t1 = first;
			while (e != last) {
				t2 = e.lNext();
				if (t2 == last && t1 == first)
					break;
				if (LeftOf(e.dest(), t1)) {
					if (t1 == first)
						t1 = first = connect(e, t1).sym();
					else
						t1 = connect(e, t1).sym();
					a = t1.oPrev();
					b = t1.dNext();
					fixEdge(a);
					fixEdge(b);
					e = t2;
				} else {
					t1 = e;
					e = t2;
				}
			}
		}
		a = last.lNext();
		b = last.lPrev();
		fixEdge(a);
		fixEdge(b);
		fixEdge(last);

	}

	/**
	 * Returns TRUE if the points a and b are closer than dist to each other. This is useful for creating nicer meshes, by preventing points from being too
	 * close to each other.
	 * 
	 * @param x
	 * @param orig
	 * @return
	 */
	private boolean coincide(Vector2d a, Vector2d b) {
		Vector2d d = a.minus(b);

		if (Math.abs(d.x) > epsilon || Math.abs(d.y) > epsilon)
			return false;

		return ((d.dotProduct(d)) <= epsilon * epsilon);
	}

	/**
	 * Inserts a new site into a CDT. This is basically the Guibas-Stolfi incremental site insertion algorithm, except that is does not flip constraining edges
	 * (in fact, it does not even test them.) Returns NIL(Edge) if insertion has failed; otherwise, returns an edge whose origin is the new site.
	 * 
	 * @param x
	 * @return
	 */
	private QuadEdge insertSite(Vector2d x) {
		QuadEdge e = locate(x);
		// logger.debug("inserting sites: " + x.toString());
		// logger.debug("locating x = " + x.toString() + " at " + e.toString());
		if (e == null) {
			return e;
		}

		if (coincide(x, e.orig())) {
			return e;
		}
		if (coincide(x, e.dest())) {
			return e.sym();
		}

		boolean hasLeft = hasLeftFace(e);
		boolean hasRight = hasRightFace(e);

		if (!hasLeft && !hasRight) {
			logger.error("InsertSite: edge does not have any faces");
			return null;
		}
		// x should be inside a face adjacent to e:
		boolean onEdge = onEdge(x, e);

		Boolean insideLeft = hasLeft && (onEdge || LeftOf(x, e)) && RightOf(x, e.oNext()) && RightOf(x, e.dPrev());
		Boolean insideRight = hasRight && (onEdge || RightOf(x, e)) && LeftOf(x, e.oPrev()) && LeftOf(x, e.dNext());

		if (!insideLeft && !insideRight) {
			logger.error("InsertSite: point not in a face adjacent to edge");
			return null;
		}

		if (insideLeft && coincide(x, e.oNext().dest()))
			return e.lPrev();
		if (insideRight && coincide(x, e.oPrev().dest()))
			return e.dNext();
		// Now we know, x lies within the quadrilateral whose diagonal is e (or a triangle, if e only has one adjacent face). We also know x is not one of e's
		// endpoints.
		if (onEdge) {
			// snap x to e, and check for coincidence:
			Vector2d xx = snap(x, e.orig(), e.dest());
			if (coincide(xx, e.orig()))
				return e;
			if (coincide(xx, e.dest()))
				return e.sym();

			if (hasRight && hasLeft) {
				// has two faces
				QuadEdge a, b, c, d;
				a = e.oPrev();
				b = e.dNext();
				c = e.lNext();
				d = e.lPrev();
				splitEdge(e, x);
				connect(e, e.lPrev());
				connect(e.oPrev(), e.sym());
				// TODO check
				Triangle t1 = new Triangle(x, b.orig(), b.dest());
				Triangle t2 = new Triangle(x, c.orig(), c.dest());
				addTriangle(t1);
				addTriangle(t2);
				//
				fixEdge(a);
				fixEdge(b);
				fixEdge(c);
				fixEdge(d);
			} else {
				// has only one face
				if (hasRight) {
					e = e.sym();
				}
				QuadEdge c = e.lNext();
				QuadEdge d = e.lPrev();
				splitEdge(e, x);
				connect(e, e.lPrev());
				// TODO check
				Triangle t1 = new Triangle(x, c.orig(), c.dest());
				addTriangle(t1);
				//
				fixEdge(c);
				fixEdge(d);
			}
			startingEdge = e.sym();
			return startingEdge;
		}
		// x is not on e, should be in face to the left of e:
		if (!insideLeft) {
			logger.error("InsertSite: point is not to the left of edge");
			return null;
		}

		// x should be strictly inside the left face:
		if (onEdge(x, e.oNext()) || onEdge(x, e.dPrev())) {
			logger.error("InsertSite: point is not strictly inside face");
			return null;
		}

		// Now, hopefully, x is strictly inside the left face of e, so everything should proceed smoothly from this point on.
		Vector2d first = e.orig();
		QuadEdge base = makeEdge(e.orig(), x, false);

		splice(base, e);
		// use this edge as the starting point next time:
		startingEdge = base.sym();
		do {
			base = connect(e, base.sym());
			e = base.oPrev();
		} while (e.dPrev() != startingEdge);
		// TODO check
		Triangle t1 = new Triangle(startingEdge.orig(), startingEdge.dest(), e.orig());
		addTriangle(t1);
		//

		// Examine suspect edges to ensure that the Delaunay condition is satisfied.
		do {
			QuadEdge t = e.oPrev();
			if (!e.isConstrained() && inCircle(e.orig(), t.dest(), e.dest(), x)) {
				swap(e);
				e = e.oPrev();
			} else if (e.lPrev() == startingEdge) { // no more suspect edges
				return startingEdge;
			} else {
				// pop a suspect edge
				e = e.oNext().lPrev();
			}
		} while (true);
	}

	/**
	 * Inserts a constraining edge into a CDT.
	 * 
	 * @param a
	 * @param b
	 */
	private void insertEdge(Vector2d a, Vector2d b) {
		logger.debug("inserting edge: " + a.toString() + "->" + b.toString());
		// add at 2014-7-19
		// to avoid aa.equals(bb, epsilon)
		if (coincide(a, b)) {
			logger.debug("coincide");
			return;
		}
		Vector2d aa = null;
		Vector2d bb = null;
		QuadEdge ea = insertSite(a);
		if (ea != null) {
			aa = ea.orig();
		}
		QuadEdge eb = insertSite(b);
		if (eb != null)
			bb = eb.orig();
		if (ea == null || eb == null) {
			logger.error("InsertEdge: could not insert endpoints of edge");
			return;
		}

		startingEdge = ea;
		ea = locate(aa);
		if (ea == null) {
			logger.error("InsertEdge: could not locate an endpoint");
			return;
		}
		if (!(aa.equals(ea.orig(), epsilon))) {
			ea = ea.sym();
		}
		if (!(aa.equals(ea.orig(), epsilon))) {
			logger.error("InsertEdge: point a is not an endpoint of ea");
			return;
		}

		if (aa.equals(bb, epsilon)) {
			logger.debug("InsertEdge: both ends map to same vertex");
			return;
		}

		// aa and bb are vertices in the mesh; our goal is to connect them by a sequence of one or more edges.

		Line ab = new Line(aa, bb);
		Vector2d dd = bb.minus(aa);
		QuadEdge t, ne;
		Vector2d last = eb.orig();

		while (!ea.orig().equals(last, epsilon)) {

			// Set ea to the first edge to the right of (or aligned with) the segment (a,b), by moving ccw around the origin of ea:
			t = ea;
			do {
				if ((ea.dest().onLine(ab, epsilon) && ((ea.dest().minus(aa).dotProduct(dd)) > 0))) {
					break;
				}
				if (ea.oNext().dest().onLine(ab, epsilon) && ((ea.oNext().dest().minus(aa)).dotProduct(dd)) > 0) {
					ea = ea.oNext();
					break;
				}
				if (!cw(ea.dest(), bb, aa) && cw(ea.oNext().dest(), bb, aa)) {
					break;
				}
				ea = ea.oNext();
				if (ea == t) {
					// TODO check this ==
					logger.error("InsertEdge: infinite loop");
					return;
				}
			} while (true);

			// check to see if an edge is already there:
			if (ea.dest().onLine(ab, epsilon)) {
				ea.constrain();
				aa = ea.dest();
				if (aa == bb) {
					return;
				}
				ab = new Line(aa, bb);

				dd = bb.minus(aa);
				ea = ea.sym().oNext();
				continue;
			}

			t = ea;

			while (true) {
				if (t.lNext().dest().onLine(ab, epsilon)) {
					if (t.lNext().lNext().lNext() == ea) {
						// edge is already there
						t.lNext().lNext().constrain();
						ea = t.lNext().sym();
						break;
					} else {
						// need a new edge
						ne = connect(t.lNext(), ea);
						ne.constrain();
						ea = t.lNext().sym();
						triangulate(ne.lNext());// TODO
						triangulate(ne.oPrev());
						break;
					}
				}
				if (t.lNext().dest().smallerThan(ab, epsilon)) {
					logger.debug("t: " + t.toString());
					logger.debug("t.lNext(): " + t.lNext().toString());
					logger.debug("ab: " + aa.toString() + "->" + bb.toString());

					// edges cross
					if (!t.lNext().isConstrained()) {
						deleteEdge(t.lNext());
						// TODO add at 2014-7-21
					} else {
						// the crossing edge is also constrained compute and insert the intersection
						Vector2d x = Intersect(t.lNext(), ab);
						// split t->Lnext() into two at x
						splitEdge(t.lNext(), x);
						// connect to the new vertex:
						ne = connect(t.lNext(), ea);
						ne.constrain();
						ea = t.lNext().sym();
						triangulate(ne.lNext());
						triangulate(ne.oPrev());
						break;
					}
				} else {
					t = t.lNext();
				}
			}
		}
	}

	/**
	 * The goals of this routine are as follows:
	 * </p>
	 * - if x coincides with one or more vertices in the mesh, return an edge whose origin is one such vertex;
	 * </p>
	 * - otherwise, return the edge nearest to x oriented ccw around a face containing x (up to numerical round-off);
	 * </p>
	 * In the event of failure, we pass the search to BruteForceLocate.
	 * 
	 * @param x
	 * @return
	 */
	private QuadEdge locate(Vector2d x) {
		if (bruteFalseLocate) {
			return bruteForceLocate(x);
		}

		QuadEdge e = startingEdge;
		int iterations = 0;

		while (true) {
			iterations++;

			if (iterations > numEdges()) {
				logger.debug("Locate: infinite loop");
				return bruteForceLocate(x);
			}

			if (coincide(x, e.orig())) {
				startingEdge = e;
				return startingEdge;
			}
			if (coincide(x, e.dest())) {
				startingEdge = e.sym();
				return startingEdge;
			}
			if (RightOf(x, e)) {
				e = e.sym();
			}
			if (hasLeftFace(e)) {

				if (coincide(x, e.lPrev().orig())) {
					startingEdge = e.lPrev();
					return startingEdge;
				}

				if (LeftOf(x, e.oNext())) {
					e = e.oNext();
					continue;
				}

				if (LeftOf(x, e.dPrev())) {
					e = e.dPrev();
					continue;
				}

				Line l1 = new Line(e.orig(), e.dest());
				Line l2 = new Line(e.dest(), e.lPrev().orig());
				Line l3 = new Line(e.lPrev().orig(), e.orig());
				//
				Triangle t = new Triangle(e.orig(), e.dest(), e.lPrev().orig());
				removeTriangle(t);
				//
				int d = min(Math.abs(l1.eval(x)), Math.abs(l2.eval(x)), Math.abs(l3.eval(x)));
				switch (d) {
				case 0:
					startingEdge = e;
					break;
				case 1:
					startingEdge = e.lNext();
					break;
				case 2:
					startingEdge = e.lPrev();
					break;
				}
				return startingEdge;
			}

			// there is no left face!
			if (onEdge(x, e)) {
				startingEdge = e.sym();
				return startingEdge;
			}

			if (RightOf(x, e.oPrev())) {
				e = e.oPrev().sym();
				continue;
			}
			if (RightOf(x, e.dNext())) {
				e = e.dNext().sym();
				continue;
			}
			logger.debug("Locate: I'm stuck and I can't move on");
			return bruteForceLocate(x);
		}
	}

	/**
	 * Same as Locate, but uses a brute force exhaustive search.
	 * 
	 * @param x
	 * @return
	 */
	private QuadEdge bruteForceLocate(Vector2d x) {
		logger.debug("bruteForceLocate");
		QuadEdge e;
		for (LlistNode<Edge> p = edges.first(); !edges.isEnd(p); p = edges.next(p)) {
			e = ((QuadEdge) edges.retrieve(p));
			// first, examine the endpoints for coincidence:
			if (coincide(x, e.orig())) {
				startingEdge = e;
				return startingEdge;
			}
			if (coincide(x, e.dest())) {
				startingEdge = e.sym();
				return startingEdge;
			}
			// x isn't one of the endpoints.
			// orient e s.t. x is not to its right:
			if (RightOf(x, e)) {
				e = e.sym();
			}
			// x is not to the right of e.
			// now we need to test if x is in the left face of e:
			if (hasLeftFace(e)) {

				if (coincide(x, e.lPrev().orig())) {
					startingEdge = e.lPrev();
					return startingEdge;
				}

				if (!RightOf(x, e.lPrev()) && !RightOf(x, e.lNext())) {
					Line l1 = new Line(e.orig(), e.dest());
					Line l2 = new Line(e.dest(), e.lPrev().orig());
					Line l3 = new Line(e.lPrev().orig(), e.orig());
					//
					Triangle t = new Triangle(e.orig(), e.dest(), e.lPrev().orig());
					removeTriangle(t);
					//
					int d = min(Math.abs(l1.eval(x)), Math.abs(l2.eval(x)), Math.abs(l3.eval(x)));
					switch (d) {
					case 0:
						startingEdge = e;
						break;
					case 1:
						startingEdge = e.lNext();
						break;
					case 2:
						startingEdge = e.lPrev();
						break;
					}
					return startingEdge;
				} else {
					continue;
				}
			}

			// there is no left face!
			if (onEdge(x, e)) {
				startingEdge = e.sym();
				return startingEdge;
			} else {
				continue;
			}

		}
		logger.debug("Locate: Could not locate using brute force");
		return null;
	}

	/************************* Mesh Traversal Routines **************************/
	// private void MarkEdge(QuadEdge e) {
	// QuadEdge t = e;
	// do {
	// t.mark = Edge.V_FLAG.VISITED;
	// t = t.oNext();
	// } while (t != e);
	// }

	/*********************** Basic Topological Operators ************************/

	// private QuadEdge MakeEdge(boolean constrained)
	// {
	// QuadEdge qe = new QuadEdge(constrained);
	// qe.p = edges.insert(edges.first(), qe);
	// return qe.edges();
	// }

	// private QuadEdge MakeEdge(Vector2d a, Vector2d b, boolean constrained) {
	// logger.debug("MakeEdge: " + a.toString() + "->" + b.toString() + ": " + constrained);
	// QuadEdge e = MakeEdge(constrained);
	// e.endPoints(a, b);
	// return e;
	// }

	private QuadEdge makeEdge(Vector2d a, Vector2d b, boolean constrained) {
		QuadEdge qe = QuadEdge.makeEdge(a, b);
		if (Strategy.countNumQueries == 57) {
			// printALine(a, b, Color.GREEN);
			logger.debug("MakeEdge: " + a.toString() + "->" + b.toString());
			logger.debug("");
		}
		if (constrained) {
			qe.constrain();
		}
		qe.p = edges.insert(edges.first(), qe);
		return qe;
	}

	/**
	 * This operator affects the two edge rings around the origins of a and b, and, independently, the two edge rings around the left faces of a and b. In each
	 * case, (i) if the two rings are distinct, Splice will combine them into one; (ii) if the two are the same ring, Splice will break it into two separate
	 * pieces. Thus, Splice can be used both to attach the two edges together, and to break them apart. See Guibas and Stolfi (1985) p.96 for more details and
	 * illustrations.
	 * 
	 * @param a
	 * @param b
	 */
	private void splice(QuadEdge a, QuadEdge b) {
		QuadEdge.splice(a, b);

	}

	private void deleteEdge(QuadEdge e) {
//		logger.debug("deleteEdge: " + e.toString());
//		if (findAnEdge(e)) {
//			logger.debug("find");
//		} else {
//			logger.debug("not find");
//		}
		// Make sure the starting edge does not get deleted:
		if (startingEdge == e) {
			logger.debug("Mesh::DeleteEdge: attempting to delete starting edge");
			// try to recover:
			if (e != e.oNext()) {
				startingEdge = e.oNext();
			} else {
				startingEdge = e.dNext();
			}
		}

		// remove edge from the edge list:
		QuadEdge qe = e;
		edges.remove(edges.prev(qe.p));
		
		// add at 2014-7-11 remove a triangle and add a new triangle
		Triangle old1 = new Triangle(e.orig(), e.dest(), e.oPrev().dest());
		Triangle old2 = new Triangle(e.orig(), e.dest(), e.sym().oPrev().dest());
		removeTriangle(old1);
		removeTriangle(old2);

		// add at 2014-7-21
		splice(e, e.oPrev());
		splice(e.sym(), e.sym().oPrev());
		e.delete();
	}

	/****************************************************************/

	private int min(double abs, double abs2, double abs3) {
		if (abs <= abs2 && abs <= abs3) {
			return 0;
		} else if (abs2 <= abs && abs2 <= abs3) {
			return 1;
		} else {
			return 2;
		}
	}

	private int numEdges() {
		return edges.length();
	}

	/************** For Testing ***************/
	public void printEdges() {
		logger.debug("Printing Edges: " + edges.length());
		PaintShapes.paint.color = Color.RED;
		for (LlistNode<Edge> p = edges.first(); !edges.isEnd(p); p = edges.next(p)) {
			QuadEdge e = ((QuadEdge) edges.retrieve(p));
			logger.debug(e.toString());
			Vector2d a = e.orig();
			Vector2d b = e.sym().orig();
//			PaintShapes.paint.addLine(a, b);
		}
//		PaintShapes.paint.myRepaint();
		logger.debug("End of Printing Edges.");
	}

	public void printTriangles() {
		logger.debug("Printing Triangles: " + triangleMap.size());
		PaintShapes.paint.color = PaintShapes.paint.blackTranslucence;
		Iterator it = triangleMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry pairs = (Entry) it.next();
			Triangle t = (Triangle) pairs.getKey();
			PaintShapes.paint.addTriangle(t);
			logger.debug(t.toString());
		}
		PaintShapes.paint.myRepaint();

		logger.debug("End of Printing Triangles.");
	}

	private void printALine(Vector2d a, Vector2d b, java.awt.Color color) {
		// paint the triangle
		PaintShapes.paint.color = color;
		PaintShapes.paint.addLine(a, b);
		PaintShapes.paint.myRepaint();
	}

	private void printATriangle(Triangle t, java.awt.Color color) {
		// paint the triangle
		PaintShapes.paint.color = color;
		PaintShapes.paint.addTriangle(t);
		PaintShapes.paint.myRepaint();
	}

	private void printVectorList(Vector2d[] vs) {
		StringBuffer sb = new StringBuffer("Vectors: ");
		for (int i = 0; i < vs.length; i++) {
			sb.append(vs[i].toString());
			sb.append("; ");
		}
		logger.debug(sb.toString());
	}

	private boolean findAnEdge(QuadEdge target) {
		for (LlistNode<Edge> p = edges.first(); !edges.isEnd(p); p = edges.next(p)) {
			QuadEdge e = ((QuadEdge) edges.retrieve(p));
			if (coincide(target.orig(), e.orig()) && coincide(target.dest(), e.dest())) {
				return true;
			}
		}
		return false;
	}

	/*************** Triangle operators *****************/
	private void addTriangle(Triangle t) {
		Vector2d center = t.centroid();
		//
		Coordinate outerPoint = AlgoDCDT.outerPoint;
		Coordinate p = new Coordinate(center.x, center.y);
		boolean inside = false;
		if (hole != null) {
			inside = GeoOperator.pointInsidePolygon(hole, outerPoint, p);
		}
		if (!inside) {
			Triangle sorted = t.sortTriangle();
//			if (Strategy.countNumQueries == 57) {
//				printATriangle(sorted, Color.GREEN);
//				logger.info("trying to add: " + sorted.toString());
//				if (triangleMap.get(sorted) == null) {
//					logger.debug("adding for the first time");
//				} else {
//					logger.debug("This key exists in the map");
//				}
//			}
			triangleMap.put(sorted, "");
		} else {
			logger.debug("inside: " + hole.toString());
			logger.debug(t.toString());
		}
	}

	public void removeTriangle(Triangle t) {
		Triangle sorted = t.sortTriangle();
		String value = triangleMap.get(sorted);
		if (value == null) {
			logger.debug("removing nothing: " + sorted.toString());
		}
//		if (Strategy.countNumQueries == 57) {
//			printATriangle(sorted, Color.BLUE);
//			logger.info("trying to remove: " + sorted.toString());
//			if (triangleMap.get(sorted) == null) {
//				logger.debug("remove nothing");
//			} else {
//				logger.debug("remove successfully");
//			}
//		}
		triangleMap.remove(sorted.sortTriangle());
	}

	/**
	 * @return sorted Triangles, from large to small
	 */
	public HashMap<Triangle, String> getTriangles() {
		return triangleMap;
	}

	public Triangle getBiggestTriangle() {
		if (triangleMap.size() == 0) {
			return null;
		}
		double maxArea = Double.MIN_VALUE;
		// max triangle
		Triangle triangle = null;
		// find the largest triangle
		Iterator it = triangleMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			Triangle t = (Triangle) entry.getKey();
			if (t.area() > maxArea) {
				maxArea = t.area();
				triangle = t;
			}
		}
		return triangle;
	}

	/**
	 * Check whether the computed triangles are consisting with the edges.
	 */
	public void checkTriangles() {
		// TODO

	}

	/**
	 * Avoiding holes
	 * 
	 * @param holes
	 * @return
	 */
	public HashMap<Triangle, String> getTrianglesFromEdges(ArrayList<Polygon> holes) {
		for (LlistNode<Edge> p = edges.first(); !edges.isEnd(p); p = edges.next(p)) {
			QuadEdge e = ((QuadEdge) edges.retrieve(p));
		}

		return null;
	}
	
	public boolean same(Triangle triangle, Polygon inner) {
		List<TriangulationPoint> list = inner.getPoints();
		if(list.size() != 3) {
			return false;
		}
		for (int i = 0; i < list.size(); i++) {
			TriangulationPoint tp = list.get(i);
			Vector2d tpV = new Vector2d(tp.getX(), tp.getY());
			Vector2d v = triangle.points[i];
			if(!coincide(tpV, v)){
				return false;
			}
		}
		return true;
	}

}
