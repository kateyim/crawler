package mo.umac.crawler;

import java.util.ArrayList;
import java.util.HashMap;

import mo.umac.db.DBInMemory;
import mo.umac.metadata.APOI;
import mo.umac.uscensus.USDensity;
import mo.umac.uscensus.UScensusData;
import myrtree.MyRTree;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import paint.PaintShapes;
import utils.FileOperator;

public class MainYahoo {

	public static Logger logger = Logger.getLogger(MainYahoo.class.getName());

	public static String LOG_PROPERTY_PATH = "./log4j.xml";

	public final static String DB_NAME_TARGET = "../data-experiment/target";
	public final static String DB_NAME_CRAWL = "../data-experiment/datasets";

	public static boolean debug = false;
	private static int topK = 100;

	private static Coordinate outerPoint = new Coordinate(-100, -100);
	/******** synthetic dataset ********/
	// public final static String DB_NAME_SOURCE = "../crawler-data/yahoolocal-h2/source/ny-prun";
	/******** NY ********/
	private static Envelope envelope = new Envelope(-79.76259, -71.777491, 40.477399, 45.015865);
	private static String category = "Restaurants";
	private static String state = "NY";
	private static int categoryID = 96926236;
	public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ny-prun-4";

	/******** UT ********/
	// private static Envelope envelope = new Envelope(-114.052998, -109.04105799999999, 36.997949, 42.001618);
	// private static String category = "Restaurants";
	// private static String state = "UT";
	// private static int categoryID = 96926236;
	// public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ut-prun";

	/******** OK ********/
	// private static Envelope envelope = new Envelope(-103.002455, -94.430662, 33.615787, 37.002311999999996);
	// private static String category = "Restaurants";
	// private static String state = "OK";
	// private static int categoryID = 96926236;
	// public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ok-prun-4";

	public static void main(String[] args) {
		/************************* Change these lines *************************/
		debug = false;
		PaintShapes.painting = false;
		initForServer(false);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
		shutdownLogs(MainYahoo.debug);
		/************************* Crawling Algorithm ***************************/
		// // Strategy crawlerStrategy = new AlgoSlice();
		// Strategy crawlerStrategy = new AlgoProjection();
		// Strategy crawlerStrategy = new AlgoPartition();
		// AlgoPartition.clusterRegionFile = USDensity.clusterRegionFile;
		// // Strategy crawlerStrategy = new AlgoDCDT();
		// // AlgoDCDT.outerPoint = outerPointNY;
		// //
		// Context crawlerContext = new Context(crawlerStrategy);
		// /**********************************************************************/
		// Strategy.MAX_TOTAL_RESULTS_RETURNED = topK;
		// crawlerContext.callCrawlingSingle(state, categoryID, category, envelope);
		/************************* Testing Parameters ***************************/
		testingNY();
		// testingUTOK();
		// debuggingTestNY();
		// nyPartition();
		Strategy.endData();
	}

	public static void nyPartition() {
		logger.info("begin testing parameters in partition");
		Envelope envelopeNY = USDensity.envelopeNY;
		String unZipFolderPath = USDensity.UN_ZIP_FOLDER_PATH;
		double granularityX = 0;
		double granularityY = 0;
		ArrayList<Envelope> mbrs;
		AlgoPartition.clusterRegionFile = null;
		//
		double granularity = 0.02;
		double alpha = 0.3;
		int numDense = 37;
		//
		// double granularity = 0.5;
		// double alpha = 0.3;
		// int numDense = 2;
		//
		granularityX = granularity;
		granularityY = granularity;
		// 1. grids
		double[][] densityAll = USDensity.computeDensityInEachGridsByRoads(unZipFolderPath, envelopeNY, granularityX, granularityY);

		// 2. partitioned mbrs
		mbrs = USDensity.partitionBasedOnDenseBefore(numDense, alpha, densityAll, envelopeNY, granularityX, granularityY);
		// debug
		for (int j = 0; j < mbrs.size(); j++) {
			Envelope e = mbrs.get(j);
			System.out.println(USDensity.partitionToString(e));
		}
		//
		// 3. calling the algorithm
		Strategy crawlerStrategy = new AlgoPartition();
		Strategy.MAX_TOTAL_RESULTS_RETURNED = topK;
		AlgoPartition.mbrList = mbrs;
		Context crawlerContext = new Context(crawlerStrategy);
		crawlerContext.callCrawlingSingle(state, categoryID, category, envelopeNY);
	}

	/**
	 * Testing parameters for partitioning the NY
	 * 
	 */
	public static void testingNY() {
		logger.info("begin testing parameters in partition");
		Envelope envelopeNY = USDensity.envelopeNY;
		String unZipFolderPath = USDensity.UN_ZIP_FOLDER_PATH;
		ArrayList<Envelope> mbrs;
		AlgoPartition.clusterRegionFile = null;
		int currentCount = 0;
		//
		int minCount = Integer.MAX_VALUE;
		double minCopies = Double.MAX_VALUE;
		double minAlpha = Double.MAX_VALUE;
		int minNumDense = Integer.MAX_VALUE;
		//
		for (double copies = 0.1; copies <= 0.1; copies = copies + 0.01) {
			logger.info("copies = " + copies);
			// 1. grids
			double[][] densityAll = USDensity.computeDensityInEachGridsByRoads(unZipFolderPath, envelopeNY, copies);

			for (double alpha = 0.3; alpha <= 0.3; alpha = alpha + 0.1) {
				logger.info("alpha = " + alpha);
//				for (int numDense = 37; numDense <= 37; numDense++) { // later I should eliminate this parameter
					// logger.info("numDense = " + numDense);
					// 2. partitioned mbrs
					mbrs = USDensity.partitionBasedOnDense(alpha, densityAll, envelopeNY, copies);
					logger.info("mbrs.size() = " + mbrs.size());
					// debug
					// for (int j = 0; j < mbrs.size(); j++) {
					// Envelope e = mbrs.get(j);
					// System.out.println(USDensity.partitionToString(e));
					// }
					//
					// 3. calling the algorithm
//					Strategy crawlerStrategy = new AlgoPartition();
//					AlgoPartition.mbrList = mbrs;
//					//
//					Context crawlerContext = new Context(crawlerStrategy);
//					Strategy.MAX_TOTAL_RESULTS_RETURNED = topK;
//					currentCount = crawlerContext.callCrawlingSingle(state, categoryID, category, envelopeNY);
//					if (currentCount < minCount) {
//						minCount = currentCount;
//						minCopies = copies;
//						minAlpha = alpha;
//						minNumDense = mbrs.size();
//					}
//				}
			}
		}
		logger.info("-----------------------------");
		logger.info("minCount = " + minCount);
		logger.info("minCopies = " + minCopies);
		logger.info("minAlpha = " + minAlpha);
		logger.info("minNumDense = " + minNumDense);
	}

	public static void testingUTOK() {
		logger.info("begin testing parameters in partition");
		double granularityX = 0;
		double granularityY = 0;
		ArrayList<Envelope> mbrs;
		AlgoPartition.clusterRegionFile = null;
		int currentCount = 0;
		//
		int minCount = Integer.MAX_VALUE;
		double minGranularity = Double.MAX_VALUE;
		double minAlpha = Double.MAX_VALUE;
		int minNumDense = Integer.MAX_VALUE;
		//
		Strategy crawlerStrategy = new AlgoPartition();
		// Strategy crawlerStrategy = new AlgoProjection();
		// Strategy crawlerStrategy = new AlgoDCDT();
		// AlgoDCDT.outerPoint = outerPoint;
		crawlerStrategy.prepareData(category, state);
		HashMap<Integer, APOI> pois = Strategy.dbInMemory.pois;
		//
		for (double granularity = 0.06; granularity <= 0.6; granularity = granularity + 0.01) {
			logger.info("granularity = " + granularity);
			granularityX = granularity;
			granularityY = granularity;
			// 1. grids
			double[][] densityAll = USDensity.computeDensityInEachGridsByPoints(pois, envelope, granularityX, granularityY);

			for (double alpha = 0.3; alpha <= 0.3; alpha = alpha + 0.1) {
				logger.info("alpha = " + alpha);
				for (int numDense = 5; numDense <= 5; numDense++) {
					logger.info("numDense = " + numDense);
					// 2. partitioned mbrs
					mbrs = USDensity.partitionBasedOnDenseBefore(numDense, alpha, densityAll, envelope, granularityX, granularityY);
					// debug
					for (int j = 0; j < mbrs.size(); j++) {
						Envelope e = mbrs.get(j);
						System.out.println(USDensity.partitionToString(e));
					}
					//
					// 3. calling the algorithm
					// Strategy crawlerStrategy = new AlgoPartition();
					AlgoPartition.mbrList = mbrs;
					//
					Context crawlerContext = new Context(crawlerStrategy);
					Strategy.MAX_TOTAL_RESULTS_RETURNED = topK;
					currentCount = crawlerContext.callCrawlingSingle(state, categoryID, category, envelope);
					if (currentCount < minCount) {
						minCount = currentCount;
						minGranularity = granularity;
						minAlpha = alpha;
						minNumDense = numDense;
					}
				}
			}
		}
		logger.info("-----------------------------");
		logger.info("minCount = " + minCount);
		logger.info("minGranularity = " + minGranularity);
		logger.info("minAlpha = " + minAlpha);
		logger.info("minNumDense = " + minNumDense);
	}

	public static void debuggingTestNY() {
		logger.info("begin testing parameters in partition");
		Envelope envelopeNY = USDensity.envelopeNY;
		String unZipFolderPath = USDensity.UN_ZIP_FOLDER_PATH;
		double granularityX = 0;
		double granularityY = 0;
		ArrayList<Envelope> mbrs = new ArrayList<Envelope>();
		AlgoPartition.clusterRegionFile = null;
		int currentCount = 0;
		//
		//
		double granularity = 0.02;
		granularityX = granularity;
		granularityY = granularity;
		// 1. grids
		double[][] densityAll = USDensity.computeDensityInEachGridsByRoads(unZipFolderPath, envelopeNY, granularityX, granularityY);
		double alpha = 0.5;
		int numDense = 2;
		logger.info("numDense = " + numDense);
		// 2. partitioned mbrs
		mbrs = USDensity.partitionBasedOnDenseBefore(numDense, alpha, densityAll, envelopeNY, granularityX, granularityY);
		// debug
		for (int j = 0; j < mbrs.size(); j++) {
			Envelope e = mbrs.get(j);
			System.out.println(USDensity.partitionToString(e));
		}
		// Strategy crawlerStrategy = new AlgoPartition();
		// AlgoPartition.mbrList = mbrs;
		// Context crawlerContext = new Context(crawlerStrategy);
		// Strategy.MAX_TOTAL_RESULTS_RETURNED = topK;
		// currentCount = crawlerContext.callCrawlingSingle(state, categoryID, category, envelopeNY);
	}

	/**
	 * If packaging, then changing the destiny of paths of the configure files
	 * 
	 * @param packaging
	 */
	public static void initForServer(boolean packaging) {
		if (packaging) {
			// for packaging, set the resources folder as
			// OnlineStrategy.PROPERTY_PATH = "target/crawler.properties";
			Strategy.CATEGORY_ID_PATH = "target/cat_id.txt";
			MainYahoo.LOG_PROPERTY_PATH = "target/log4j.xml";
			UScensusData.STATE_SHP_FILE_NAME = "target/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
			UScensusData.STATE_DBF_FILE_NAME = "target/UScensus/tl_2012_us_state/tl_2012_us_state.dbf";
		} else {
			// for debugging, set the resources folder as
			// OnlineStrategy.PROPERTY_PATH =
			// "./src/main/resources/crawler.properties";
			Strategy.CATEGORY_ID_PATH = "./src/main/resources/cat_id.txt";
			// Main.LOG_PROPERTY_PATH = "./src/main/resources/log4j.xml";
			MainYahoo.LOG_PROPERTY_PATH = "./log4j.xml";
			UScensusData.STATE_SHP_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
			UScensusData.STATE_DBF_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.dbf";
		}
	}

	public static void shutdownLogs(boolean debug) {
		if (!debug) {
			Strategy.logger.setLevel(Level.INFO);
			DBInMemory.logger.setLevel(Level.INFO);
			MyRTree.logger.setLevel(Level.INFO);
			USDensity.logger.setLevel(Level.INFO);
		} else {
			Strategy.logger.setLevel(Level.DEBUG);
			DBInMemory.logger.setLevel(Level.DEBUG);
			MyRTree.logger.setLevel(Level.DEBUG);
			USDensity.logger.setLevel(Level.DEBUG);
		}
	}

}
