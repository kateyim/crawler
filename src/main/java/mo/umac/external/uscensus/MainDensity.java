package mo.umac.external.uscensus;

import mo.umac.crawler.MainCrawler;

import org.apache.log4j.xml.DOMConfigurator;

import com.vividsolutions.jts.geom.Envelope;

public class MainDensity {

	// yanhui
	// NY: Env[-79.76259 : -71.777491, 40.477399 : 45.015865]
	public static final double NYLatitudeMin = 40.477399;
	public static final double NYLatitudeMax = 45.015865;
	public static final double NYLongitudeMin = -79.76259;
	public static final double NYLongitudeMax = -71.777491;

	// OK: // FIXME change these values
	public static final double OKLatitudeMin = 40.477399;
	public static final double OKLatitudeMax = 45.015865;
	public static final double OKLongitudeMin = -79.76259;
	public static final double OKLongitudeMax = -71.777491;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure(MainCrawler.LOG_PROPERTY_PATH);
		// for NY
		Envelope envelope = new Envelope(NYLongitudeMin, NYLongitudeMax, NYLatitudeMin, NYLatitudeMax);
		String zipFolderPath = "../data-map/us-road/new-york/";
		String unZipfolderPath = "../data-map/us-road/new-york-upzip/";
		String densityFile = zipFolderPath + "densityMap.txt";
		double granularityX = 0.08;
		double granularityY = 0.04;
		String combinedFile = zipFolderPath + "combinedDensity.txt";

		// TODO add crawler
//		USDensity.densityMap(envelope, granularityX, granularityY, zipFolderPath, unZipfolderPath, densityFile);
		USDensity.combineDensityMap(densityFile, combinedFile);
	}

}
