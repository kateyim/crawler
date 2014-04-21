package mo.umac.triangulation;

public class AdvancingFrontNode {
	protected AdvancingFrontNode next = null;
	protected AdvancingFrontNode prev = null;

	/**
	 * The x value of this node
	 */
	protected final Double key; // XXX: BST
	/**
	 * The x value of this node
	 */
	protected final double value;
	protected final Point point;
	protected DelaunayTriangle triangle;

	public AdvancingFrontNode(Point point) {
		this.point = point;
		value = point.getX();
		key = Double.valueOf(value); // XXX: BST
	}

	public AdvancingFrontNode getNext() {
		return next;
	}

	public AdvancingFrontNode getPrevious() {
		return prev;
	}

	public Point getPoint() {
		return point;
	}

	public DelaunayTriangle getTriangle() {
		return triangle;
	}

	public boolean hasNext() {
		return next != null;
	}

	public boolean hasPrevious() {
		return prev != null;
	}
}
