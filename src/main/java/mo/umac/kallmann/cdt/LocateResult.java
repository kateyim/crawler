package mo.umac.kallmann.cdt;

public class LocateResult {
	
	enum LOC {
		VERTICE, EDGE, OTHER
	}
	
	public LOC location;

	public int elementID = Triangulation.INIT_VALUE;
	
}
