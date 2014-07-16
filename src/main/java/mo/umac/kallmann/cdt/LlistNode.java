package mo.umac.kallmann.cdt;

public class LlistNode<T> {
	private T data;
	private LlistNode<T> next;
	private LlistNode<T> prev;

	public LlistNode() {
		data = null;
		next = prev = this;
	}

	/**
	 * insert node n after this node
	 * 
	 * @param d
	 * @param n
	 */
	public LlistNode(T d, LlistNode<T> n) {
		this.data = d;
		this.next = n;
		this.prev = n.getPrev();
		n.setPrev(this);
	}
	
	public LlistNode(T d) {
		this.data = d;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public LlistNode<T> getNext() {
		return next;
	}

	public void setNext(LlistNode<T> next) {
		this.next = next;
	}

	public LlistNode<T> getPrev() {
		return prev;
	}

	public void setPrev(LlistNode<T> prev) {
		this.prev = prev;
	}

}
