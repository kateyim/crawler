package mo.umac.crawler.offline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mo.umac.crawler.CrawlerStrategy;
import mo.umac.db.DBExternal;
import mo.umac.db.DBInMemory;
import mo.umac.db.H2DB;
import mo.umac.metadata.AQuery;
import mo.umac.metadata.ResultSet;
import mo.umac.spatial.GeoOperator;
import mo.umac.utils.CommonUtils;
import mo.umac.utils.FileOperator;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Envelope;

/**
 * The off line algorithm is for testing different algorithms in the
 * experiments.
 * 
 * @author Kate
 * 
 */
public abstract class OfflineStrategy extends CrawlerStrategy {
    protected static Logger logger = Logger.getLogger(OfflineStrategy.class
	    .getName());

    /**
     * This is the crawling algorithm
     */
    public abstract void crawl(String state, int category, String query,
	    Envelope envelopeState);

    /**
     * @param aQuery
     * @return
     */
    public static ResultSet query(AQuery aQuery) {
	return CrawlerStrategy.dbInMemory.query(aQuery);
    }

    protected void prepareData(String category, String state) {
	// 
	CrawlerStrategy.categoryIDMap = FileOperator
		.readCategoryID(CATEGORY_ID_PATH);
	// source database
	CrawlerStrategy.dbExternal = new H2DB(H2DB.DB_NAME_SOURCE,
		H2DB.DB_NAME_TARGET);
	CrawlerStrategy.dbInMemory = new DBInMemory();
	CrawlerStrategy.dbInMemory.readFromExtenalDB(category, state);
	CrawlerStrategy.dbInMemory.index();
	// target database
	CrawlerStrategy.dbExternal.createTables(H2DB.DB_NAME_TARGET);
    }

    /*
     * (non-Javadoc)
     * 
     * @see mo.umac.crawler.YahooLocalCrawlerStrategy#endData()
     */
    protected void endData() {
	// TODO shut down the connection
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

		    logger.debug(envelopeStateLLA.toString());
		    Envelope envelopeStateECEF = GeoOperator
			    .lla2ecef(envelopeStateLLA);
		    logger.debug(envelopeStateECEF.toString());

		    crawl(state, category, query, envelopeStateECEF);
		    //
		} else {
		    logger.error("Cannot find category id for query: " + query
			    + " in categoryIDMap");
		}
	    }
	}
    }
}