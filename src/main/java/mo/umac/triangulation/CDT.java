/**
 * 
 */
package mo.umac.triangulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author kate
 */
public class CDT {

	protected static Logger logger = Logger.getLogger(CDT.class.getName());

	/**
	 * The final delaunay triangles.
	 */
	protected ArrayList<DelaunayTriangle> triList = null;
	protected ArrayList<Point> allPoints = null;
	private boolean terminated = false;
	private Point tailP;
	private Point headP;

	/**
	 * The advancing front
	 */
	private AdvancingFront aFront;
	private final double ALPHA = 0.3;

	public void triangulate(ArrayList<Polygon> polygonList) {
		clear();
		for (Polygon p : polygonList) {
			prepareTriangulation(p);
		}
		triangulate();
	}

	public void clear() {
		terminated = false;

		if (triList == null) {
			triList = new ArrayList<DelaunayTriangle>();
		} else {
			triList.clear();
		}
		if (allPoints == null) {
			allPoints = new ArrayList<Point>(200);
		} else {
			allPoints.clear();
		}
	}

	/**
	 * Creates constraints and populates the context with points
	 */
	private void prepareTriangulation(Polygon p) {
		// TODO prepare for the boundary polygon
		// add these holes as constraints
		List<Point> points = p.getPoints();
		for (int i = 0; i < points.size() - 1; i++) {
			new DTSweepConstraint(points.get(i), points.get(i + 1));
		}
		new DTSweepConstraint(points.get(0), points.get(points.size() - 1));
		allPoints.addAll(points);
	}

	private void triangulate() {
		initialization();
		sweeping();
		finalization();
	}

	private void initialization() {
		sort();
		addTheFirstSweepingLine();
		createAdvancingFront();
	}

	/**
	 * all input points are sorted regarding y coordinate, regardless of whether they define an edge or not.
	 * Those points having the same y coordinates are also sorted in the x direction.
	 */
	private void sort() {
		Collections.sort(allPoints, new DTSweepPointComparator());
	}

	private void addTheFirstSweepingLine() {
		double xmax, xmin;
		double ymax, ymin;

		xmax = xmin = allPoints.get(0).getX();
		ymax = ymin = allPoints.get(0).getY();
		// Calculate bounds. Should be combined with the sorting
		for (Point p : allPoints) {
			if (p.getX() > xmax)
				xmax = p.getX();
			if (p.getX() < xmin)
				xmin = p.getX();
			if (p.getY() > ymax)
				ymax = p.getY();
			if (p.getY() < ymin)
				ymin = p.getY();
		}
		double deltaX = ALPHA * (xmax - xmin);
		double deltaY = ALPHA * (ymax - ymin);
		// two artificial points
		Point p1 = new Point(xmin - deltaX, ymin - deltaY);
		Point p2 = new Point(xmax + deltaX, ymin - deltaY);

		headP = p1;
		tailP = p2;
	}

	private void createAdvancingFront() {
		AdvancingFrontNode head, tail, middle;
		// Initial triangle
		DelaunayTriangle iTriangle = new DelaunayTriangle(allPoints.get(0), tailP, headP);
		triList.add(iTriangle);

		// TODO which one is the head? order problems?
		head = new AdvancingFrontNode(iTriangle.points[1]);
		head.triangle = iTriangle;
		middle = new AdvancingFrontNode(iTriangle.points[0]);
		middle.triangle = iTriangle;
		tail = new AdvancingFrontNode(iTriangle.points[2]);

		aFront = new AdvancingFront(head, tail);

		// TODO: I think it would be more intuitive if head is middles next and not previous so swap head and tail
		aFront.head.next = middle;
		middle.next = aFront.tail;
		middle.prev = aFront.head;
		aFront.tail.prev = middle;
	}

	private void sweeping() {
		Point point;
		AdvancingFrontNode nodeLeft;

		// This loop represents the sweeping line
		for (int i = 0; i < allPoints.size(); i++) {
			point = allPoints.get(i);
			// point event
			nodeLeft = pointEvent(point);
			if (point.hasEdges()) {
				for (DTSweepConstraint e : point.getEdges()) {
					// edge event
					edgeEvent(e, nodeLeft);
				}
			}

		}

	}

	private AdvancingFrontNode pointEvent(Point point) {
		AdvancingFrontNode nodeLeft = null;
		boolean onTheLeftNode = aFront.locateNode(point, nodeLeft);
		AdvancingFrontNode nodeRight = null;
		// TODO judge of the null point
		if (onTheLeftNode) {
			AdvancingFrontNode node = nodeLeft;
			nodeLeft = node.prev;
			nodeRight = node.next;
			DelaunayTriangle dt1 = new DelaunayTriangle(point, nodeLeft.getPoint(), node.getPoint());
			DelaunayTriangle dt2 = new DelaunayTriangle(point, nodeRight.getPoint(), node.getPoint());
			// TODO 
			if (!legalize(dt)) {
				mapTriangleToNodes(dt);
			}
			triList.add(dt1);
			triList.add(dt2);
		} else {
			nodeRight = nodeLeft.next;
			DelaunayTriangle dt = new DelaunayTriangle(point, nodeLeft.getPoint(), nodeRight.getPoint());
			if (!legalize(dt)) {
				mapTriangleToNodes(dt);
			}
			triList.add(dt);
		}
		// TODO neighbor?
		//
		AdvancingFrontNode newNode = new AdvancingFrontNode(point);
		newNode.next = nodeLeft.next;
		newNode.prev = nodeLeft;
		nodeLeft.next.prev = newNode;
		nodeLeft.next = newNode;
		//
		aFront.addNode(newNode);
		return newNode;
	}

	/**
	 * Returns true if triangle was legalized
	 */
	private boolean legalize(DelaunayTriangle t) {
		int oi;
		boolean inside;
		Point p, op;
		DelaunayTriangle ot;
		// To legalize a triangle we start by finding if any of the three edges
		// violate the Delaunay condition
		for (int i = 0; i < 3; i++) {
			// TODO: fix so that cEdge is always valid when creating new triangles then we can check it here
			// instead of below with ot
			if (t.dEdge[i]) {
				continue;
			}
			ot = t.neighbors[i];
			if (ot != null) {
				p = t.points[i];
				op = ot.oppositePoint(t, p);
				oi = ot.index(op);
				// If this is a Constrained Edge or a Delaunay Edge(only during recursive legalization)
				// then we should not try to legalize
				if (ot.cEdge[oi] || ot.dEdge[oi]) {
					t.cEdge[i] = ot.cEdge[oi]; // XXX: have no good way of setting this property when creating new triangles so lets set it here
					continue;
				}
				inside = TriangulationUtil.smartIncircle(p, t.pointCCW(p), t.pointCW(p), op);
				if (inside) {
					boolean notLegalized;

					// Lets mark this shared edge as Delaunay
					t.dEdge[i] = true;
					ot.dEdge[oi] = true;

					// Lets rotate shared edge one vertex CW to legalize it
					rotateTrianglePair(t, p, ot, op);

					// We now got one valid Delaunay Edge shared by two triangles
					// This gives us 4 new edges to check for Delaunay

					// Make sure that triangle to node mapping is done only one time for a specific triangle
					notLegalized = !legalize(t);
					if (notLegalized) {
						mapTriangleToNodes(t);
					}
					notLegalized = !legalize(ot);
					if (notLegalized) {
						mapTriangleToNodes(ot);
					}

					// Reset the Delaunay edges, since they only are valid Delaunay edges
					// until we add a new triangle or point.
					// XXX: need to think about this. Can these edges be tried after we
					// return to previous recursive level?
					t.dEdge[i] = false;
					ot.dEdge[oi] = false;

					// If triangle have been legalized no need to check the other edges since
					// the recursive legalization will handles those so we can end here.
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Rotates a triangle pair one vertex CW
	 * 
	 * <pre>
	 *       n2                    n2
	 *  P +-----+             P +-----+
	 *    | t  /|               |\  t |  
	 *    |   / |               | \   |
	 *  n1|  /  |n3           n1|  \  |n3
	 *    | /   |    after CW   |   \ |
	 *    |/ oT |               | oT \|
	 *    +-----+ oP            +-----+
	 *       n4                    n4
	 * </pre>
	 */
	private static void rotateTrianglePair(DelaunayTriangle t, Point p, DelaunayTriangle ot, Point op) {
		DelaunayTriangle n1, n2, n3, n4;
		n1 = t.neighborCCW(p);
		n2 = t.neighborCW(p);
		n3 = ot.neighborCCW(op);
		n4 = ot.neighborCW(op);

		boolean ce1, ce2, ce3, ce4;
		ce1 = t.getConstrainedEdgeCCW(p);
		ce2 = t.getConstrainedEdgeCW(p);
		ce3 = ot.getConstrainedEdgeCCW(op);
		ce4 = ot.getConstrainedEdgeCW(op);

		boolean de1, de2, de3, de4;
		de1 = t.getDelunayEdgeCCW(p);
		de2 = t.getDelunayEdgeCW(p);
		de3 = ot.getDelunayEdgeCCW(op);
		de4 = ot.getDelunayEdgeCW(op);

		t.legalize(p, op);
		ot.legalize(op, p);

		// Remap dEdge
		ot.setDelunayEdgeCCW(p, de1);
		t.setDelunayEdgeCW(p, de2);
		t.setDelunayEdgeCCW(op, de3);
		ot.setDelunayEdgeCW(op, de4);

		// Remap cEdge
		ot.setConstrainedEdgeCCW(p, ce1);
		t.setConstrainedEdgeCW(p, ce2);
		t.setConstrainedEdgeCCW(op, ce3);
		ot.setConstrainedEdgeCW(op, ce4);

		// Remap neighbors
		// XXX: might optimize the markNeighbor by keeping track of
		// what side should be assigned to what neighbor after the
		// rotation. Now mark neighbor does lots of testing to find
		// the right side.
		t.clearNeighbors();
		ot.clearNeighbors();
		if (n1 != null)
			ot.markNeighbor(n1);
		if (n2 != null)
			t.markNeighbor(n2);
		if (n3 != null)
			t.markNeighbor(n3);
		if (n4 != null)
			ot.markNeighbor(n4);
		t.markNeighbor(ot);
	}

	/**
	 * Try to map a node to all sides of this triangle that don't have
	 * a neighbor.
	 * 
	 * @param t
	 */
	public void mapTriangleToNodes(DelaunayTriangle t) {
		AdvancingFrontNode n;
		for (int i = 0; i < 3; i++) {
			if (t.neighbors[i] == null) {
				n = aFront.locatePoint(t.pointCW(t.points[i]));
				if (n != null) {
					n.triangle = t;
				}
			}
		}
	}

	/**
	 * Adds a triangle to the advancing front to fill a hole.
	 * 
	 * @param tcx
	 * @param node
	 *            - middle node, that is the bottom of the hole
	 */
	private static void fill(AdvancingFrontNode node) {
		DelaunayTriangle triangle = new DelaunayTriangle(node.prev.point, node.point, node.next.point);
		// TODO: should copy the cEdge value from neighbor triangles
		// for now cEdge values are copied during the legalize
		triangle.markNeighbor(node.prev.triangle);
		triangle.markNeighbor(node.triangle);
		tcx.addToList(triangle);

		// Update the advancing front
		node.prev.next = node.next;
		node.next.prev = node.prev;
		tcx.removeNode(node);

		// If it was legalized the triangle has already been mapped
		if (!legalize(tcx, triangle)) {
			tcx.mapTriangleToNodes(triangle);
		}
	}

	/**
	 * Fills holes in the Advancing Front
	 * 
	 * @param tcx
	 * @param n
	 */
	private static void fillAdvancingFront(AdvancingFrontNode n) {
		AdvancingFrontNode node;
		double angle;

		// Fill right holes
		node = n.next;
		while (node.hasNext()) {
			if (isLargeHole(node)) {
				break;
			}
			fill(tcx, node);
			node = node.next;
		}

		// Fill left holes
		node = n.prev;
		while (node.hasPrevious()) {
			if (isLargeHole(node)) {
				break;
			}
			fill(tcx, node);
			node = node.prev;
		}

		// Fill right basins
		if (n.hasNext() && n.next.hasNext()) {
			angle = basinAngle(n);
			if (angle < PI_3div4) {
				fillBasin(tcx, n);
			}
		}
	}

	private void updateAdvancingFront() {

	}

	private void edgeEvent(DTSweepConstraint e, AdvancingFrontNode nodes) {

	}

	private void legalize() {

	}

	private void finalization() {
		// TODO Auto-generated method stub

	}

}
