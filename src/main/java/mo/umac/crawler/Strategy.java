package mo.umac.crawler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mo.umac.db.DBExternal;
import mo.umac.db.DBInMemory;
import mo.umac.uscensus.UScensusData;
import myrtree.MyRTree;

import org.apache.log4j.Logger;

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
	protected static int MAX_TOTAL_RESULTS_RETURNED = MAX_START + MAX_RESULTS_NUM; // =270;

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
	 * Entrance of the crawler
	 * 
	 * @param listNameStates
	 * @param listCategoryNames
	 */
	public void callCrawling(LinkedList<String> listNameStates, List<String> listCategoryNames) {

		LinkedList<Envelope> listEnvelopeStates = selectEnvelopes(listNameStates);
		if (listNameStates.size() == 0) {
			listNameStates = (LinkedList<String>) UScensusData.stateName(UScensusData.STATE_DBF_FILE_NAME);
		}
		HashMap<Integer, String> categoryIDMap = FileOperator.readCategoryID(CATEGORY_ID_PATH);
		crawlByCategoriesStates(listEnvelopeStates, listCategoryNames, listNameStates, categoryIDMap);
	}

	protected abstract void crawlByCategoriesStates(LinkedList<Envelope> listEnvelopeStates, List<String> listCategoryNames, LinkedList<String> listNameStates, HashMap<Integer, String> categoryIDMap);

	/**
	 * Select the envelope information from UScensus data, if listNameStates is
	 * empty, then return all envelopes in the U.S.
	 * 
	 * @param listNameStates
	 * @return
	 */
	private LinkedList<Envelope> selectEnvelopes(LinkedList<String> listNameStates) {
		// State's information provided by UScensus
		LinkedList<Envelope> allEnvelopeStates = (LinkedList<Envelope>) UScensusData.MBR(UScensusData.STATE_SHP_FILE_NAME);
		LinkedList<String> allNameStates = (LinkedList<String>) UScensusData.stateName(UScensusData.STATE_DBF_FILE_NAME);

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
	
	public boolean covered(Envelope envelopeStateECEF) {
		return Strategy.rtreeRectangles.contains(envelopeStateECEF);
	}

}
