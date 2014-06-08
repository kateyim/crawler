package mo.umac.analytics;

import mo.umac.db.H2DB;

public class OperOnData {

	public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ny-prun";

	public final static String DB_NAME_SCALE = "../data-experiment/yahoo/ny-prun-scale";

	public double factor = 100;

	public static void main(String[] args) {
		OperOnData operation = new OperOnData();
		operation.scale();
	}

	public void scale() {
		H2DB h2 = new H2DB();
		// h2.scale(DB_NAME_SOURCE, DB_NAME_SCALE, factor);
		h2.printItemTable(DB_NAME_SCALE);
	}

}
