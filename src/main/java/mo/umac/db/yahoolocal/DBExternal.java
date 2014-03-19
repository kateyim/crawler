package mo.umac.db.yahoolocal;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import mo.umac.metadata.yahoolocal.APOI;
import mo.umac.metadata.yahoolocal.AQuery;
import mo.umac.metadata.yahoolocal.ResultSet;
import mo.umac.metadata.yahoolocal.ResultSetYahooOnline;
import mo.umac.metadata.yahoolocal.YahooLocalQueryFileDB;

public abstract class DBExternal {

	/**
	 * A folder stores all crawled .xml file from Yahoo Local.
	 * 
	 */
	public static final String FOLDER_NAME = "../crawler-data/yahoolocal/";

	/**
	 * A file stores the xml file's name, query condition, and the count
	 * information of this query.
	 */
	public static final String QUERY_FILE_NAME = "query";

	/**
	 * A file stores the xml file's name and the detailed results of a query.
	 */
	public static final String RESULT_FILE_NAME = "results";

	public static String dbNameSource = "";

	public static String dbNameTarget = "";

	/**
	 * String represents the name of the database;
	 */
	public static Map connMap = new HashMap<String, java.sql.Connection>();

	public abstract void init();

	/**
	 * Read dataset from external database.
	 */
	public abstract HashMap<Integer, APOI> readFromExtenalDB(String category, String state);

	public abstract void writeToExternalDB(int queryID, AQuery query, ResultSet resultSet);

	public abstract void writeToExternalDBFromOnline(int queryID, int level, int parentID, YahooLocalQueryFileDB qc, ResultSetYahooOnline resultSet);

	public abstract void createTables(String dbNameTarget);

	public abstract int numCrawlerPoints();

	public abstract void removeDuplicate();
	
	/**
	 * Update the numCrawled
	 */
	public abstract void updataExternalDB();

	/**
     * 
     */
	public static void distroyConn() {
		Iterator it = connMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			java.sql.Connection conn = (java.sql.Connection) entry.getValue();
			try {
				conn.commit();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
