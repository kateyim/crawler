package mo.umac.crawler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mo.umac.db.DBExternal;
import mo.umac.db.DBInMemory;
import mo.umac.db.H2DB;
import mo.umac.metadata.AQuery;
import mo.umac.metadata.ResultSetD2;
import mo.umac.uscensus.UScensusData;
import myrtree.MyRTree;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import utils.CommonUtils;
import utils.FileOperator;

import com.vividsolutions.jts.geom.Envelope;

public abstract class Strategy {

	protected static Logger logger = Logger.getLogger(Strategy.class.getName());

	/**
	 * The path of the category ids of Yahoo! Local
	 */
	public static String CATEGORY_ID_PATH = "./src/main/resources/cat_id.txt";

	/**
	 * The maximum number of returned results by a query.
	 */
	protected final static int MAX_RESULTS_NUM = 20;
	/**
	 * The maximum starting result position to return.
	 */
	protected final static int MAX_START = 250;

	/**
	 * The maximum number of results on can get through this query by only
	 * changing the start value.
	 */
	protected static int MAX_TOTAL_RESULTS_RETURNED;// = MAX_START +
													// MAX_RESULTS_NUM; // =270;

	public static int countNumQueries = 1;

	protected int zip = 0;

	public static DBInMemory dbInMemory;

	public static DBExternal dbExternal;

	public static final double EPSILON = 0.00000001;

	public static HashMap<Integer, String> categoryIDMap;

	/**
	 * The index for all covered rectangles
	 */
	public static MyRTree rtreeRectangles = new MyRTree();

	public static int rectangleId = 0;

	/**
	 * This is the crawling algorithm
	 */
	protected abstract void crawl(String state, int category, String query,
			Envelope envelopeState);

	/**
	 * Entrance of the crawler
	 * 
	 * @param listNameStates
	 * @param listCategoryNames
	 */
	protected void callCrawling(LinkedList<String> listNameStates,
			List<String> listCategoryNames) {

		LinkedList<Envelope> listEnvelopeStates = selectEnvelopes(listNameStates);
		if (listNameStates.size() == 0) {
			listNameStates = (LinkedList<String>) UScensusData
					.stateName(UScensusData.STATE_DBF_FILE_NAME);
		}
		HashMap<Integer, String> categoryIDMap = FileOperator
				.readCategoryID(CATEGORY_ID_PATH);
		crawlByCategoriesStates(listEnvelopeStates, listCategoryNames,
				listNameStates, categoryIDMap);
	}

	/**
	 * Select the envelope information from UScensus data, if listNameStates is
	 * empty, then return all envelopes in the U.S.
	 * 
	 * @param listNameStates
	 * @return
	 */
	private LinkedList<Envelope> selectEnvelopes(
			LinkedList<String> listNameStates) {
		// State's information provided by UScensus
		LinkedList<Envelope> allEnvelopeStates = (LinkedList<Envelope>) UScensusData
				.MBR(UScensusData.STATE_SHP_FILE_NAME);
		LinkedList<String> allNameStates = (LinkedList<String>) UScensusData
				.stateName(UScensusData.STATE_DBF_FILE_NAME);

		LinkedList<Envelope> listEnvelopeStates = new LinkedList<Envelope>();

		// select the specified states according to the listNameStates
		for (int i = 0; i < listNameStates.size(); i++) {
			String specifiedName = listNameStates.get(i);
			for (int j = 0; j < allNameStates.size(); j++) {
				String name = allNameStates.get(j);
				if (name.equals(specifiedName)) {
					listEnvelopeStates.add(allEnvelopeStates.get(j));
				}
			}
		}
		// revised at 2013-09-10
		if (listNameStates.size() == 0) {
			listEnvelopeStates = allEnvelopeStates;
		}
		return listEnvelopeStates;
	}

	protected boolean covered(Envelope envelopeStateECEF) {
		return Strategy.rtreeRectangles.contains(envelopeStateECEF);
	}

	/**
	 * @param aQuery
	 * @return
	 */
	protected static ResultSetD2 query(AQuery aQuery) {
		return Strategy.dbInMemory.query(aQuery);
	}

	/**
	 * @param category
	 * @param state
	 */
	protected void prepareData(String category, String state) {
		//
		logger.info("preparing data...");
		Strategy.categoryIDMap = FileOperator.readCategoryID(CATEGORY_ID_PATH);
		// source database
		Strategy.dbExternal = new H2DB(Main.DB_NAME_SOURCE, Main.DB_NAME_TARGET);
		Strategy.dbInMemory = new DBInMemory();
		// add at 2013-9-23
		Strategy.dbInMemory.poisCrawledTimes = new HashMap<Integer, Integer>();
		Strategy.dbInMemory.readFromExtenalDB(category, state);
		Strategy.dbInMemory.index();
		logger.info("There are in total " + Strategy.dbInMemory.pois.size()
				+ " points.");
		// target database
		Strategy.dbExternal.createTables(Main.DB_NAME_TARGET);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mo.umac.crawler.YahooLocalCrawlerStrategy#endData() shut down the
	 * connection
	 */
	protected static void endData() {
		DBExternal.distroyConn();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mo.umac.crawler.YahooLocalCrawlerStrategy#crawlByCategoriesStates(java
	 * .util.LinkedList, java.util.List, java.util.LinkedList,
	 * java.util.HashMap)
	 */
	protected void crawlByCategoriesStates(
			LinkedList<Envelope> listEnvelopeStates,
			List<String> listCategoryNames, LinkedList<String> nameStates,
			HashMap<Integer, String> categoryIDMap) {

		long before = System.currentTimeMillis();
		logger.info("Start at : " + before);

		for (int i = 0; i < nameStates.size(); i++) {
			String state = nameStates.get(i);
			logger.info("crawling in the state: " + state);
			for (int j = 0; j < listCategoryNames.size(); j++) {
				String query = listCategoryNames.get(j);
				logger.info("crawling the category: " + query);

				// load data from the external dataset
				prepareData(query, state);

				// initial category
				int category = -1;
				Object searchingResult = CommonUtils.getKeyByValue(
						categoryIDMap, query);
				if (searchingResult != null) {
					category = (Integer) searchingResult;
					//
					Envelope envelopeStateLLA = listEnvelopeStates.get(i);
					// Envelope envelopeStateECEF =
					// GeoOperator.lla2ecef(envelopeStateLLA);
					if (logger.isDebugEnabled()) {
						logger.debug(envelopeStateLLA.toString());
						// logger.debug(envelopeStateECEF.toString());
					}
					// crawl(state, category, query, envelopeStateECEF);
					crawl(state, category, query, envelopeStateLLA);
					//
				} else {
					logger.error("Cannot find category id for query: " + query
							+ " in categoryIDMap");
				}
				logger.info("removing duplicate records in the external db");
				Strategy.dbExternal.removeDuplicate();
				logger.info("begin updating the external db");
				Strategy.dbInMemory.updataExternalDB();
				logger.info("end updating the external db");
				endData();
			}
		}

		/**************************************************************************/
		long after = System.currentTimeMillis();
		logger.info("Stop at: " + after);
		logger.info("time for crawling = " + (after - before) / 1000);
		//
		logger.info("countNumQueries = " + Strategy.countNumQueries);
		logger.info("countCrawledPoints = "
				+ Strategy.dbInMemory.poisIDs.size());
		logger.info("Finished ! Oh ! Yeah! ");

		// logger.info("poisCrawledTimes:");
		// Iterator it1 =
		// CrawlerStrategy.dbInMemory.poisCrawledTimes.entrySet().iterator();
		// while (it1.hasNext()) {
		// Entry entry = (Entry) it1.next();
		// int poiID = (Integer) entry.getKey();
		// int times = (Integer) entry.getValue();
		// APOI aPOI = CrawlerStrategy.dbInMemory.pois.get(poiID);
		// double longitude = aPOI.getCoordinate().x;
		// double latitude = aPOI.getCoordinate().y;
		// logger.info(poiID + ": " + times + ", " + "[" + longitude + ", " +
		// latitude + "]");
		// }
		// delete
		// Set set = CrawlerStrategy.dbInMemory.poisIDs;
		// Iterator<Integer> it = set.iterator();
		// while (it.hasNext()) {
		// int id = it.next();
		// logger.info(id);
		// }

	}

}
