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
		createAdvancingFront();
		addTheFirstSweepingLine();
		sort();
	}

	private void createAdvancingFront() {
		AdvancingFrontNode head, tail, middle;
		// Initial triangle
		DelaunayTriangle iTriangle = new DelaunayTriangle(allPoints.get(0), tailP, headP);
		triList.add(iTriangle);

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
		Point p1 = new Point(xmax + deltaX, ymin - deltaY);
		Point p2 = new Point(xmin - deltaX, ymin - deltaY);

		headP = p1;
		tailP = p2;
	}

	/**
	 * all input points are sorted regarding y coordinate, regardless of whether they define an edge or not.
	 * Those points having the same y coordinates are also sorted in the x direction.
	 */
	private void sort() {
		Collections.sort(allPoints, new DTSweepPointComparator());
	}

	private void sweeping() {
		Point point;
		AdvancingFrontNode node;

		for (int i = 0; i < allPoints.size(); i++) {
			point = allPoints.get(i);
			// point event
			node = pointEvent(point);

			if (point.hasEdges()) {
				for (DTSweepConstraint e : point.getEdges()) {
					// edge event
					edgeEvent(e, node);
				}
			}

		}

	}

	private AdvancingFrontNode pointEvent(Point point) {
		
		
		return null;
	}

	private void edgeEvent(DTSweepConstraint e, AdvancingFrontNode node) {
		
		
	}

	private void finalization() {
		// TODO Auto-generated method stub

	}

}
