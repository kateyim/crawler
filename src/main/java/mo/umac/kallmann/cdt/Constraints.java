package mo.umac.kallmann.cdt;

import java.util.ArrayList;
import java.util.List;

import org.poly2tri.triangulation.TriangulationPoint;


/**
 * Constraints given to the triangulation
 * 
 * @author Kate
 *
 */
public class Constraints {
	
	/**
	 * If it is a polygon, then both of the first and the last point should be inserted.
	 */
	public ArrayList<Vector2d> pointList = new ArrayList<Vector2d>();
	
//	public ArrayList<Edge> edgeList = new ArrayList<Edge>();

	public Constraints(ArrayList<Vector2d> pointList) {
		this.pointList = pointList;
	}

	public Constraints(List<TriangulationPoint> points) {
		for (int i = 0; i < points.size(); i++) {
			TriangulationPoint tp = points.get(i);
			Vector2d v = new Vector2d(tp.getX(), tp.getY());
			pointList.add(v);
		}
	}
	
	
}
