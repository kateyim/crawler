/**
 * 
 */
package mo.umac.triangulation;

import java.util.ArrayList;
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
	protected ArrayList<TriangulationPoint> allPoints = null;
	private boolean terminated = false;

	public void triangulate(ArrayList<Polygon> polygonList) {
		clear();
		for (Polygon p : polygonList) {
			prepareTriangulation(p);
		}
		triangulate();
		clear();
	}

	/**
	 * Creates constraints and populates the context with points
	 */
	public void prepareTriangulation(Polygon p) {
		List<TriangulationPoint> points = p.getPoints();
		// Outer constraints
		for (int i = 0; i < points.size() - 1; i++) {
			newConstraint(points.get(i), points.get(i + 1));
		}
		newConstraint(points.get(0), points.get(points.size() - 1));
		allPoints.addAll(points);
	}

	/**
	 * Give two points in any order. Will always be ordered so
	 * that q.y > p.y and q.x > p.x if same y value
	 * 
	 * @param p1
	 * @param p2
	 */
	public void newConstraint(TriangulationPoint p1, TriangulationPoint p2)
	// throws DuplicatePointException
	{
		TriangulationPoint p = p1;
		TriangulationPoint q = p2;
		if (p1.getY() > p2.getY()) {
			q = p1;
			p = p2;
		} else if (p1.getY() == p2.getY()) {
			if (p1.getX() > p2.getX()) {
				q = p1;
				p = p2;
			} else if (p1.getX() == p2.getX()) {
				logger.error("Failed to create constraint " + p1.toString() + "=" + p2.toString());
				// throw new DuplicatePointException( p1 + "=" + p2 );
				// return;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("DTSweepConstraint, q is [" + q.getX() + ", " + q.getY() + "]");
			logger.debug("DTSweepConstraint, p is [" + p.getX() + ", " + p.getY() + "]");

		}
		q.addEdge(this);
	}

	public void clear() {
		terminated = false;

		if (triList == null) {
			triList = new ArrayList<DelaunayTriangle>();
		} else {
			triList.clear();
		}
		if (points == null) {
			points = new ArrayList<TriangulationPoint>(200);
		} else {
			points.clear();
		}
	}

}
