import com.vividsolutions.jts.triangulate.quadedge.QuadEdge;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Test.testRead();
		// generateArray();
		// sizeOf();
		testBreak();

	}
	
	public void testQE(){
		QuadEdge qe;
		com.vividsolutions.jts.triangulate.quadedge.QuadEdgeTriangle tri;
	}

	public static void testRead() {
		String s = "5.912360262131225E-4";
		double d = Double.parseDouble(s);
		System.out.println(d);
	}

	public static void generateArray() {
		int numX = 7986;
		int numY = 4539;
		double[][] arr = new double[numX][numY];
		for (int i = 0; i < numX; i++) {
			for (int j = 0; j < numY; j++) {
				arr[i][j] = Double.MAX_VALUE;
			}
		}
		System.out.println("generate array done!");
	}

	public static void sizeOf() {
		int i = Integer.MAX_VALUE;
		int j = Integer.MAX_VALUE;
		double d = Double.MAX_VALUE;

		Runtime.getRuntime().gc();

		long before = Runtime.getRuntime().freeMemory();
		// Grid grids = new Grid(i, j, d, Grid.Flag.UNVISITED);
		long after = Runtime.getRuntime().freeMemory();

		System.out.println("Memory used:" + (before - after));
	}

	public static void testBreak() {
		int i = 0;
		while (true) {
			if (i == 0) {
				i++;
				if (i == 1) {
					i++;
					if (i == 2) {
						break;
					}
					System.out.println("inner if 2");
				}
				System.out.println("inner if 1");
			}
			System.out.println("inner while");
		}
		System.out.println("outside");
	}

}
