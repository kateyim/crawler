package mo.umac.kallmann.cdt;

import org.apache.log4j.Logger;

/**
 * Double circular linked list
 * 
 * @author Kate
 *
 * @param <T>
 */
public class Llist<T> {
	
	protected static Logger logger = Logger.getLogger(Llist.class.getName());


	private int len;
	
	/**
	 * The first node
	 */
	private LlistNode<T> head;

	public Llist() {
		head = new LlistNode<T>(); 
		len = 0; 
	}

	public boolean empty() {
		return head == head.getNext();
	}

	public boolean isEnd(LlistNode<T> p) {
		return p.getNext() == head;
	}

	public LlistNode<T> first() {
		return head;
	}

	public LlistNode<T> next(LlistNode<T> p) {
		return p.getNext();
	}

	public LlistNode<T> prev(LlistNode<T> p) {
		return p.getPrev();
	}

	/**
	 * @param p
	 * @return
	 */
	public T retrieve(LlistNode<T> p) {
		return (T) p.getNext().getData();
	}

	public void store(LlistNode<T> p, T d) {
		p.getNext().setData(d);
	}

	public int length() {
		return len;
	}

	/**
	 * Insert a node with value d after the node p
	 * 
	 * @param p
	 * @param d
	 * @return
	 */
	public LlistNode<T> insert(LlistNode<T> p, T d) {
		len++;
		LlistNode<T> n = new LlistNode<T>(d, p.getNext());
		p.setNext(n);
		return p.getNext();
	}

	/**
	 * Delete the next node after p
	 * 
	 * @param p
	 */
	public void remove(LlistNode<T> p)
	{
		LlistNode<T> t = p.getNext();
		p.setNext(t.getNext());
		t.getNext().setPrev(t.getPrev());
		len--;
	}
	
}
