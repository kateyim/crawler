/**
 * 
 */
package mo.umac.crawler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mo.umac.geo.Circle;
import mo.umac.geo.Coverage;
import mo.umac.geo.UScensusData;
import mo.umac.parser.ResultSet;
import mo.umac.parser.StaXParser;
import mo.umac.parser.YahooXmlType;
import mo.umac.utils.CommonUtils;
import mo.umac.utils.FileOperator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author Kate Yim
 * 
 */
public abstract class OnlineYahooLocalCrawlerStrategy {

	public static Logger logger = Logger.getLogger(OnlineYahooLocalCrawlerStrategy.class.getName());

	public static final String APPID = "appid";

	public static String PROPERTY_PATH = "./src/main/resources/crawler.properties";

	/**
	 * The path of the category ids of Yahoo! Local
	 */
	public static String CATEGORY_ID_PATH = "./src/main/resources/cat_id.txt";

	/**
	 * The maximum number of returned results by a query.
	 */
	protected final int MAX_RESULTS_NUM = 20;
	/**
	 * The maximum starting result position to return.
	 */
	protected final int MAX_START = 250;

	/**
	 * The maximum number of results on can get through this query by only
	 * changing the start value.
	 */
	protected final int MAX_TOTAL_RESULTS_RETURNED = MAX_START + MAX_RESULTS_NUM; // =270;

	protected final long DAY_TIME = 24 * 60 * 60 * 1000;

	protected boolean firstCrawl = false;

	/**
	 * Record the time, because of the restriction of 5000 queries per day
	 */
	protected long beginTime = 0;

	protected HttpClient httpClient;

	protected int numQueries = 1;

	// protected String query = "restaurants";

	protected int zip = 0;

	/* abbr. of the city name */
	// protected String state = "";
	//
	// protected int category = 0;

	protected Envelope firstEnvelope = null;

	/**
	 * The maximum radius (in miles) of the query.
	 */
	protected final double MAX_R = 0.0;

	/**
	 * Count the number of continuous limited pages
	 */
	private int limitedPageCount = 0;

	protected abstract IndicatorResult crawl(String appid, String state, int category, String query, String subFolder, Envelope envelopeState,
			String queryFile, BufferedWriter queryOutput, String resultsFile, BufferedWriter resultsOutput);

	/**
	 * Entrance of the crawler
	 */

	/**
	 * @param listNameStates
	 * @param listCategoryNames
	 */
	public void callCrawling(LinkedList<String> listNameStates, List<String> listCategoryNames) {
		// Standard information provided by UScensus
		LinkedList<Envelope> allEnvelopeStates = (LinkedList<Envelope>) UScensusData.MBR(UScensusData.STATE_SHP_FILE_NAME);
		LinkedList<String> allNameStates = (LinkedList<String>) UScensusData.stateName(UScensusData.STATE_DBF_FILE_NAME);

		// TODO select the specified states according to the listNameStates
		LinkedList<Envelope> listEnvelopeStates = new LinkedList<Envelope>();
		listEnvelopeStates = allEnvelopeStates;
		listNameStates = allNameStates;

		// Create the parent folder for all data
		FileOperator.createFolder("", DBFile.FOLDER_NAME);
		httpClient = createHttpClient();
		HashMap<Integer, String> categoryIDMap = FileOperator.readCategoryID(CATEGORY_ID_PATH);

		// int category = -1;
		// if (categoryName != null) {
		// Object searchingResult;
		// searchingResult = CommonUtils.getKeyByValue(categoryIDMap,
		// categoryName);
		// if (searchingResult != null) {
		// category = (Integer) searchingResult;
		// }
		// crawlOneCategoryInUS(envelopeStates, nameStates, category,
		// categoryName);
		// } else {
		// crawlAllCategoriesInUS(envelopeStates, nameStates, categoryIDMap);
		// }
		crawlByCategoriesStates(listEnvelopeStates, listCategoryNames, listNameStates, categoryIDMap);

		httpClient.getConnectionManager().shutdown();

	}

	/**
	 * Add at 2013-4-15
	 * 
	 * @param listEnvelopeStates
	 * @param listCategoryName
	 * @param nameStates
	 * @param categoryIDMap
	 */
	private void crawlByCategoriesStates(LinkedList<Envelope> listEnvelopeStates, List<String> listCategoryNames, LinkedList<String> nameStates,
			HashMap<Integer, String> categoryIDMap) {
		try {
			String appid = FileOperator.readAppid(OnlineYahooLocalCrawlerStrategy.PROPERTY_PATH);
			for (int i = 0; i < nameStates.size(); i++) {
				String state = nameStates.get(i);
				logger.info("crawling in the state: " + state);
				for (int j = 0; j < listCategoryNames.size(); j++) {
					String query = listCategoryNames.get(j);
					logger.info("crawling the category: " + query);
					// initial category
					int category = -1;
					Object searchingResult = CommonUtils.getKeyByValue(categoryIDMap, query);
					if (searchingResult != null) {
						category = (Integer) searchingResult;
						// creating folder and files
						String categoryFolderName = category + "+" + query;
						// first create the category folder, and then create the
						// state folder inside the category folder.
						FileOperator.createFolder(DBFile.FOLDER_NAME, categoryFolderName);
						String categoryFolderPath = DBFile.FOLDER_NAME + categoryFolderName + "/";
						String subFolder = FileOperator.createFolder(categoryFolderPath, state);
						// create log files
						// 1. query file
						String queryFile = subFolder + DBFile.QUERY_FILE_NAME;
						FileOperator.createFile(queryFile);
						BufferedWriter queryOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryFile, true)));
						// 2. results file
						String resultsFile = subFolder + DBFile.RESULT_FILE_NAME;
						FileOperator.createFile(resultsFile);
						BufferedWriter resultsOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsFile, true)));
						//
						Envelope envelopeState = listEnvelopeStates.get(i);
						crawl(appid, state, category, query, subFolder, envelopeState, queryFile, queryOutput, resultsFile, resultsOutput);
						//
						queryOutput.flush();
						resultsOutput.flush();
						queryOutput.close();
						resultsOutput.close();
					} else {
						logger.error("Cannot find category id for query: " + query + " in categoryIDMap");
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * For every category, traverse all states.
	 * 
	 * @param envelopeStates
	 * @param nameStates
	 * @param categoryIDMap
	 */
	private void crawlAllCategoriesInUS(LinkedList<Envelope> envelopeStates, LinkedList<String> nameStates, HashMap<Integer, String> categoryIDMap) {
		Iterator iter = categoryIDMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			int category = (Integer) entry.getKey();
			String queries = (String) entry.getValue();
			String[] query = queries.split("&");
			for (int i = 0; i < query.length; i++) {
				crawlOneCategoryInUS(envelopeStates, nameStates, category, query[i].trim());
			}
		}
	}

	private void crawlOneCategoryInUS(LinkedList<Envelope> envelopeStates, LinkedList<String> nameStates, int category, String query) {
		String categoryFolderName = category + "+" + query;
		FileOperator.createFolder(DBFile.FOLDER_NAME, categoryFolderName);
		String categoryFolderPath = DBFile.FOLDER_NAME + categoryFolderName + "/";
		try {
			String appid = FileOperator.readAppid(OnlineYahooLocalCrawlerStrategy.PROPERTY_PATH);
			for (int i = 0; i < nameStates.size(); i++) {
				// for (int i = nameStates.size() - 1; i >= 0; i--) {
				String state = nameStates.get(i);
				String subFolder = FileOperator.createFolder(categoryFolderPath, state);
				// query file
				String queryFile = subFolder + DBFile.QUERY_FILE_NAME;
				FileOperator.createFile(queryFile);
				BufferedWriter queryOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryFile, true)));
				// results file
				String resultsFile = subFolder + DBFile.RESULT_FILE_NAME;
				FileOperator.createFile(resultsFile);
				BufferedWriter resultsOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsFile, true)));
				//
				Envelope envelopeState = envelopeStates.get(i);
				logger.info("Crawling " + state);
				crawl(appid, state, category, query, subFolder, envelopeState, queryFile, queryOutput, resultsFile, resultsOutput);
				//
				queryOutput.flush();
				resultsOutput.flush();
				queryOutput.close();
				resultsOutput.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Common steps in one crawling procedure
	 * 
	 * @param appid
	 * @param aEnvelope
	 * @param category
	 * @param query
	 * @param subFolder
	 * @param queryFile
	 * @param queryOutput
	 * @param resultsFile
	 * @param resultsOutput
	 * @param stateName
	 * @return an indicator of the result of this query
	 */
	protected IndicatorResult oneCrawlingProcedure(String appid, Envelope aEnvelope, String state, int category, String query, String subFolder,
			String queryFile, BufferedWriter queryOutput, String resultsFile, BufferedWriter resultsOutput) {
		// the first page for any query
		int start = 1;
		Circle circle = Coverage.computeCircle(aEnvelope);
		YahooLocalQuery qc = new YahooLocalQuery(subFolder, queryFile, queryOutput, resultsFile, resultsOutput, aEnvelope, appid, state, category, start,
				circle, numQueries, query, zip, MAX_RESULTS_NUM);
		ResultSet resultSet = query(qc);
		//
		// This loop represents turning over the page.
		int maxStartForThisQuery = maxStartForThisQuery(resultSet);
		// logger.debug("totalResultsAvailable=" +
		// resultSet.getTotalResultsAvailable());
		// logger.debug("maxStartForThisQuery=" + maxStartForThisQuery);
		for (start += MAX_RESULTS_NUM; start < maxStartForThisQuery; start += MAX_RESULTS_NUM) {
			qc = new YahooLocalQuery(subFolder, queryFile, queryOutput, resultsFile, resultsOutput, aEnvelope, appid, state, category, start, circle,
					numQueries, query, zip, MAX_RESULTS_NUM);
			query(qc);
		}
		// the last query
		if (maxStartForThisQuery == MAX_START) {
			// logger.info("maxStartForThisQuery == MAX_START");
			qc = new YahooLocalQuery(subFolder, queryFile, queryOutput, resultsFile, resultsOutput, aEnvelope, appid, state, category, maxStartForThisQuery,
					circle, numQueries, query, zip, MAX_RESULTS_NUM);
			query(qc);
		}
		if (resultSet.getTotalResultsAvailable() > MAX_TOTAL_RESULTS_RETURNED) {
			return IndicatorResult.OVERFLOW;
		}
		return IndicatorResult.NONOVERFLOW;
	}

	/**
	 * Look up the query results in local.
	 * 
	 * @param qc
	 * @return
	 */
	private File lookup(YahooLocalQuery qc) {
		File xmlFile = null;
		String queryFile = qc.getQueryFile();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(queryFile)));
			String data = null;
			String[] split;
			// TODO Look the query from queryFile
			String query = qc.toStringForWritting();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return xmlFile;
	}

	/**
	 * The query process, including construct the url; create the .xml file;
	 * fetching from the web; parse the .xml file, storing the result, etc.
	 * 
	 * @param qc
	 *            All information need in one query
	 * @return The parsed result set.
	 */
	public ResultSet query(YahooLocalQuery qc) {
		File xmlFile = null;
		ResultSet resultSet;
		StaXParser parseXml = new StaXParser();
		String url = qc.toUrl();
		xmlFile = issueToWeb(qc);
		resultSet = parseXml.readConfig(xmlFile.getPath());
		if (resultSet.getXmlType() == YahooXmlType.VALID) {
			resultSet.setResultsOutput(qc.getResultsOutput());
			DBFile.writeQueryFile(xmlFile.getName(), qc, resultSet);
			if (resultSet.getTotalResultsReturned() > 0) {
				DBFile.writeResultsFile(xmlFile.getName(), resultSet);
			}
			limitedPageCount = 0;
		} else { // for error pages
			logger.error(xmlFile.getName() + ":" + url);
			if (resultSet.getXmlType() == YahooXmlType.LIMIT_EXCEEDED) {
				limitedPageCount++;
				sleeping(limitedPageCount);
			} else if (resultSet.getXmlType() == YahooXmlType.OTHER_ERROR) {
				limitedPageCount = 0;
				logger.error("YahooXmlType.OTHER_ERROR: " + url);
			} else {
				// breaking point
				// FIXME XML parse error
				logger.error("Unexpected type error of the resultSet!");
			}
			try {
				qc.getQueryOutput().flush();
				qc.getResultsOutput().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultSet;
	}

	private File issueToWeb(YahooLocalQuery qc) {
		// This is a new query which will be issued to the website.
		String url = qc.toUrl();
		url = url.replaceAll(" ", "%20");

		// logger.info("numQueries=" + numQueries);
		// logger.info(url);

		if (!firstCrawl) {
			firstCrawl = true;
			beginTime = System.currentTimeMillis();
		}
		// writing to the files, make sure that the buffered writer will be
		// written to the disk.
		if (qc.getNumQueries() % 100 == 0) {
			logger.debug("For testing flush..." + qc.getNumQueries());
			try {
				qc.getQueryOutput().flush();
				qc.getResultsOutput().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// This is the IP restriction by Yahoo! Local
		if (qc.getNumQueries() % 5000 == 0) {
			logger.info("numQueries=" + numQueries);
			logger.info(url);
			logger.info("query: " + qc.toString());
			// TODO gzip
			sleepForIPRestriction(beginTime);
			beginTime = System.currentTimeMillis();
		}
		File xmlFile = FileOperator.createFileAutoAscending(qc.getSubFolder(), qc.getNumQueries(), ".xml");
		boolean success = false;
		int i = 0;
		if (!success) {
			success = fetching(httpClient, xmlFile, url);
			i++;
			if (i > 1) {
				logger.error("fetching for the " + i + "times");
			}
		}
		numQueries++;
		return xmlFile;
	}

	/**
	 * Get next region according to the previous envelope
	 * 
	 * @param envelopeState
	 *            The MBR of all regions
	 * @param aEnvelope
	 *            previous region
	 * @param unit
	 *            the unit region
	 * @param overflow
	 * @return
	 */
	public Envelope nextEnvelopeInRegion(Envelope region, Envelope previousEnvelope, Envelope unit, boolean overflow) {
		if (previousEnvelope == null) {
			return firstEnvelopeInRegion(region, unit, overflow);
		} else {
			return nextButNotFirstEnvelopeInRegion(region, previousEnvelope, unit, overflow);
		}
	}

	/**
	 * Get the first envelope in this region.
	 * 
	 * @param region
	 *            : the whole region need to be covered
	 * @param unit
	 *            : the unit rectangle
	 * @param overflow
	 *            : if overflow=true, then divide the rectangle, else find the
	 *            left-corner rectangle in the region.
	 * @return
	 */
	public Envelope firstEnvelopeInRegion(Envelope region, Envelope unit, boolean overflow) {
		return null;
	}

	/**
	 * Compute the next envelope (not the first one!)
	 * 
	 * @param region
	 * @param unit
	 * @param overflow
	 * @return
	 */
	public Envelope nextButNotFirstEnvelopeInRegion(Envelope region, Envelope previousEnvelope, Envelope unit, boolean overflow) {
		double x1 = 0;
		double y1 = 0;
		// Get to the right-most boundary
		if (previousEnvelope.getMaxX() >= region.getMaxX()) {
			x1 = region.getMinX();
			y1 = previousEnvelope.getMaxY();
		} else {
			x1 = previousEnvelope.getMaxX();
			y1 = previousEnvelope.getMinY();
		}
		Envelope next = new Envelope(x1, x1 + unit.getWidth(), y1, y1 + unit.getHeight());
		return next;
	}

	/**
	 * judge whether the web-site returns random results (whether the ranking
	 * function of the web-site is static)
	 * 
	 * @return
	 */
	private boolean isStaticRanking() {
		// TODO isStaticRanking
		return true;
	}

	/**
	 * Add more restriction to the query, in order to solve the restriction on
	 * the radius.
	 */
	private void fineQuery() {
		// TODO fineQuery
	}

	/**
	 * Issue the query, and then save the returned .xml file.
	 * 
	 * @param httpclient
	 * @param xmlFile
	 * @param url
	 */
	protected boolean fetching(HttpClient httpclient, File xmlFile, String url) {
		OutputStream output;
		try {
			output = new BufferedOutputStream(new FileOutputStream(xmlFile));
			logger.debug("fetching... " + url);
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				entity.writeTo(output);
				// release the resources
				httpget.abort();
			} else {
				logger.error("fetching an empty entity!");
				// TODO re-fetching
				return false;
			}
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Check whether it still follows the query limitation
	 * 
	 * @param numQueries
	 * @param beginTime
	 */
	protected void sleepForIPRestriction(long beginTime) {
		// sleep to satisfy the ip restriction: 5000 accesses per day
		long now = System.currentTimeMillis();
		long diff = (now - beginTime);
		if (diff < DAY_TIME) {
			try {
				Thread.currentThread().sleep(DAY_TIME - diff);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// How can I made this mistake!!!
		// beginTime = System.currentTimeMillis();
	}

	/**
	 * Force sleep if there are unexpected access limitations.
	 * 
	 * @param limitedPageCount
	 */
	private void sleeping(int limitedPageCount) {
		// Set 5 minutes as the unit
		long unit = 5 * 60 * 1000;
		long interval = 0;
		for (int i = 0; i < limitedPageCount; i++) {
			interval += unit;
		}
		logger.info("Sleeping " + limitedPageCount * 5 + " minutes.");
		try {
			Thread.currentThread().sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calculate the maximum start number of a query
	 * 
	 * @param resultSet
	 * @return the max start value in constructing a query.
	 */
	protected int maxStartForThisQuery(ResultSet resultSet) {
		int totalResultAvailable = resultSet.getTotalResultsAvailable();
		if (totalResultAvailable > MAX_START + MAX_RESULTS_NUM) {
			return MAX_START;
		}
		return (int) (Math.floor(1.0 * totalResultAvailable / MAX_RESULTS_NUM) * 20);
	}

	protected List<Envelope> divideARectangle() {
		return null;
	}

	/**
	 * @param previousEnvelope
	 * @param region
	 * @return
	 */
	protected boolean finishedCrawling(Envelope previousEnvelope, Envelope region) {
		// Get to the right-most boundary
		if (previousEnvelope.getMaxX() >= region.getMaxX()) {
			// Get to the upper-most boundary
			if (previousEnvelope.getMaxY() >= region.getMaxY()) {
				return true;
			}
		}
		return false;
	}

	protected HttpClient createHttpClient() {
		PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
		HttpParams params = new BasicHttpParams();
		int timeout = 1000 * 24 * 60 * 60;
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		HttpClient httpClient = new DefaultHttpClient(manager, params);
		return httpClient;
	}

	/**
	 * Concatenate the file name using these fields.
	 * 
	 * @param query
	 * @param zip
	 * @param results
	 * @param start
	 * @param latitude
	 * @param longitude
	 * @param radius
	 * @return
	 */
	protected String concatenateFileName(String query, int zip, int results, int start, double latitude, double longitude, double radius) {
		StringBuffer sb = new StringBuffer();
		if (query != null) {
			sb.append("query=");
			sb.append(query);
		}
		if (zip > 0) {
			sb.append(",zip=");
			sb.append(zip);
		}
		if (results > 0) {
			sb.append(",results=");
			sb.append(results);
		}
		if (start > 0) {
			sb.append(",start=");
			sb.append(start);
		}
		if (latitude > 0) {
			sb.append(",latitude=");
			sb.append(latitude);
		}
		if (longitude > 0) {
			sb.append(",longitude=");
			sb.append(longitude);
		}
		if (radius > 0) {
			sb.append(",radius=");
			sb.append(radius);
		}
		return sb.toString();
	}

}