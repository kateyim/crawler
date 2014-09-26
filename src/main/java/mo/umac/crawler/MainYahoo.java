package mo.umac.crawler;

import mo.umac.db.DBInMemory;
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

	/******** synthetic dataset ********/
	// public final static String DB_NAME_SOURCE = "../crawler-data/yahoolocal-h2/source/ny-prun";
	/******** NY ********/
	 private static Envelope envelope = new Envelope(-79.76259, -71.777491, 40.477399, 45.015865);
	 private static Coordinate outerPointNY = new Coordinate(-100, -100);
	 private static String category = "Restaurants";
	 private static String state = "NY";
	 private static int categoryID = 96926236;
	 public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ny-prun";

	/******** UT ********/
//	private static Envelope envelope = new Envelope(-114.052998, -109.04105799999999, 36.997949, 42.001618);
//	private static Coordinate outerPoint = new Coordinate(-100, -100);
//	private static String category = "Restaurants";
//	private static String state = "UT";
//	private static int categoryID = 96926236;
//	public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ut-prun-4";

	/******** OK ********/
	// private static Envelope envelope = new Envelope(-103.002455, -94.430662, 33.615787 , 37.002311999999996);
	// private static Coordinate outerPoint = new Coordinate(-100, -100);
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
		// Strategy crawlerStrategy = new AlgoSlice();
		Strategy crawlerStrategy = new AlgoPartition();
		AlgoPartition.clusterRegionFile = USDensity.clusterRegionFile;
		// Strategy crawlerStrategy = new AlgoDCDT();
		// AlgoDCDT.outerPoint = outerPointNY;
		//
		Context crawlerContext = new Context(crawlerStrategy);
		/**********************************************************************/
		Strategy.MAX_TOTAL_RESULTS_RETURNED = topK;
		crawlerContext.callCrawlingSingle(state, categoryID, category, envelope);
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
