package mo.umac.external.uscensus;

import java.util.ArrayList;

import mo.umac.crawler.MainCrawler;

import org.apache.log4j.xml.DOMConfigurator;

import com.vividsolutions.jts.geom.Coordinate;
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

		// compute the density on the map, run only once for a state folder
		// ArrayList<Coordinate[]> roadList = UScensusData.readRoad(zipFolderPath, unZipfolderPath);
		// double[][] density = USDensity.densityList(envelope, granularityX, granularityY, roadList);
		// USDensity.writeDensityToFile(density, densityFile);
		//
		// cluster the regions, and then write to file
		ArrayList<double[]> density = USDensity.readDensityFromFile(densityFile);
		String clusterRegionFile = zipFolderPath + "combinedDensity.txt";
		ArrayList<Envelope> clusteredRegion = USDensity.combineDensityMap(density, granularityX, granularityY, 0.5);
		USDensity.writePartition(clusterRegionFile, clusteredRegion);
	}

}