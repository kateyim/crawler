/**
 * 
 */
package mo.umac.triangulation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.delaunay.sweep.AdvancingFront;
import org.poly2tri.triangulation.delaunay.sweep.AdvancingFrontNode;

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
	public void prepareTriangulation(Polygon p) {
		List<Point> points = p.getPoints();
		// Outer constraints
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
		createAdvancingFront();
	}

	/**
	 * all input points are sorted regarding y coordinate, regardless of whether
	 * they define an edge or not. Those points having the same y coordinates
	 * are also
	 * sorted in the x direction.
	 */
	private void sort() {

	}

	public void createAdvancingFront() {
		AdvancingFrontNode head, tail, middle;
		// Initial triangle
		DelaunayTriangle iTriangle = new DelaunayTriangle(_points.get(0), getTail(), getHead());
		addToList(iTriangle);

		head = new AdvancingFrontNode(iTriangle.points[1]);
		head.triangle = iTriangle;
		middle = new AdvancingFrontNode(iTriangle.points[0]);
		middle.triangle = iTriangle;
		tail = new AdvancingFrontNode(iTriangle.points[2]);

		aFront = new AdvancingFront(head, tail);
		// XXX Kate: didn't add node middle
		aFront.addNode(middle);

		// TODO: I think it would be more intuitive if head is middles next and
		// not previous
		// so swap head and tail
		aFront.head.next = middle;
		middle.next = aFront.tail;
		middle.prev = aFront.head;
		aFront.tail.prev = middle;
	}

	private void sweeping() {
		// TODO Auto-generated method stub

	}

	private void finalization() {
		// TODO Auto-generated method stub

	}

}
