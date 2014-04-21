package mo.umac.triangulation;

import java.util.List;

public class AdvancingFront {
	public AdvancingFrontNode head;
	public AdvancingFrontNode tail;

	public AdvancingFront(AdvancingFrontNode head, AdvancingFrontNode tail) {
		this.head = head;
		this.tail = tail;
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

	/**
	 * We use a balancing tree to locate a node smaller or equal to
	 * given key value
	 * 
	 * @param x
	 * @return the left node on the advancing front
	 */
	public boolean locateNode(Point point, AdvancingFrontNode nodeLeft) {
		double x = point.getX();
		// search from head.
		AdvancingFrontNode node = head;
		// TODO check whether the x value are always increasing on the front line
		while (node.next != null) {
			if (x > node.value) {
				node = node.next;
			} else if (x == node.value) {
				nodeLeft = node;
				return true;
			} else {
				nodeLeft = node.prev;
				return false;
			}
			node = node.next;
		}
		nodeLeft = node;
		return false;
	}

	/**
	 * This implementation will use simple node traversal algorithm to find a point on the front
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
