package mo.umac.crawler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mo.umac.db.DBExternal;
import mo.umac.db.DBInMemory;
import mo.umac.db.H2DB;

import org.apache.log4j.Logger;

import utils.CommonUtils;
import utils.FileOperator;

import com.vividsolutions.jts.geom.Envelope;

/**
 * The off line algorithm is for testing different algorithms in the
 * experiments.
 * 
 * @author Kate
 * 
 */
public abstract class OfflineStrategy extends Strategy {
	protected static Logger logger = Logger.getLogger(OfflineStrategy.class.getName());

	/**
	 * This is the crawling algorithm
	 */
	public abstract void crawl(String state, int category, String query, Envelope envelopeState);

	/**
	 * @param aQuery
	 * @return
	 */
	public static ResultSet query(AQuery aQuery) {
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
		logger.info("There are in total " + Strategy.dbInMemory.pois.size() + " points.");
		// target database
		Strategy.dbExternal.createTables(Main.DB_NAME_TARGET);
	}

	/*
	 * (non-Javadoc)
	 * @see mo.umac.crawler.YahooLocalCrawlerStrategy#endData()
	 * shut down the connection
	 */
	public static void endData() {
		DBExternal.distroyConn();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * mo.umac.crawler.YahooLocalCrawlerStrategy#crawlByCategoriesStates(java
	 * .util.LinkedList, java.util.List, java.util.LinkedList,
	 * java.util.HashMap)
	 */
	protected void crawlByCategoriesStates(LinkedList<Envelope> listEnvelopeStates, List<String> listCategoryNames, LinkedList<String> nameStates, HashMap<Integer, String> categoryIDMap) {

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
				Object searchingResult = CommonUtils.getKeyByValue(categoryIDMap, query);
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
					logger.error("Cannot find category id for query: " + query + " in categoryIDMap");
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
		logger.info("number of points crawled = " + Strategy.dbInMemory.poisIDs.size());
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
