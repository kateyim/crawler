package mo.umac.kallmann.cdt;

public class Edge {

	private boolean constrained = false;
	
	public int num;
	public V_FLAG mark;

	public LlistNode<Edge> p;
	
	public boolean isConstrained() {
		return constrained;
	}

	public void constrain() {
		this.constrained = true;
	}

	enum V_FLAG {
		UNVISITED, VISITED
	}
	
}
