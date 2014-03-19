package mo.umac.crawler.offline.yahoolocal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import mo.umac.db.yahoolocal.DBExternal;
import mo.umac.db.yahoolocal.DBInMemory;
import mo.umac.db.yahoolocal.H2DB;
import mo.umac.main.CrawlerStrategy;
import mo.umac.main.MainCrawlerYahooLocal;
import mo.umac.metadata.yahoolocal.APOI;
import mo.umac.metadata.yahoolocal.AQuery;
import mo.umac.metadata.yahoolocal.ResultSet;
import mo.umac.paint.PaintShapes;
import mo.umac.rtree.MyRTree;
import mo.umac.spatial.GeoOperator;
import mo.umac.utils.CommonUtils;
import mo.umac.utils.FileOperator;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * The off line algorithm is for testing different algorithms in the
 * experiments.
 * 
 * @author Kate
 * 
 */
public abstract class OfflineStrategy extends CrawlerStrategy {
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
		return CrawlerStrategy.dbInMemory.query(aQuery);
	}

	/**
	 * @param category
	 * @param state
	 */
	protected void prepareData(String category, String state) {
		//
		logger.info("preparing data...");
		CrawlerStrategy.categoryIDMap = FileOperator.readCategoryID(CATEGORY_ID_PATH);
		// source database
		CrawlerStrategy.dbExternal = new H2DB(MainCrawlerYahooLocal.DB_NAME_SOURCE, MainCrawlerYahooLocal.DB_NAME_TARGET);
		CrawlerStrategy.dbInMemory = new DBInMemory();
		// add at 2013-9-23
		CrawlerStrategy.dbInMemory.poisCrawledTimes = new HashMap<Integer, Integer>();
		CrawlerStrategy.dbInMemory.readFromExtenalDB(category, state);
		CrawlerStrategy.dbInMemory.index();
		logger.info("There are in total " + CrawlerStrategy.dbInMemory.pois.size() + " points.");
		// target database
		CrawlerStrategy.dbExternal.createTables(MainCrawlerYahooLocal.DB_NAME_TARGET);
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
				CrawlerStrategy.dbExternal.removeDuplicate();
				logger.info("begin updating the external db");
				CrawlerStrategy.dbInMemory.updataExternalDB();
				logger.info("end updating the external db");
				endData();
			}
		}

		/**************************************************************************/
		long after = System.currentTimeMillis();
		logger.info("Stop at: " + after);
		logger.info("time for crawling = " + (after - before) / 1000);
		//
		logger.info("countNumQueries = " + CrawlerStrategy.countNumQueries);
		logger.info("number of points crawled = " + CrawlerStrategy.dbInMemory.poisIDs.size());
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
