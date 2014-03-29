public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Test.testRead();
//		generateArray();
		sizeOf();

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
//	    Grid grids = new Grid(i, j, d, Grid.Flag.UNVISITED);
	    long after = Runtime.getRuntime().freeMemory();

	    System.out.println("Memory used:"+(before-after));
	}

}
