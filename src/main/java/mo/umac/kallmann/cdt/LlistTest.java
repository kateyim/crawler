package mo.umac.kallmann.cdt;

public class LlistTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LlistTest test = new LlistTest();
		test.test();
	}

	public void test(){
		Llist list = new Llist<String>();
		
		if(list.empty()) {
			System.out.println("empty");
		}
		
		list.insert(list.first(), "1");
		list.insert(list.first(), "2");
		list.insert(list.first(), "3");
		
		LlistNode p;
		for (p = list.first(); !list.isEnd(p); p = list.next(p)) {
			String s = ((String) list.retrieve(p));
			System.out.println(s);
		}
		
		System.out.println("length = " + list.length());
		
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
//		LlistNode q = list.first(); 
//		list.remove(q);
		list.remove(p);
		
		System.out.println("After removing...");
		System.out.println("length = " + list.length());
		for (/*LlistNode*/ p = list.first(); !list.isEnd(p); p = list.next(p)) {
			String s = ((String) list.retrieve(p));
			System.out.println(s);
		}

	}
	
}
