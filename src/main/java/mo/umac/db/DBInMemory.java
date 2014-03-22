package mo.umac.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mo.umac.crawler.Main;
import mo.umac.crawler.Strategy;
import mo.umac.metadata.APOI;
import mo.umac.metadata.AQuery;
import mo.umac.metadata.ResultSetD2;
import myrtree.MyRTree;

import org.apache.log4j.Logger;


import com.vividsolutions.jts.geom.Coordinate;

public class DBInMemory {

	protected static Logger logger = Logger.getLogger(DBInMemory.class.getName());

	/**
	 * All tuples; Integer is the item's id
	 */
	public static HashMap<Integer, APOI> pois;

	/**
	 * The index for all points in the database
	 */
	public static MyRTree rtreePoints;

	// TODO treeset is for debugging. change to hashset when running the program
	public static Set<Integer> poisIDs = new HashSet<Integer>();

	/**
	 * add at 2013-9-23 Stores the number of times a points being crawled
	 */
	public static Map<Integer, Integer> poisCrawledTimes;

	// /**
	// * table 4: the pair of query's id and returned poi's id
	// */
	// public static Set<String> queryPoiCrawled;
	//
	// public static Set<>

	/**
	 * @param externalDataSet
	 */
	public void readFromExtenalDB(String category, String state) {
		pois = Strategy.dbExternal.readFromExtenalDB(category, state);
	}

	// public void writeToExternalDB(int queryID, int level, int parentID, YahooLocalQueryFileDB qc, ResultSetYahooOnline resultSet) {
	// Strategy.dbExternal.writeToExternalDBFromOnline(queryID, level, parentID, qc, resultSet);
	// }

	/**
	 * For recording the query results
	 * 
	 * @param dbName
	 * @param queryID
	 * @param query
	 * @param resultSet
	 */
	private void writeToExternalDB(int queryID, AQuery query, ResultSetD2 resultSet) {
		Strategy.dbExternal.writeToExternalDB(queryID, query, resultSet);
	}

	/**
	 * Update the numCrawled
	 */
	public void updataExternalDB() {
		Strategy.dbExternal.updataExternalDB();
	}

	public void index(List<Coordinate> coordinate) {
		rtreePoints = new MyRTree(coordinate);
	}

	/**
	 * Indexing all pois
	 */
	public void index() {
		rtreePoints = new MyRTree(pois);
	}

	public ResultSetD2 query(AQuery qc) {
		Coordinate queryPoint = qc.getPoint();
		if (logger.isDebugEnabled()) {
			logger.debug("query point = " + queryPoint.toString());
		}
		List<Integer> resultsID = rtreePoints.searchNN(queryPoint, qc.getTopK());
		//
		for (int i = 0; i < resultsID.size(); i++) {
			int id = resultsID.get(i);
			int times = 0;
			if (poisCrawledTimes.containsKey(id)) {
				times = poisCrawledTimes.get(id);
			}
			times += 1;
			poisCrawledTimes.put(id, times);
		}

		poisIDs.addAll(resultsID);

		// FIXME add re-transfer from the break point.
		int queryID = Strategy.countNumQueries;

		if (logger.isDebugEnabled()) {
			logger.debug("countNumQueries = " + Strategy.countNumQueries);
		}

		ResultSetD2 resultSet = queryByID(resultsID);
		resultSet.setTotalResultsReturned(resultsID.size());

		// if (logger.isDebugEnabled()) {
		// int size1 = resultsID.size();
		// int size2 = resultSet.getPOIs().size();
		// if (size1 != size2) {
		// logger.error("size1 != size2");
		// }
		// }

		writeToExternalDB(queryID, qc, resultSet);
		// revised at 2013-9-30
		// writeToInMemoryDB(queryID, qc, resultSet);

		//
		// if (logger.isDebugEnabled()) {
		// logger.debug("countNumQueries = " + CrawlerStrategy.countNumQueries);
		// logger.debug("number of points crawled = " + numCrawlerPoints());
		//
		// int size1 = numCrawlerPoints();
		// Set set = new TreeSet();
		// int size2 = numOfTuplesInExternalDB(set);
		// logger.debug("numCrawlerPoints in memory = " + size1);
		// logger.debug("numCrawlerPoints in db = " + size2);
		// if (size1 != size2) {
		// logger.error("size1 != size2");
		// logger.error("countNumQueries = " + CrawlerStrategy.countNumQueries);
		// logger.error("numCrawlerPoints in memory = " + size1);
		// logger.error("numCrawlerPoints in db = " + size2);
		// }
		// }

		if (queryID % 10 == 0) {
			logger.info("countNumQueries = " + Strategy.countNumQueries);
			logger.info("number of points crawled = " + numCrawlerPoints());
		}
		Strategy.countNumQueries++;
		return resultSet;
	}

	/**
	 * Only for debugging
	 * 
	 * @return
	 */
	public int numOfTuplesInExternalDB(Set set) {
		H2DB h2db = new H2DB();
		String dbName = Main.DB_NAME_TARGET;
		Connection conn = h2db.getConnection(dbName);
		try {
			Statement stat = conn.createStatement();
			String sql = "select distinct itemid from item";
			java.sql.ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt(1);
				set.add(id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set.size();
	}

	/**
	 * @param resultsID
	 * @return
	 */
	public ResultSetD2 queryByID(List<Integer> resultsID) {
		List<APOI> points = new ArrayList<APOI>();
		for (int i = 0; i < resultsID.size(); i++) {
			int id = resultsID.get(i);
			APOI point = pois.get(id);
			points.add(point);
		}
		ResultSetD2 resultSet = new ResultSetD2();
		resultSet.setPOIs(points);
		return resultSet;
	}

	public int numCrawlerPoints() {
		return DBInMemory.poisIDs.size();
	}

}
