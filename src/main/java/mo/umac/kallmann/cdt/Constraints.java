package mo.umac.kallmann.cdt;

import java.util.ArrayList;

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
	public ArrayList<Vertice> verticeList = new ArrayList<Vertice>();
	
	public ArrayList<Edge> edgeList = new ArrayList<Edge>();

	public Constraints(ArrayList<Vertice> verticeList, ArrayList<Edge> edgeList) {
		super();
		this.verticeList = verticeList;
		this.edgeList = edgeList;
	}
	
	
}
