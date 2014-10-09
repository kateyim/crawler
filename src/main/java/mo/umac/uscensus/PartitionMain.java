package mo.umac.uscensus;

import mo.umac.crawler.MainYahoo;

import org.apache.log4j.xml.DOMConfigurator;

public class PartitionMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean debug = false;
		MainYahoo.shutdownLogs(debug);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
		PartitionMain p = new PartitionMain();
		// p.partitionNY();

	}

	public void partitionNY() {
		USDensity usDensity = new USDensity();
		// computeDensityInEachGrids();
		// usDensity.forYahooNYOnlyDenses();
		usDensity.forYahooNYEmptyAndDenses();
	}

	public void partitionSynthetic() {
		USDensity usDensity = new USDensity();
		// computeDensityInEachGrids();
		usDensity.forYahooNYOnlyDenses();
		// forSkewedDB();
	}

	public void partitionOKUT() {
		
	}

}
