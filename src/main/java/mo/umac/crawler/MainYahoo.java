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
	public static boolean debug = false;

	// used in offline algorithm
	// public final static String DB_NAME_SOURCE =
	// "../crawler-data/yahoolocal-h2/source/ny-prun";
	// public final static String DB_NAME_TARGET =
	// "../crawler-data/yahoolocal-h2/target/ny-prun-c-one";
	// public final static String DB_NAME_CRAWL =
	// "../crawler-data/yahoolocal-h2/crawl/datasets";

	public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ny-prun";
	// public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ny-prun-scale";
	public final static String DB_NAME_TARGET = "../data-experiment/ny-prun-c-one";
	public final static String DB_NAME_CRAWL = "../data-experiment/datasets";

	private static int topK = 100;

	/******** NY ********/
	// private static Envelope envelope = new Envelope(-79.76259, -71.777491, 40.477399, 45.015865);
	// public static double factor = 100;
	// private static Envelope envelope = new Envelope(-79.76259 * factor * (-1), -71.777491 * factor * (-1), 40.477399 * factor, 45.015865 * factor);
	private static Envelope envelope = new Envelope(-79.76259, -71.777491, 40.477399, 45.015865);
	private static Coordinate outerPointNY = new Coordinate(-100, -100);
	private static String category = "Restaurants";
	private static String state = "NY";
	private static int categoryID = 96926236;

	// public static void main(String[] args) {
	// /************************* Change these lines *************************/
	// debug = false;
	// initForServer(false);
	// DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
	// shutdownLogs(MainYahoo.debug);
	// /************************* Crawling Algorithm ***************************/
	// // Strategy crawlerStrategy = new AlgoSlice();
	// // Strategy crawlerStrategy = new AlgoPartition();
	// // AlgoPartition.clusterRegionFile = USDensity.clusterRegionFile;
	// Strategy crawlerStrategy = new AlgoDCDT();
	// /**********************************************************************/
	// Context crawlerContext = new Context(crawlerStrategy);
	// // specify the states to be crawled
	// LinkedList<String> listNameStates = new LinkedList<String>();
	// // if the listNameStates is empty, then crawl all states.
	// String city1 = "NY";
	// listNameStates.add(city1);
	// // String city2 = "UT";
	// // listNameStates.add(city2);
	// // String city3 = "OK";
	// // listNameStates.add(city3);
	// //
	// List<String> listCategoryNames = new LinkedList<String>();
	// // String category1 = "Hotels & Motels";
	// // listCategoryNames.add(category1);
	// String category2 = "Restaurants";
	// listCategoryNames.add(category2);
	// //
	// PaintShapes.painting = true;
	// // change top-k
	// Strategy.MAX_TOTAL_RESULTS_RETURNED = 100;
	// crawlerContext.callCrawling(listNameStates, listCategoryNames);
	// }

	public static void main(String[] args) {
		/************************* Change these lines *************************/
		debug = false;
		PaintShapes.painting = false;
		initForServer(false);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
		shutdownLogs(MainYahoo.debug);
		/************************* Crawling Algorithm ***************************/
		// Strategy crawlerStrategy = new AlgoSlice();
		// Strategy crawlerStrategy = new AlgoPartition();
		// AlgoPartition.clusterRegionFile = USDensity.clusterRegionFile;
		Strategy crawlerStrategy = new AlgoDCDT();
		AlgoDCDT.outerPoint = outerPointNY;
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
			UScensusData.STATE_SHP_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
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
