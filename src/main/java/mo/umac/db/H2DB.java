package mo.umac.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import mo.umac.crawler.MainYahoo;
import mo.umac.crawler.Strategy;
import mo.umac.metadata.APOI;
import mo.umac.metadata.AQuery;
import mo.umac.metadata.Category;
import mo.umac.metadata.Rating;
import mo.umac.metadata.ResultSetD2;

import org.apache.log4j.Logger;

import utils.CommonUtils;
import utils.DefaultValues;

/**
 * Operators of the database
 * 
 * @author Kate
 */
public class H2DB extends DBExternal {

	protected static Logger logger = Logger.getLogger(H2DB.class.getName());

	// table names
	private final String QUERY = "QUERY";
	public final static String ITEM = "ITEM";
	private final String CATEGORY = "CATEGORY";
	private final String RELATIONSHIP = "RELATIONSHIP";

	public H2DB() {
		super();
		super.dbNameSource = MainYahoo.DB_NAME_SOURCE;
		super.dbNameTarget = MainYahoo.DB_NAME_TARGET;
	}

	public H2DB(String dbNameSource, String dbNameTarget) {
		super();
		super.dbNameSource = dbNameSource;
		super.dbNameTarget = dbNameTarget;
	}

	/****************************** sqls for deleting table ******************************/
	private String sqlDeleteQueryTable = "DROP TABLE IF EXISTS QUERY";
	private String sqlDeleteItemTable = "DROP TABLE IF EXISTS ITEM";
	private String sqlDeleteCategoryTable = "DROP TABLE IF EXISTS CATEGORY";
	private String sqlDeleteRelationshipTable = "DROP TABLE IF EXISTS RELATIONSHIP";

	/****************************** sqls for creating table ******************************/
	/**
	 * level: the divided level radius: the radius of the circle want to covered PRIMARY KEY
	 */
	private String sqlCreateQueryTable = "CREATE TABLE IF NOT EXISTS QUERY "
			+ "(QUERYID INT, QUERY VARCHAR(100), ZIP INT, RESULTS INT, START INT, "
			+ "LATITUDE DOUBLE, LONGITUDE DOUBLE, RADIUS DOUBLE, LEVEL INT, PARENTID INT, "
			+ "TOTALRESULTSAVAILABLE INT, TOTALRESULTSRETURNED INT, FIRSTRESULTPOSITION INT)";
	// revised at 2013-9-26
	private String sqlCreateItemTable = "CREATE TABLE IF NOT EXISTS ITEM "
			+ "(ITEMID INT, TITLE VARCHAR(200), CITY VARCHAR(200), STATE VARCHAR(10), "
			+ "LATITUDE DOUBLE, LONGITUDE DOUBLE, DISTANCE DOUBLE, "
			+ "AVERAGERATING DOUBLE, TOTALRATINGS DOUBLE, TOTALREVIEWS DOUBLE, NUMCRAWLED INT)";

	private String sqlCreateItemTableKey = "CREATE TABLE IF NOT EXISTS ITEM "
			+ "(ITEMID INT PRIMARY KEY, TITLE VARCHAR(200), CITY VARCHAR(200), STATE VARCHAR(10), "
			+ "LATITUDE DOUBLE, LONGITUDE DOUBLE, DISTANCE DOUBLE, "
			+ "AVERAGERATING DOUBLE, TOTALRATINGS DOUBLE, TOTALREVIEWS DOUBLE, NUMCRAWLED INT)";

	private String sqlCreateCategoryTable = "CREATE TABLE IF NOT EXISTS CATEGORY (ITEMID INT, "
			+ "CATEGORYID INT, CATEGORYNAME VARCHAR(200))";

	/**
	 * This table records that the item is returned by which query in which position.
	 */
	private String sqlCreateRelationshipTable = "CREATE TABLE IF NOT EXISTS RELATIONSHIP "
			+ "(ITEMID INT, QEURYID INT, POSITION INT)";

	/****************************** sqls preparation for insertion ******************************/
	private String sqlPrepInsertQuery = "INSERT INTO QUERY (QUERYID, QUERY, ZIP, RESULTS, START, "
			+ "LATITUDE, LONGITUDE, RADIUS, LEVEL, PARENTID, "
			+ "TOTALRESULTSAVAILABLE ,TOTALRESULTSRETURNED, FIRSTRESULTPOSITION) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private String sqlPrepInsertItem = "INSERT INTO ITEM (ITEMID, TITLE, CITY, STATE, "
			+ "LATITUDE, LONGITUDE, DISTANCE, AVERAGERATING, TOTALRATINGS, TOTALREVIEWS, NUMCRAWLED) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

	private String sqlPrepInsertCategory = "INSERT INTO CATEGORY (ITEMID, CATEGORYID, CATEGORYNAME) VALUES (?,?,?)";

	private String sqlPrepInsertRelationship = "INSERT INTO RELATIONSHIP (ITEMID, QEURYID, POSITION) VALUES(?,?,?)";

	/****************************** sqls preparation for update ******************************/
	private String sqlPrepUpdateItem = "update item set NUMCRAWLED = ? where ITEMID = ?";

	/**
	 * sql for select all data from a table. Need concatenate the table's names.
	 */
	public static String sqlSelectStar = "SELECT * FROM ";

	private String sqlSelectCountStar = "SELECT COUNT(*) FROM ";

	/****************************** sqls revome duplicate ******************************/
	private String s01 = "DROP TABLE IF EXISTS holdkey";
	private String s02 = "DROP TABLE IF EXISTS holdups";
	// table 1
	private String s1Query = "create table holdkey as SELECT QUERYID from QUERY GROUP BY QUERYID";
	private String s2Query = "create table holdups as SELECT DISTINCT QUERY.*  FROM QUERY, holdkey WHERE QUERY.QUERYID = holdkey.QUERYID";
	private String s3Query = "DELETE FROM QUERY";
	private String s4Query = "INSERT into QUERY SELECT * FROM holdups";
	// table 2
	private String s1Item = "create table holdkey as SELECT ITEMID from ITEM GROUP BY ITEMID";
	private String s2Item = "create table holdups as SELECT DISTINCT ITEM.*  FROM ITEM, holdkey WHERE ITEM.ITEMID = holdkey.ITEMID";
	private String s3Item = "DELETE FROM ITEM";
	private String s4Item = "INSERT into ITEM SELECT * FROM holdups";
	// table 3
	private String s1Category = "create table holdkey as SELECT ITEMID, CATEGORYID from CATEGORY GROUP BY ITEMID, CATEGORYID";
	private String s2Category = "create table holdups as SELECT DISTINCT CATEGORY.*  FROM CATEGORY, holdkey WHERE CATEGORY.ITEMID = holdkey.ITEMID and CATEGORY.CATEGORYID = holdkey.CATEGORYID";
	private String s3Category = "DELETE FROM CATEGORY";
	private String s4Category = "INSERT into CATEGORY SELECT * FROM holdups";
	// table 4
	private String s1Relationship = "create table holdkey as SELECT ITEMID, QEURYID from Relationship GROUP BY ITEMID, QEURYID";
	private String s2Relationship = "create table holdups as SELECT DISTINCT Relationship.*  FROM Relationship, holdkey WHERE Relationship.ITEMID = holdkey.ITEMID and Relationship.QEURYID = holdkey.QEURYID";
	private String s3Relationship = "DELETE FROM CATEGORY";
	private String s4Relationship = "INSERT into Relationship SELECT * FROM holdups";

	@Override
	public void writeToExternalDB(int queryID, AQuery aQuery,
			ResultSetD2 resultSet) {
		String dbName = dbNameTarget;
		Connection con = getConnection(dbName);
		//
		// prepared statement
		PreparedStatement prepQuery;
		PreparedStatement prepItem;
		PreparedStatement prepCategory;
		PreparedStatement prepRelationship;
		try {
			con.setAutoCommit(false);

			prepItem = con.prepareStatement(sqlPrepInsertItem);
			prepCategory = con.prepareStatement(sqlPrepInsertCategory);
			prepQuery = con.prepareStatement(sqlPrepInsertQuery);
			prepRelationship = con.prepareStatement(sqlPrepInsertRelationship);

			List<APOI> results = resultSet.getPOIs();
			double longitude = aQuery.getPoint().x;
			double latitude = aQuery.getPoint().y;
			String query = aQuery.getQuery();
			int totalResultsAvailable = resultSet.getTotalResultsAvailable();
			int totalResultsReturned = resultSet.getTotalResultsReturned();
			int firstResultPosition = DefaultValues.INIT_INT;
			double radius = DefaultValues.INIT_DOUBLE;

			for (int i = 0; i < results.size(); i++) {
				APOI point = results.get(i);
				// table 2
				setPrepItem(point, prepItem);
				prepItem.addBatch();
				// table 3
				List<Category> listCategory = point.getCategories();
				if (listCategory != null) {
					for (int j = 0; j < listCategory.size(); j++) {
						Category category = listCategory.get(j);
						setPrepCategory(point.getId(), category, prepCategory);
						prepCategory.addBatch();
					}
				}
				// table 4
				setPrepRelationship(point.getId(), queryID, i + 1,
						prepRelationship);
				prepRelationship.addBatch();
			}
			// table 1
			setPrepQuery(queryID, query, DefaultValues.INIT_INT,
					DefaultValues.INIT_INT, DefaultValues.INIT_INT, latitude,
					longitude, radius, DefaultValues.INIT_INT,
					DefaultValues.INIT_INT, totalResultsAvailable,
					totalResultsReturned, firstResultPosition, prepQuery);
			prepQuery.addBatch();

			prepItem.executeBatch();
			prepCategory.executeBatch();
			prepRelationship.executeBatch();
			prepQuery.executeBatch();

			con.commit();
			con.setAutoCommit(true);
			prepItem.close();
			prepCategory.close();
			prepQuery.close();
			prepRelationship.close();
		} catch (SQLException e) {
			// FIXME yanhui comment "the Unique index or primary key violation"
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void updataExternalDB() {
		String dbName = dbNameTarget;
		Connection con = getConnection(dbName);

		PreparedStatement prepItem;

		try {
			con.setAutoCommit(false);

			prepItem = con.prepareStatement(sqlPrepUpdateItem);
			Iterator it = Strategy.dbInMemory.poisCrawledTimes.entrySet()
					.iterator();
			int i = 0;
			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				int poiID = (Integer) entry.getKey();
				int times = (Integer) entry.getValue();
				prepItem.setInt(1, times);
				prepItem.setInt(2, poiID);
				prepItem.addBatch();
				i++;
				if (i % 1000 == 0) {
					logger.info("updating " + i);
					prepItem.executeBatch();
				}

			}
			prepItem.executeBatch();
			con.commit();
			con.setAutoCommit(true);
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public int count(String dbName, String tableName) {
		int count = 0;
		String sql = sqlSelectCountStar + tableName;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sql);
				while (rs.next()) {

					count = rs.getInt(1);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * Exam whether data has been successfully inserted to the database
	 */
	public void examData(String dbName) {
		// print
		// printQueryTable(dbName);
		// printItemTable(dbName);
		// printCategoryTable(dbName);
		// printRelationshipTable(dbName);

		// count
		int c1 = count(dbName, QUERY);
		System.out.println("count QUERY = " + c1);
		int c2 = count(dbName, ITEM);
		System.out.println("count ITEM = " + c2);
		int c3 = count(dbName, CATEGORY);
		System.out.println("count CATEGORY = " + c3);
		int c4 = count(dbName, RELATIONSHIP);
		System.out.println("count RELATIONSHIP = " + c4);
	}

	/****************************** Printing tables ******************************/

	private void printQueryTable(String dbName) {
		String sqlSelectQuery = sqlSelectStar + QUERY;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectQuery);
				while (rs.next()) {

					int queryID = rs.getInt(1);
					String query = rs.getString(2);
					int zip = rs.getInt(3);
					int results = rs.getInt(4);
					int start = rs.getInt(5);
					double latitude = rs.getDouble(6);
					double longitude = rs.getDouble(7);
					double radius = rs.getDouble(8);
					int level = rs.getInt(9);
					int parentID = rs.getInt(10);
					int totalResultsAvailable = rs.getInt(11);
					int totalResultsReturned = rs.getInt(12);
					int firstResultPosition = rs.getInt(13);

					// print query result to console
					System.out.println("queryID: " + queryID);
					System.out.println("query: " + query);
					System.out.println("zip: " + zip);
					System.out.println("results: " + results);
					System.out.println("start: " + start);
					System.out.println("latitude: " + latitude);
					System.out.println("longitude: " + longitude);
					System.out.println("radius: " + radius);
					System.out.println("level: " + level);
					System.out.println("parentID: " + parentID);
					System.out.println("totalResultsAvailable: "
							+ totalResultsAvailable);
					System.out.println("totalResultsReturned: "
							+ totalResultsReturned);
					System.out.println("firstResultPosition: "
							+ firstResultPosition);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void printItemTable(String dbName) {
		String sqlSelectItem = sqlSelectStar + ITEM;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectItem);
				while (rs.next()) {

					int itemID = rs.getInt(1);
					String title = rs.getString(2);
					String city = rs.getString(3);
					String state = rs.getString(4);

					double latitude = rs.getDouble(5);
					double longitude = rs.getDouble(6);
					double distance = rs.getDouble(7);

					double averageRating = rs.getDouble(8);
					double totalRating = rs.getDouble(9);
					double totalReviews = rs.getDouble(10);

					int numCrawler = rs.getInt(11);

					// print query result to console
					System.out.println("itemID: " + itemID);
					System.out.println("title: " + title);
					System.out.println("city: " + city);
					System.out.println("state: " + state);
					System.out.println("latitude: " + latitude);
					System.out.println("longitude: " + longitude);
					System.out.println("distance: " + distance);
					System.out.println("averageRating: " + averageRating);
					System.out.println("totalRating: " + totalRating);
					System.out.println("totalReviews: " + totalReviews);
					System.out.println("numCrawler: " + numCrawler);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void printCategoryTable(String dbName) {
		String sqlSelectCategory = sqlSelectStar + CATEGORY;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat.executeQuery(sqlSelectCategory);
				while (rs.next()) {

					int itemID = rs.getInt(1);
					int categoryID = rs.getInt(2);
					String categoryName = rs.getString(3);

					// print query result to console
					System.out.println("itemID: " + itemID);
					System.out.println("categoryID: " + categoryID);
					System.out.println("categoryName: " + categoryName);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void printRelationshipTable(String dbName) {
		String sqlSelectRelationship = sqlSelectStar + RELATIONSHIP;
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			try {
				java.sql.ResultSet rs = stat
						.executeQuery(sqlSelectRelationship);
				while (rs.next()) {

					int itemID = rs.getInt(1);
					int queryID = rs.getInt(2);
					int position = rs.getInt(3);

					// print query result to console
					System.out.println("itemID: " + itemID);
					System.out.println("queryID: " + queryID);
					System.out.println("position: " + position);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/****************************** Insert values ******************************/
	private PreparedStatement setPrepQuery(int queryID, String query, int zip,
			int results, int start, double latitude, double longitude,
			double radius, int level, int parentID, int totalResultsAvailable,
			int totalResultsReturned, int firstResultPosition,
			PreparedStatement prepQuery) {
		try {
			prepQuery.setInt(1, queryID);
			prepQuery.setString(2, query);
			prepQuery.setInt(3, zip);
			prepQuery.setInt(4, results);
			prepQuery.setInt(5, start);
			prepQuery.setDouble(6, latitude);
			prepQuery.setDouble(7, longitude);
			prepQuery.setDouble(8, radius);
			prepQuery.setInt(9, level);
			prepQuery.setInt(10, parentID);
			prepQuery.setInt(11, totalResultsAvailable);
			prepQuery.setInt(12, totalResultsReturned);
			prepQuery.setInt(13, firstResultPosition);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepQuery;
	}

	private PreparedStatement setPrepItem(APOI result,
			PreparedStatement prepItem) {
		try {
			prepItem.setInt(1, result.getId());
			prepItem.setString(2, result.getTitle());
			prepItem.setString(3, result.getCity());
			prepItem.setString(4, result.getState());
			prepItem.setDouble(5, result.getLatitude());
			prepItem.setDouble(6, result.getLongitude());
			prepItem.setDouble(7, result.getDistance());
			Rating rating = result.getRating();
			if (rating != null) {
				prepItem.setDouble(8, rating.getAverageRating());
				prepItem.setDouble(9, rating.getTotalRatings());
				prepItem.setDouble(10, rating.getTotalReviews());
			} else {
				prepItem.setDouble(8, Rating.noAverageRatingValue);
				prepItem.setDouble(9, Rating.noAverageRatingValue);
				prepItem.setDouble(10, Rating.noAverageRatingValue);
			}
			// add at 2013-9-26
			prepItem.setDouble(11, result.getNumCrawled());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepItem;
	}

	private PreparedStatement setPrepCategory(int resultID, Category category,
			PreparedStatement prepCategory) {
		try {
			prepCategory.setInt(1, resultID);
			prepCategory.setInt(2, category.getId());
			prepCategory.setString(3, category.getName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepCategory;
	}

	private PreparedStatement setPrepRelationship(int resultID, int queryID,
			int position, PreparedStatement prepRelationship) {
		try {
			prepRelationship.setInt(1, resultID);
			prepRelationship.setInt(2, queryID);
			prepRelationship.setInt(3, position);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prepRelationship;
	}

	@Override
	public void createTables(String dbName) {
		try {
			Connection conn = getConnection(dbName);
			Statement stat = conn.createStatement();
			// XXX delete tables before creating
			stat.execute(sqlDeleteQueryTable);
			stat.execute(sqlDeleteItemTable);
			stat.execute(sqlDeleteCategoryTable);
			stat.execute(sqlDeleteRelationshipTable);
			//
			stat.execute(sqlCreateQueryTable);
			stat.execute(sqlCreateItemTable);
			stat.execute(sqlCreateCategoryTable);
			stat.execute(sqlCreateRelationshipTable);
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection(String dbname) {
		if (connMap.get(dbname) == null) {
			try {
				Class.forName("org.h2.Driver");
				java.sql.Connection conn = DriverManager
						.getConnection(
								"jdbc:h2:file:"
										+ dbname
										+ ";MVCC=true;LOCK_TIMEOUT=3000000;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
								"sa", "");
				connMap.put(dbname, conn);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return (Connection) connMap.get(dbname);
	}

	/**
	 * @param dbname
	 * @deprecated See DBExternal.distroyConn();
	 */
	public void closeConnection(String dbname) {
		try {
			getConnection(dbname).close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transfer the plain text dataset to the h2 dataset
	 * 
	 * @param folderPath
	 * @param h2Name
	 *            : not in use
	 */
	public void convertFileDBToH2DB(String folderPath, String h2Name) {
		createTables(dbNameSource);
		convertQueryFile(folderPath, h2Name);
		convertResultsFile(folderPath, h2Name);
	}

	private void convertQueryFile(String folderPath, String h2Name) {
		String queryFile = folderPath + "query";
		BufferedReader brQuery = null;
		try {
			Connection conn = getConnection(dbNameSource);
			brQuery = new BufferedReader(new InputStreamReader(
					new FileInputStream(queryFile)));
			String data = null;
			String[] split;
			PreparedStatement prepQuery = conn
					.prepareStatement(sqlPrepInsertQuery);
			while ((data = brQuery.readLine()) != null) {
				data = data.trim();
				split = data.split(";");
				// query id
				String queryIDString = split[0];
				int queryID = parseID(queryIDString);

				// query Info
				String query = split[1];
				int zip = Integer.parseInt(split[2]);
				int results = Integer.parseInt(split[3]);
				int start = Integer.parseInt(split[4]);
				double latitude = Double.parseDouble(split[5]);
				double longitude = Double.parseDouble(split[6]);
				double radius = Double.parseDouble(split[7]);
				int totalResultsAvailable = Integer.parseInt(split[8]);
				int totalResultsReturned = Integer.parseInt(split[9]);
				int firstResultPosition = Integer.parseInt(split[10]);
				// additional information not download by previous data
				int level = -1;
				int parentID = -1;

				setPrepQuery(queryID, query, zip, results, start, latitude,
						longitude, radius, level, parentID,
						totalResultsAvailable, totalResultsReturned,
						firstResultPosition, prepQuery);
				prepQuery.addBatch();

			}
			prepQuery.executeBatch();
			brQuery.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	private void convertResultsFile(String folderPath, String h2Name) {
		String resultsFile = folderPath + "results";
		BufferedReader brResult = null;
		try {
			Connection conn = getConnection(dbNameSource);
			brResult = new BufferedReader(new InputStreamReader(
					new FileInputStream(resultsFile)));
			String data = null;
			String[] split;
			int position = 1;
			PreparedStatement prepItem = conn
					.prepareStatement(sqlPrepInsertItem);
			PreparedStatement prepCategory = conn
					.prepareStatement(sqlPrepInsertCategory);
			PreparedStatement prepRelationship = conn
					.prepareStatement(sqlPrepInsertRelationship);
			while ((data = brResult.readLine()) != null) {
				try {
					data = data.trim();
					split = data.split(";");
					int queryID = parseID(split[0]);
					int itemID = Integer.parseInt(split[1]);
					String title = split[2];
					// for Dominos#39;s
					if (title.equals("Dominos#39")) {
						title = "Dominos#39;s";
						String city = split[4];
						String state = split[5];
						double latitude = Double.parseDouble(split[6]);
						double longitude = Double.parseDouble(split[7]);
						double distance = Double.parseDouble(split[8]);
						List<Category> categories = new ArrayList<Category>();
						for (int i = 9; i < split.length; i = i + 2) {
							// prepare category
							Category category = new Category(
									Integer.parseInt(split[i]), split[i + 1]);
							categories.add(category);

							setPrepCategory(itemID, category, prepCategory);
							prepCategory.addBatch();
						}
						APOI result = new APOI(itemID, title, city, state,
								longitude, latitude, null, distance,
								categories, 0);

						setPrepItem(result, prepItem);
						prepItem.addBatch();
					} else {
						String city = split[3];
						String state = split[4];
						double latitude = Double.parseDouble(split[5]);
						double longitude = Double.parseDouble(split[6]);
						double distance = Double.parseDouble(split[7]);
						List<Category> categories = new ArrayList<Category>();
						for (int i = 8; i < split.length; i = i + 2) {
							// prepare category
							Category category = new Category(
									Integer.parseInt(split[i]), split[i + 1]);
							categories.add(category);

							setPrepCategory(itemID, category, prepCategory);
							prepCategory.addBatch();
						}
						APOI result = new APOI(itemID, title, city, state,
								longitude, latitude, null, distance,
								categories, 0);
						setPrepItem(result, prepItem);
						prepItem.addBatch();
					}
					//
					setPrepRelationship(itemID, queryID, position++,
							prepRelationship);
					prepRelationship.addBatch();
				} catch (Exception e) {
					System.out.println(data);
				}
			}
			// execute prepare statements...
			prepCategory.executeBatch();
			prepItem.executeBatch();
			prepRelationship.executeBatch();

			brResult.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	private int parseID(String queryIDString) {
		int queryID;
		int indexHyphenm;
		int indexDot;
		indexHyphenm = queryIDString.indexOf("-");
		if (indexHyphenm != -1) {
			queryIDString = queryIDString.substring(0, indexHyphenm);
			queryID = Integer.parseInt(queryIDString);
		} else {
			indexDot = queryIDString.indexOf(".xml");
			queryIDString = queryIDString.substring(0, indexDot);
			queryID = Integer.parseInt(queryIDString);
		}
		return queryID;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mo.umac.db.DBExternal#readFromExtenalDB(java.lang.String, java.lang.String)
	 */
	@Override
	public HashMap<Integer, APOI> readFromExtenalDB(String categoryQ,
			String stateQ) {
		HashMap<Integer, APOI> map = new HashMap<Integer, APOI>();
		try {
			Connection conn = getConnection(dbNameSource);
			Statement stat = conn.createStatement();

			// String sql = "SELECT * FROM item where state = '" + stateQ +
			// "' and itemid in (select ITEMID from CATEGORY where CATEGORYNAME = '"
			// + categoryQ + "')";
			// because the database has already been prunned before
			String sql = "select * from item";
			try {
				java.sql.ResultSet rs = stat.executeQuery(sql);
				while (rs.next()) {

					int itemID = rs.getInt(1);
					String title = rs.getString(2);
					String city = rs.getString(3);
					String state = rs.getString(4);

					double latitude = rs.getDouble(5);
					double longitude = rs.getDouble(6);
					double distance = rs.getDouble(7);

					double averageRating = rs.getDouble(8);
					double totalRating = rs.getDouble(9);
					double totalReviews = rs.getDouble(10);
					Rating rating = new Rating();
					rating.setAverageRating(averageRating);
					rating.setTotalRatings((int) totalRating);
					rating.setTotalReviews((int) totalReviews);
					int numCrawled = rs.getInt(11);
					//
					List<Category> categories = new ArrayList<Category>();
					Object searchingResult = CommonUtils.getKeyByValue(
							Strategy.categoryIDMap, categoryQ);
					if (searchingResult != null) {
						int categoryID = (Integer) searchingResult;
						new ArrayList<Category>();
						Category category = new Category(categoryID, categoryQ);
						categories.add(category);
					}

					// revised at 2013-9-27
					// transfer from lla to ecef
					// Coordinate lla = new Coordinate(longitude, latitude);
					// Coordinate ecef = ECEFLLA.lla2ecef(lla);
					// longitude = ecef.x;
					// latitude = ecef.y;
					// // transfer from miles to meters
					// distance = 1609.34 * distance;

					APOI poi = new APOI(itemID, title, city, state, longitude,
							latitude, rating, distance, categories, numCrawled);
					map.put(itemID, poi);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

	public void prun(String categoryQ, String stateQ) {
		String sql = "SELECT * FROM item where state = '"
				+ stateQ
				+ "' and itemid in (select ITEMID from CATEGORY where CATEGORYNAME = '"
				+ categoryQ + "')";
		try {
			Connection conSrc = getConnection(dbNameSource);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(dbNameTarget);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget
					.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			while (rs.next()) {

				int itemID = rs.getInt(1);
				String title = rs.getString(2);
				String city = rs.getString(3);
				String state = rs.getString(4);

				double latitude = rs.getDouble(5);
				double longitude = rs.getDouble(6);
				double distance = rs.getDouble(7);

				double averageRating = rs.getDouble(8);
				double totalRating = rs.getDouble(9);
				double totalReviews = rs.getDouble(10);
				Rating rating = new Rating();
				rating.setAverageRating(averageRating);
				rating.setTotalRatings((int) totalRating);
				rating.setTotalReviews((int) totalReviews);

				int numCrawled = rs.getInt(11);

				// write
				prepItem.setInt(1, itemID);
				prepItem.setString(2, title);
				prepItem.setString(3, city);
				prepItem.setString(4, state);
				prepItem.setDouble(5, latitude);
				prepItem.setDouble(6, longitude);
				prepItem.setDouble(7, distance);

				prepItem.setDouble(8, Rating.noAverageRatingValue);
				prepItem.setDouble(9, Rating.noAverageRatingValue);
				prepItem.setDouble(10, Rating.noAverageRatingValue);

				prepItem.setDouble(11, numCrawled);
				prepItem.addBatch();

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void prunDuplicate(String dbName, String duplicateTableName,
			String targetTableName) {
		// FIXME
		String sql = "SELECT * FROM item where itemid in (select distinct ITEMID from item)";
		try {
			Connection conSrc = getConnection(dbNameSource);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(dbNameTarget);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget
					.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			while (rs.next()) {

				int itemID = rs.getInt(1);
				String title = rs.getString(2);
				String city = rs.getString(3);
				String state = rs.getString(4);

				double latitude = rs.getDouble(5);
				double longitude = rs.getDouble(6);
				double distance = rs.getDouble(7);

				double averageRating = rs.getDouble(8);
				double totalRating = rs.getDouble(9);
				double totalReviews = rs.getDouble(10);
				Rating rating = new Rating();
				rating.setAverageRating(averageRating);
				rating.setTotalRatings((int) totalRating);
				rating.setTotalReviews((int) totalReviews);

				int numCrawled = rs.getInt(11);

				// write
				prepItem.setInt(1, itemID);
				prepItem.setString(2, title);
				prepItem.setString(3, city);
				prepItem.setString(4, state);
				prepItem.setDouble(5, latitude);
				prepItem.setDouble(6, longitude);
				prepItem.setDouble(7, distance);

				prepItem.setDouble(8, Rating.noAverageRatingValue);
				prepItem.setDouble(9, Rating.noAverageRatingValue);
				prepItem.setDouble(10, Rating.noAverageRatingValue);

				prepItem.setDouble(11, numCrawled);
				prepItem.addBatch();

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int numCrawlerPoints() {
		int c = count(dbNameTarget, ITEM);
		return c;
	}

	/**
	 * Convert H2DB to a files, only contain id, numCrawl, coordinates from the table
	 * 
	 * @param dbName
	 * @param tableName
	 * @param fileName
	 */
	public void extractValuesFromItemTable(String dbName, String tableName,
			String fileName) {
		// delete the duplicate
		Map idMap = new HashMap<Integer, Integer>();

		List<Integer> idList = new ArrayList<Integer>();
		List<Double> latList = new ArrayList<Double>();
		List<Double> longList = new ArrayList<Double>();
		List<Integer> numCrawledList = new ArrayList<Integer>();

		try {
			Connection conn = getConnection(dbNameSource);
			Statement stat = conn.createStatement();
			String sql = "SELECT ITEMID, LATITUDE, LONGITUDE, NUMCRAWLED FROM item";
			try {
				java.sql.ResultSet rs = stat.executeQuery(sql);
				while (rs.next()) {
					int itemID = rs.getInt(1);
					double latitude = rs.getDouble(2);
					double longitude = rs.getDouble(3);
					int numCrawled = rs.getInt(4);
					//
					if (!idMap.containsKey(itemID)) {
						idMap.put(itemID, 0);

						idList.add(itemID);
						latList.add(latitude);
						longList.add(longitude);
						numCrawledList.add(numCrawled);
					}
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stat.close();
			// conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// writing to the file
		File file = new File(fileName);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			int n = idList.size();
			for (int i = 0; i < n; i++) {
				int id = idList.get(i);
				double latitude = latList.get(i);
				double longitude = longList.get(i);
				int numCrawled = numCrawledList.get(i);
				bw.write(Integer.toString(id));
				bw.write(";");
				bw.write(Integer.toString(numCrawled));
				bw.write(";");
				bw.write(Double.toString(longitude));
				bw.write(";");
				bw.write(Double.toString(latitude));
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeDuplicate() {

		try {
			Connection conTarget = getConnection(dbNameTarget);
			Statement statTarget = conTarget.createStatement();
			// table 1: query table
			statTarget.execute(s01);
			statTarget.execute(s02);
			statTarget.execute(s1Query);
			statTarget.execute(s2Query);
			statTarget.execute(s3Query);
			statTarget.execute(s4Query);
			// table 2: item table
			// create tables
			statTarget.execute(s01);
			statTarget.execute(s02);
			statTarget.execute(s1Item);
			statTarget.execute(s2Item);
			statTarget.execute(s3Item);
			statTarget.execute(s4Item);
			// table 3: category table
			// XXX =0 why?
			statTarget.execute(s01);
			statTarget.execute(s02);
			statTarget.execute(s1Category);
			statTarget.execute(s2Category);
			statTarget.execute(s3Category);
			statTarget.execute(s4Category);
			// table 4: relationship table
			statTarget.execute(s01);
			statTarget.execute(s02);
			statTarget.execute(s1Relationship);
			statTarget.execute(s2Relationship);
			statTarget.execute(s3Relationship);
			statTarget.execute(s4Relationship);
			//
			statTarget.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * add at 2014-6-4
	 * 
	 * @param source
	 * @param target
	 * @param factor
	 */
	public void scale(String source, String target, double factor) {
		String sql = "select * from item";

		try {
			Connection conSrc = getConnection(source);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(target);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget
					.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			while (rs.next()) {

				int itemID = rs.getInt(1);
				String title = rs.getString(2);
				String city = rs.getString(3);
				String state = rs.getString(4);

				double latitude = rs.getDouble(5);
				double longitude = rs.getDouble(6);
				double distance = rs.getDouble(7);

				double averageRating = rs.getDouble(8);
				double totalRating = rs.getDouble(9);
				double totalReviews = rs.getDouble(10);
				Rating rating = new Rating();
				rating.setAverageRating(averageRating);
				rating.setTotalRatings((int) totalRating);
				rating.setTotalReviews((int) totalReviews);

				int numCrawled = rs.getInt(11);

				// write
				prepItem.setInt(1, itemID);
				prepItem.setString(2, title);
				prepItem.setString(3, city);
				prepItem.setString(4, state);
				prepItem.setDouble(5, latitude * factor);
				prepItem.setDouble(6, longitude * factor * (-1));
				prepItem.setDouble(7, distance);

				prepItem.setDouble(8, Rating.noAverageRatingValue);
				prepItem.setDouble(9, Rating.noAverageRatingValue);
				prepItem.setDouble(10, Rating.noAverageRatingValue);

				prepItem.setDouble(11, numCrawled);
				prepItem.addBatch();

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
			conTarget.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Sample from the database
	 * 
	 * @param factor
	 */
	public void sample(int factor) {
		String sql = sqlSelectStar + ITEM;
		try {
			Connection conSrc = getConnection(dbNameSource);
			Statement statSrc = conSrc.createStatement();

			Connection conTarget = getConnection(dbNameTarget);
			conTarget.setAutoCommit(false);
			Statement statTarget = conTarget.createStatement();
			// create tables
			statTarget.execute(sqlCreateItemTableKey);

			PreparedStatement prepItem = conTarget
					.prepareStatement(sqlPrepInsertItem);

			java.sql.ResultSet rs = statSrc.executeQuery(sql);
			while (rs.next()) {

				int itemID = rs.getInt(1);
				String title = rs.getString(2);
				String city = rs.getString(3);
				String state = rs.getString(4);

				double latitude = rs.getDouble(5);
				double longitude = rs.getDouble(6);
				double distance = rs.getDouble(7);

				double averageRating = rs.getDouble(8);
				double totalRating = rs.getDouble(9);
				double totalReviews = rs.getDouble(10);
				Rating rating = new Rating();
				rating.setAverageRating(averageRating);
				rating.setTotalRatings((int) totalRating);
				rating.setTotalReviews((int) totalReviews);

				int numCrawled = rs.getInt(11);

				Random random = new Random(System.currentTimeMillis());
				int r = random.nextInt();
				if (r % factor == 0) {
					// write
					prepItem.setInt(1, itemID);
					prepItem.setString(2, title);
					prepItem.setString(3, city);
					prepItem.setString(4, state);
					prepItem.setDouble(5, latitude);
					prepItem.setDouble(6, longitude);
					prepItem.setDouble(7, distance);

					prepItem.setDouble(8, Rating.noAverageRatingValue);
					prepItem.setDouble(9, Rating.noAverageRatingValue);
					prepItem.setDouble(10, Rating.noAverageRatingValue);

					prepItem.setDouble(11, numCrawled);
					prepItem.addBatch();
				}

			}
			rs.close();
			statSrc.close();

			prepItem.executeBatch();
			conTarget.commit();
			conTarget.setAutoCommit(true);
			statTarget.close();
			prepItem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
