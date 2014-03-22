package mo.umac.crawler;

import java.util.LinkedList;
import java.util.List;

import mo.umac.uscensus.UScensusData;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import paint.PaintShapes;

public class Main {

	public static Logger logger = Logger.getLogger(Main.class.getName());

	public static String LOG_PROPERTY_PATH = "./log4j.xml";
	public static boolean debug = false;


	// used in offline algorithm
	public final static String DB_NAME_SOURCE = "../crawler-data/yahoolocal-h2/source/ok-prun";
	public final static String DB_NAME_TARGET = "../crawler-data/yahoolocal-h2/target/ok-prun-c-one";
	public final static String DB_NAME_CRAWL = "../crawler-data/yahoolocal-h2/crawl/datasets";

	public static void main(String[] args) {
		/************************* Change these lines *************************/
		initForServer(false);
		DOMConfigurator.configure(Main.LOG_PROPERTY_PATH);
		/************************* Crawling Algorithm ***************************/
		// CrawlerStrategy crawlerStrategy = new QuadTreeCrawler();
		Strategy crawlerStrategy = new AlgoSlice();
		// CrawlerStrategy crawlerStrategy = new BlockCrawler();
		/**********************************************************************/
		Context crawlerContext = new Context(crawlerStrategy);
		// specify the states to be crawled
		LinkedList<String> listNameStates = new LinkedList<String>();
		// if the listNameStates is empty, then crawl all states.
		// String city1 = "NY";
		// listNameStates.add(city1);
		// String city2 = "UT";
		// listNameStates.add(city2);
		String city3 = "OK";
		listNameStates.add(city3);
		List<String> listCategoryNames = new LinkedList<String>();
		// String category1 = "Hotels & Motels";
		// listCategoryNames.add(category1);
		String category2 = "Restaurants";
		listCategoryNames.add(category2);
		//
		PaintShapes.painting = false;
		// change top-k
		Strategy.MAX_TOTAL_RESULTS_RETURNED = 100;
		crawlerContext.callCrawling(listNameStates, listCategoryNames);
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
			Main.LOG_PROPERTY_PATH = "target/log4j.xml";
			UScensusData.STATE_SHP_FILE_NAME = "target/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
			UScensusData.STATE_DBF_FILE_NAME = "target/UScensus/tl_2012_us_state/tl_2012_us_state.dbf";
		} else {
			// for debugging, set the resources folder as
			// OnlineStrategy.PROPERTY_PATH = "./src/main/resources/crawler.properties";
			Strategy.CATEGORY_ID_PATH = "./src/main/resources/cat_id.txt";
			Main.LOG_PROPERTY_PATH = "./src/main/resources/log4j.xml";
			UScensusData.STATE_SHP_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
			UScensusData.STATE_SHP_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
		}
	}
	

}
