package mo.umac.db.yahoolocal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import com.vividsolutions.jts.io.WKBWriter;

import mo.umac.metadata.yahoolocal.APOI;
import mo.umac.metadata.yahoolocal.AQuery;
import mo.umac.metadata.yahoolocal.ResultSet;
import mo.umac.metadata.yahoolocal.ResultSetYahooOnline;
import mo.umac.metadata.yahoolocal.YahooLocalQueryFileDB;
import mo.umac.parser.Category;
import mo.umac.parser.Rating;
import mo.umac.spatial.GeoOperator;

/**
 * H2 dataset with spatial index
 * 
 * @author kate
 * 
 */
public class H2DBGeo extends DBExternal {

    public final static String GEO_DB_NAME = "../crawler-data/yahoolocal-h2-geo/datasets";

    public static Connection conn = null;

    // table names
    private final String QUERY = "QUERY";
    private final String ITEM = "ITEM";
    private final String CATEGORY = "CATEGORY";
    private final String RELATIONSHIP = "RELATIONSHIP";

    // sqls for creating table
    /**
     * level: the divided level radius: the radius of the circle want to covered
     */
    private String sqlCreateQueryTable = "CREATE TABLE IF NOT EXISTS QUERY (QUERYID INT PRIMARY KEY, QUERY VARCHAR(100), ZIP INT, RESULTS INT, START INT, GEOM VARBINARY(200), RADIUS DOUBLE, LEVEL INT, PARENTID INT, TOTALRESULTSAVAILABLE INT, TOTALRESULTSRETURNED INT, FIRSTRESULTPOSITION INT)";
    private String sqlCreateItemTable = "CREATE TABLE IF NOT EXISTS ITEM (ITEMID INT PRIMARY KEY, TITLE VARCHAR(200), CITY VARCHAR(200), STATE VARCHAR(10), GEOM VARBINARY(200), DISTANCE DOUBLE, AVERAGERATING DOUBLE, TOTALRATINGS DOUBLE, TOTALREVIEWS DOUBLE)";
    private String sqlCreateCategoryTable = "CREATE TABLE IF NOT EXISTS CATEGORY (ITEMID INT, CATEGORYID INT, CATEGORYNAME VARCHAR(200))";
    /**
     * This table records that the item is returned by which query in which
     * position.
     */
    private String sqlCreateRelationshipTable = "CREATE TABLE IF NOT EXISTS RELATIONSHIP (ITEMID INT, QEURYID INT, POSITION INT)";

    // sqls preparation for insertion
    private String sqlPrepInsertQuery = "INSERT INTO QUERY (QUERYID, QUERY, ZIP, RESULTS, START, GEOM, RADIUS, LEVEL, PARENTID, TOTALRESULTSAVAILABLE ,TOTALRESULTSRETURNED, FIRSTRESULTPOSITION) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    private String sqlPrepInsertItem = "INSERT INTO ITEM (ITEMID, TITLE, CITY, STATE, GEOM, DISTANCE, AVERAGERATING, TOTALRATINGS, TOTALREVIEWS) VALUES (?,?,?,?,?,?,?,?,?)";
    private String sqlPrepInsertCategory = "INSERT INTO CATEGORY (ITEMID, CATEGORYID, CATEGORYNAME) VALUES (?,?,?)";
    private String sqlPrepInsertRelationship = "INSERT INTO RELATIONSHIP (ITEMID, QEURYID, POSITION) VALUES(?,?,?)";

    /**
     * sql for select all data from a table. Need concatenate the table's names.
     */
    private String sqlSelectStar = "SELECT * FROM ";

    private String sqlSelectCountStar = "SELECT COUNT(*) FROM ";

    /**
     * Transfer the plain text dataset to the h2 dataset
     * 
     * @param folderPath
     * @param h2Name
     *            : not in use
     */
    public void convertFileDBToH2DB(String folderPath, String h2Name) {
	createTables();
	convertQueryFile(folderPath, h2Name);
	convertResultsFile(folderPath, h2Name);
    }

    private void convertQueryFile(String folderPath, String h2Name) {
	String queryFile = folderPath + "query";
	BufferedReader brQuery = null;

	try {
	    Class.forName("org.h2.Driver");
	    Connection conn = DriverManager.getConnection("jdbc:h2:file:"
		    + H2DB.dbNameSource + ";AUTO_SERVER=TRUE", "sa", "");
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

		byte[] pointByte = pointWriter(longitude, latitude);
		setPrepQuery(queryID, query, zip, results, start, pointByte,
			radius, level, parentID, totalResultsAvailable,
			totalResultsReturned, firstResultPosition, prepQuery);
		prepQuery.addBatch();

	    }
	    prepQuery.executeBatch();
	    brQuery.close();
	    conn.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e1) {
	    e1.printStackTrace();
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
    }

    private void convertResultsFile(String folderPath, String h2Name) {
	String resultsFile = folderPath + "results";
	BufferedReader brResult = null;
	try {
	    Class.forName("org.h2.Driver");
	    Connection conn = DriverManager.getConnection("jdbc:h2:file:"
		    + H2DB.dbNameSource + ";AUTO_SERVER=TRUE", "sa", "");
	    brResult = new BufferedReader(new InputStreamReader(
		    new FileInputStream(resultsFile)));
	    String data = null;
	    String[] split;
	    int position = 1;
	    PreparedStatement prepCategory = conn
		    .prepareStatement(sqlPrepInsertCategory);
	    PreparedStatement prepItem = conn
		    .prepareStatement(sqlPrepInsertItem);
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
				longitude, latitude, null, distance, categories, 0);
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
				longitude, latitude, null, distance, categories, 0);
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
	    conn.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e1) {
	    e1.printStackTrace();
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
    public void writeToExternalDBFromOnline(int queryID, int level, int parentID,
	    YahooLocalQueryFileDB qc, ResultSetYahooOnline resultSet) {
	conn = createConnection();
	// prepared statement
	PreparedStatement prepQuery;
	PreparedStatement prepItem;
	PreparedStatement prepCategory;
	PreparedStatement prepRelationship;
	try {
	    prepQuery = conn.prepareStatement(sqlPrepInsertQuery);
	    prepItem = conn.prepareStatement(sqlPrepInsertItem);
	    prepCategory = conn.prepareStatement(sqlPrepInsertCategory);
	    prepRelationship = conn.prepareStatement(sqlPrepInsertRelationship);
	    List<APOI> results = resultSet.getPOIs();
	    //
	    byte[] geomByte = pointWriter(qc.getCircle().getCenter().x, qc
		    .getCircle().getCenter().y);
	    setPrepQuery(queryID, qc.getQuery(), qc.getZip(), qc.getResults(),
		    qc.getStart(), geomByte, qc.getCircle().getRadius(), level,
		    parentID, resultSet.getTotalResultsAvailable(),
		    resultSet.getTotalResultsReturned(),
		    resultSet.getFirstResultPosition(), prepQuery);
	    prepQuery.addBatch();
	    for (int i = 0; i < results.size(); i++) {
		APOI result = results.get(i);
		//
		setPrepItem(result, prepItem);
		prepItem.addBatch();
		//
		List<Category> listCategory = result.getCategories();
		for (int j = 0; j < listCategory.size(); j++) {
		    Category category = listCategory.get(j);
		    setPrepCategory(result.getId(), category, prepCategory);
		    prepCategory.addBatch();
		}
		//
		setPrepRelationship(result.getId(), queryID, i + 1,
			prepRelationship);
		prepRelationship.addBatch();
	    }
	    conn.setAutoCommit(false);
	} catch (SQLException e) {
	    e.printStackTrace();
	}

    }

    private static Connection createConnection() {
	try {
	    Class.forName("org.h2.Driver");
	    String connectString = "jdbc:h2:" + GEO_DB_NAME
		    + ";AUTO_SERVER=TRUE";
	    conn = DriverManager.getConnection(connectString, null, null);
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return conn;
    }

    public void createTables() {
	try {
	    conn = createConnection();
	    Statement stat = conn.createStatement();
	    stat.execute(sqlCreateQueryTable);
	    stat.execute(sqlCreateItemTable);
	    stat.execute(sqlCreateCategoryTable);
	    stat.execute(sqlCreateRelationshipTable);
	    stat.close();
	    conn.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    /**
     * transfer the coordinate to byte
     * 
     * @param longitude
     * @param latitude
     * @return
     */
    private byte[] pointWriter(double longitude, double latitude) {
	GeometryFactory geomFactory = new GeometryFactory();
	CoordinateArraySequenceFactory factory = CoordinateArraySequenceFactory
		.instance();
	Coordinate[] coord = new Coordinate[1];
	WKBWriter writer = new WKBWriter();
	coord[0] = new Coordinate(longitude, latitude);
	byte[] geomByte = writer.write(new Point(factory.create(coord),
		geomFactory));
	return geomByte;
    }

    private PreparedStatement setPrepQuery(int queryID, String query, int zip,
	    int results, int start, byte[] geomByte, double radius, int level,
	    int parentID, int totalResultsAvailable, int totalResultsReturned,
	    int firstResultPosition, PreparedStatement prepQuery) {
	try {
	    prepQuery.setInt(1, queryID);
	    prepQuery.setString(2, query);
	    prepQuery.setInt(3, zip);
	    prepQuery.setInt(4, results);
	    prepQuery.setInt(5, start);
	    prepQuery.setBytes(6, geomByte);
	    prepQuery.setDouble(7, radius);
	    prepQuery.setInt(8, level);
	    prepQuery.setInt(9, parentID);
	    prepQuery.setInt(10, totalResultsAvailable);
	    prepQuery.setInt(11, totalResultsReturned);
	    prepQuery.setInt(12, firstResultPosition);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return prepQuery;
    }

    private PreparedStatement setPrepItem(APOI result, PreparedStatement prepItem) {
	try {
	    prepItem.setInt(1, result.getId());
	    prepItem.setString(2, result.getTitle());
	    prepItem.setString(3, result.getCity());
	    prepItem.setString(4, result.getState());
	    byte[] pointByte = pointWriter(result.getLongitude(),
		    result.getLatitude());
	    prepItem.setBytes(5, pointByte);
	    prepItem.setDouble(6, result.getDistance());
	    Rating rating = result.getRating();
	    if (rating != null) {
		prepItem.setDouble(7, rating.getAverageRating());
		prepItem.setDouble(8, rating.getTotalRatings());
		prepItem.setDouble(9, rating.getTotalReviews());
	    } else {
		prepItem.setDouble(7, Rating.noAverageRatingValue);
		prepItem.setDouble(8, Rating.noAverageRatingValue);
		prepItem.setDouble(9, Rating.noAverageRatingValue);
	    }
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
    public void init() {
	// TODO Auto-generated method stub

    }


    @Override
    public HashMap<Integer, APOI> readFromExtenalDB(String category,
	    String state) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void writeToExternalDB(int queryID, AQuery query, ResultSet resultSet) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void createTables(String dbNameTarget) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public int numCrawlerPoints() {
	// TODO Auto-generated method stub
	return 0;
    }

	@Override
	public void updataExternalDB() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeDuplicate() {
		// TODO Auto-generated method stub
		
	}
}
