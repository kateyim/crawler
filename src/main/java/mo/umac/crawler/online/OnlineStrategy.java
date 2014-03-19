/**
 * 
 */
package mo.umac.crawler.online;

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

import mo.umac.db.yahoolocal.DBExternal;
import mo.umac.db.yahoolocal.FileDB;
import mo.umac.db.yahoolocal.H2DB;
import mo.umac.main.CrawlerStrategy;
import mo.umac.metadata.yahoolocal.ResultSetYahooOnline;
import mo.umac.metadata.yahoolocal.YahooLocalQueryFileDB;
import mo.umac.parser.StaXParser;
import mo.umac.parser.YahooXmlType;
import mo.umac.spatial.Circle;
import mo.umac.spatial.Coverage;
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
 * The online algorithm is for crawling data from the website
 * 
 * @author Kate Yim
 * 
 */
public abstract class OnlineStrategy extends CrawlerStrategy {

	protected static Logger logger = Logger.getLogger(OnlineStrategy.class.getName());

	public static final String APPID = "appid";

	protected boolean firstCrawl = false;

	protected Envelope firstEnvelope = null;

	private String appid;
	/**
	 * @deprecated The maximum radius (in miles) of the query.
	 */
	protected final double MAX_R = 0.0;

	protected HttpClient httpClient;

	protected final long DAY_TIME = 24 * 60 * 60 * 1000;

	/**
	 * Record the time, because of the restriction of 5000 queries per day
	 */
	protected long beginTime = 0;

	/**
	 * Count the number of continuous limited pages
	 */
	protected int limitedPageCount = 0;

	public static String PROPERTY_PATH = "./src/main/resources/crawler.properties";

	/**
	 * @param appid
	 * @param state
	 * @param category
	 * @param query
	 * @param subFolder
	 * @param envelopeState
	 * @param queryFile
	 * @param queryOutput
	 * @param resultsFile
	 * @param resultsOutput
	 * @return
	 * @deprecated TODO change it to without buffered writer
	 */
	protected abstract IndicatorResult crawl(String appid, String state, int category, String query, String subFolder, Envelope envelopeState, String queryFile, BufferedWriter queryOutput,
			String resultsFile, BufferedWriter resultsOutput);

	/*
	 * (non-Javadoc)
	 * @see
	 * mo.umac.crawler.YahooLocalCrawlerStrategy#crawlByCategoriesStates(java
	 * .util.LinkedList, java.util.List, java.util.LinkedList,
	 * java.util.HashMap)
	 */
	protected void crawlByCategoriesStates(LinkedList<Envelope> listEnvelopeStates, List<String> listCategoryNames, LinkedList<String> nameStates, HashMap<Integer, String> categoryIDMap) {

		FileOperator.createFolder("", DBExternal.FOLDER_NAME);
		httpClient = createHttpClient();
		appid = FileOperator.readAppid(OnlineStrategy.PROPERTY_PATH);

		long before = System.currentTimeMillis();
		logger.info("Start at : " + before);

		try {
			for (int i = 0; i < nameStates.size(); i++) {
				String state = nameStates.get(i);
				logger.info("crawling in the state: " + state);
				// add at 2013-9-23
				if (state.equals("HI")) {
					continue;
				}
				if (state.equals("AR")) {
					continue;
				}
				if (state.equals("MT")) {
					continue;
				}
				if (state.equals("NM")) {
					continue;
				}
				if (state.equals("NY")) {
					continue;
				}
				if (state.equals("ND")) {
					continue;
				}
				if (state.equals("SD")) {
					continue;
				}
				if (state.equals("UT")) {
					continue;
				}
				if (state.equals("OR")) {
					continue;
				}
				if (state.equals("AS")) {
					continue;
				}
				if (state.equals("ID")) {
					continue;
				}
				if (state.equals("MP")) {
					continue;
				}
				if (state.equals("PR")) {
					continue;
				}
				if (state.equals("WA")) {
					continue;
				}
				if (state.equals("OK")) {
					continue;
				}
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
						FileOperator.createFolder(DBExternal.FOLDER_NAME, categoryFolderName);
						String categoryFolderPath = DBExternal.FOLDER_NAME + categoryFolderName + "/";
						String subFolder = FileOperator.createFolder(categoryFolderPath, state);
						// create log files
						// 1. query file
						String queryFile = subFolder + DBExternal.QUERY_FILE_NAME;
						FileOperator.createFile(queryFile);
						BufferedWriter queryOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryFile, true)));
						// 2. results file
						String resultsFile = subFolder + DBExternal.RESULT_FILE_NAME;
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
			httpClient.getConnectionManager().shutdown();
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
		FileOperator.createFolder(DBExternal.FOLDER_NAME, categoryFolderName);
		String categoryFolderPath = DBExternal.FOLDER_NAME + categoryFolderName + "/";
		try {
			String appid = FileOperator.readAppid(OnlineStrategy.PROPERTY_PATH);
			for (int i = 0; i < nameStates.size(); i++) {
				// for (int i = nameStates.size() - 1; i >= 0; i--) {
				String state = nameStates.get(i);
				String subFolder = FileOperator.createFolder(categoryFolderPath, state);
				// query file
				String queryFile = subFolder + DBExternal.QUERY_FILE_NAME;
				FileOperator.createFile(queryFile);
				BufferedWriter queryOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryFile, true)));
				// results file
				String resultsFile = subFolder + DBExternal.RESULT_FILE_NAME;
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
	 * Check whether it still follows the query limitation
	 * 
	 * @param countNumQueries
	 * @param beginTime
	 */
	protected void sleepForIPRestriction(long beginTime) {
		// sleep to satisfy the ip restriction: 5000 accesses per day
		long now = System.currentTimeMillis();
		long diff = (now - beginTime);
		if (diff < DAY_TIME) {
			try {
				long interval = (DAY_TIME - diff) / 1000 / 60;
				logger.info("Sleeping " + interval + "minutes.");
				Thread.currentThread().sleep(DAY_TIME - diff);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// How can I made this mistake!!!
		// beginTime = System.currentTimeMillis();
	}

	/**
	 * Force sleep if there are access limitations.
	 * 
	 * @param limitedPageCount
	 */
	protected void sleeping(int limitedPageCount) {
		// Set 5 minutes as the unit
		long unit = 5 * 60 * 1000;
		long interval = 0;
		long base = 1;
		for (int i = 0; i < limitedPageCount - 1; i++) {
			base *= 2;
		}
		interval = unit * base;
		long aDay = 24 * 60 * 60 * 1000;
		try {
			if (interval > aDay) {
				logger.info("Sleeping a day");
				Thread.currentThread().sleep(aDay);
			} else {
				logger.info("Sleeping " + interval / 1000 / 60 + " minutes.");
				Thread.currentThread().sleep(interval);
			}
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
	protected int maxStartForThisQuery(ResultSetYahooOnline resultSet) {
		int totalResultAvailable = resultSet.getTotalResultsAvailable();
		if (totalResultAvailable > MAX_START + MAX_RESULTS_NUM) {
			return MAX_START;
		}
		int idealNum = (int) (Math.floor(1.0 * totalResultAvailable / MAX_RESULTS_NUM) * 20);
		if (idealNum > MAX_START) {
			return MAX_START;
		} else {
			return idealNum;
		}
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
	 * Common steps in one crawling procedure, crawl in the center point
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
	 * @param resultSet
	 *            return all POIs got in this query procedure
	 * @param stateName
	 * @return an indicator of the result of this query
	 */
	protected IndicatorResult oneCrawlingProcedure(String appid, Envelope aEnvelope, String state, int category, String query, String subFolder, String queryFile, BufferedWriter queryOutput,
			String resultsFile, BufferedWriter resultsOutput, ResultSetYahooOnline resultSet) {
		ResultSetYahooOnline tempResultSet;
		// the first page for any query
		int start = 1;
		Circle circle = Coverage.computeCircle(aEnvelope);
		YahooLocalQueryFileDB qc = new YahooLocalQueryFileDB(subFolder, queryFile, queryOutput, resultsFile, resultsOutput, aEnvelope, appid, state, category, start, circle, countNumQueries, query,
				zip, MAX_RESULTS_NUM);
		resultSet = query(qc);
		//
		// This loop represents turning over the page.
		int maxStartForThisQuery = maxStartForThisQuery(resultSet);
		// logger.debug("totalResultsAvailable=" +
		// resultSet.getTotalResultsAvailable());
		// logger.debug("maxStartForThisQuery=" + maxStartForThisQuery);
		// TODO check the last page
		for (start += MAX_RESULTS_NUM; start <= maxStartForThisQuery; start += MAX_RESULTS_NUM) {
			qc = new YahooLocalQueryFileDB(subFolder, queryFile, queryOutput, resultsFile, resultsOutput, aEnvelope, appid, state, category, start, circle, countNumQueries, query, zip,
					MAX_RESULTS_NUM);
			tempResultSet = query(qc);
			// TODO check add at 5-7-2013
			resultSet.getPOIs().addAll(tempResultSet.getPOIs());
		}
		// the last query
		if (maxStartForThisQuery == MAX_START) {
			start = maxStartForThisQuery;
			qc = new YahooLocalQueryFileDB(subFolder, queryFile, queryOutput, resultsFile, resultsOutput, aEnvelope, appid, state, category, start, circle, countNumQueries, query, zip,
					MAX_RESULTS_NUM);
			tempResultSet = query(qc);
			// TODO check add at 5-7-2013
			resultSet.getPOIs().addAll(tempResultSet.getPOIs());
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
	 * @deprecated
	 */
	private File lookup(YahooLocalQueryFileDB qc) {
		File xmlFile = null;
		String queryFile = qc.getQueryFile();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(queryFile)));
			String data = null;
			String[] split;
			// TODO Look the query from queryFile
			String query = qc.queryInfo();

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
	public ResultSetYahooOnline query(YahooLocalQueryFileDB qc) {
		// FIXME add continue crawling from the interrupt
		// First search from the query file
		// compare the query info with existing records
		// qc.queryInfo();
		// How to get file Name from BufferedWriter???

		File xmlFile = null;
		ResultSetYahooOnline resultSet;
		StaXParser parseXml = new StaXParser();
		String url = qc.toUrl();
		xmlFile = issueToWeb(qc, null);
		resultSet = parseXml.readConfig(xmlFile.getPath());
		// continues issuing to the web as long as there is an access
		// restriction.
		while (resultSet.getXmlType() != YahooXmlType.VALID) {
			logger.error(xmlFile.getName() + ":" + url);
			logger.error(resultSet.getXmlType());
			if (resultSet.getXmlType() == YahooXmlType.LIMIT_EXCEEDED) {
				limitedPageCount++;
				sleeping(limitedPageCount);
				// re-issue to the web
				xmlFile = issueToWeb(qc, xmlFile);
				resultSet = parseXml.readConfig(xmlFile.getPath());
			} else if (resultSet.getXmlType() == YahooXmlType.UNKNOWN) {
				sleeping(0);
				xmlFile = issueToWeb(qc, xmlFile);
				resultSet = parseXml.readConfig(xmlFile.getPath());
			} else {
				// try one more time
				sleeping(0);
				xmlFile = issueToWeb(qc, xmlFile);
				resultSet = parseXml.readConfig(xmlFile.getPath());
				break;
			}
			try {
				qc.getQueryOutput().flush();
				qc.getResultsOutput().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (resultSet.getXmlType() == YahooXmlType.VALID) {
			resultSet.setResultsOutput(qc.getResultsOutput());
			// revised at 2013-5-22
			FileDB.writeQueryFile(xmlFile.getName(), qc.getQueryOutput(), qc.queryInfo(), resultSet);
			if (resultSet.getTotalResultsReturned() > 0) {
				FileDB.writeResultsFile(xmlFile.getName(), resultSet);
			}
			// FIXME record all results in the database. add the connection into
			// the qc, need check!!
			// String queryIDString = xmlFile.getName();
			// int dotIndex = queryIDString.indexOf(".xml");
			// queryIDString = queryIDString.substring(0, dotIndex);
			// // FIXME change the query id to integer!!!
			// int queryID = Integer.parseInt(queryIDString);
			// DBExternal dataset = new H2DB();
			// dataset.writeToExternalDBFromOnline(queryID, 0, 0, qc,
			// resultSet);
		}
		limitedPageCount = 0;
		return resultSet;
	}

	/**
	 * 
	 * @param qc
	 * @param xmlFile
	 *            if it is not null, then write the content to this file
	 * @return
	 */
	private File issueToWeb(YahooLocalQueryFileDB qc, File xmlFile) {
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
			logger.info("numQueries=" + countNumQueries);
			logger.info(url);
			logger.info("query: " + qc.toString());
			// TODO gzip
			sleepForIPRestriction(beginTime);
			beginTime = System.currentTimeMillis();
		}
		if (xmlFile == null) {
			xmlFile = FileOperator.createFileAutoAscending(qc.getSubFolder(), qc.getNumQueries(), ".xml");
		} else {
			xmlFile.delete();
			try {
				xmlFile.createNewFile();
			} catch (IOException e) {
				logger.error("creating file failed " + xmlFile.getAbsolutePath());
				e.printStackTrace();
			}
		}
		boolean success = false;
		int i = 0;
		if (!success) {
			success = fetching(httpClient, xmlFile, url);
			i++;
			if (i > 1) {
				logger.error("fetching for the " + i + "times");
			}
		}
		countNumQueries++;
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
