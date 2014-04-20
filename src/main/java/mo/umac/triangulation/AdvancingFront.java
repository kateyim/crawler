package mo.umac.triangulation;

public class AdvancingFront {
	public AdvancingFrontNode head;
	public AdvancingFrontNode tail;
	protected AdvancingFrontNode search;

	public AdvancingFront(AdvancingFrontNode head, AdvancingFrontNode tail) {
		this.head = head;
		this.tail = tail;
		this.search = head;
		addNode(head);
		addNode(tail);
	}

	public void addNode(AdvancingFrontNode node) {
		// _searchTree.put( node.key, node );
	}

	public void removeNode(AdvancingFrontNode node) {
		// _searchTree.delete( node.key );
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		AdvancingFrontNode node = head;
		while (node != tail) {
			sb.append(node.point.getX()).append("->");
			node = node.next;
		}
		sb.append(tail.point.getX());
		return sb.toString();
	}

	private final AdvancingFrontNode findSearchNode(double x) {
		// TODO: implement BST index
		return search;
	}

	/**
	 * We use a balancing tree to locate a node smaller or equal to
	 * given key value
	 * 
	 * @param x
	 * @return
	 */
	public AdvancingFrontNode locateNode(Point point) {
		return locateNode(point.getX());
	}

	private AdvancingFrontNode locateNode(double x) {
		AdvancingFrontNode node = findSearchNode(x);
		if (x < node.value) {
			while ((node = node.prev) != null) {
				if (x >= node.value) {
					search = node;
					return node;
				}
			}
		} else {
			while ((node = node.next) != null) {
				if (x < node.value) {
					search = node.prev;
					return node.prev;
				}
			}
		}
		return null;
	}

	/**
	 * This implementation will use simple node traversal algorithm to find
	 * a point on the front
	 * 
	 * @param point
	 * @return
	 */
	public AdvancingFrontNode locatePoint(final Point point) {
		final double px = point.getX();
		AdvancingFrontNode node = findSearchNode(px);
		final double nx = node.point.getX();

		if (px == nx) {
			if (point != node.point) {
				// We might have two nodes with same x value for a short time
				if (point == node.prev.point) {
					node = node.prev;
				} else if (point == node.next.point) {
					node = node.next;
				} else {
					throw new RuntimeException("Failed to find Node for given afront point");
					// node = null;
				}
			}
		} else if (px < nx) {
			while ((node = node.prev) != null) {
				if (point == node.point) {
					break;
				}
			}
		} else {
			while ((node = node.next) != null) {
				if (point == node.point) {
					break;
				}
			}
		}
		search = node;
		return node;
	}
}
