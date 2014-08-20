package mo.umac.uscensus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mo.umac.analytics.Cluster;
import mo.umac.crawler.MainYahoo;
import mo.umac.crawler.Strategy;
import mo.umac.db.DBInMemory;
import mo.umac.db.H2DB;
import mo.umac.metadata.APOI;
import mo.umac.metadata.Rating;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import paint.PaintShapes;
import utils.FileOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Because we don't have road information from US Census website, so we use the original points to partition the map
 * 
 * @author Kate
 * 
 */
public class USDensityByPoints {
	public static Logger logger = Logger.getLogger(USDensityByPoints.class.getName());

	/************** UT *****************/
	private static Envelope envelope = new Envelope(-114.052998, -109.04105799999999, 36.997949, 42.001618);
	public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ut-prun";
	private static String densityFile = "../data-experiment/partition/densityMap-ut-0.01";
//	private static String dentiestRegionFile = "../data-experiment/partition/combinedDensity-ut-0.mbr";
	public static String clusterRegionFile = "../data-experiment/partition/combinedDensity-ut.mbr";

	/************** OK *****************/
	// private static Envelope envelope = new Envelope(-103.002455, -94.430662, 33.615787 , 37.002311999999996);
	// public final static String DB_NAME_SOURCE = "../data-experiment/yahoo/ok-prun";

	private static String category = "Restaurants";
	public static String CATEGORY_ID_PATH = "./src/main/resources/cat_id.txt";
	public final static String DB_NAME_TARGET = "../data-experiment/target";

	private static double granularityX = 0.01;
	private static double granularityY = 0.01;

	private ArrayList<double[]> density;


	public static void main(String[] args) {
		boolean debug = false;
		MainYahoo.shutdownLogs(debug);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
		USDensityByPoints test = new USDensityByPoints();
		test.computeDensityInEachGrids();
	}

	/** compute the density on the map, run only once for a state folder */
	public void computeDensityInEachGrids() {
		// read from h2 db
		HashMap<Integer, APOI> points = readPoints();
		// compute density
		double[][] density1 = densityList(envelope, granularityX, granularityY, points);
		USDensity.writeDensityToFile(density1, densityFile);
		// forYahooUT();
	}

	public static void forYahooUT() {
		ArrayList<double[]> densityAll = USDensity.readDensityFromFile(densityFile);
		// only add 1 envelope right now
		ArrayList<Envelope> envelopeList = addEnvelopeList();
		ArrayList<Envelope> results = new ArrayList<Envelope>();
		int numDense = 1;
		double a = 0.8;
		for (int i = 0; i < envelopeList.size(); i++) {
			Envelope e = envelopeList.get(i);
			ArrayList<double[]> density = USDensity.readPartOfDensity(densityAll, e);
			
			ArrayList<Envelope> denseEnvelopList = Cluster.clusterZero(granularityX, granularityY, e, density, a, numDense);
			ArrayList<Envelope> partitionedRegions = Cluster.partition(e, denseEnvelopList);
			results.addAll(partitionedRegions);
		}
		// before writing, has changed to latitude & longitude
		USDensity.writePartition(clusterRegionFile, results);
	}


	private static ArrayList<Envelope> addEnvelopeList() {
		ArrayList<Envelope> envelopeList = new ArrayList<Envelope>();
		envelopeList.add(envelope);
		return envelopeList;
	}

	private double[][] densityList(Envelope envelope2, double granularityX, double granularityY, HashMap<Integer, APOI> points) {
		logger.info("-------------computing unit density-------------");
		double width = envelope.getWidth();
		double height = envelope.getHeight();
		double minX = envelope.getMinX();
		double minY = envelope.getMinY();

		// the number of grids, begin from 0;
		int countX = (int) Math.ceil(width / granularityX);
		int countY = (int) Math.ceil(height / granularityY);
		logger.info("countX = " + countX);
		logger.info("countY = " + countY);
		// initialize to 0.0;
		double[][] density = new double[countX][countY];

		Iterator it2 = points.entrySet().iterator();
		while (it2.hasNext()) {
			Entry entry = (Entry) it2.next();
			APOI aPoint = (APOI) entry.getValue();
			Coordinate p = aPoint.getCoordinate();
			//
			int pGridX = (int) Math.floor((Math.abs(p.x - minX) / granularityX));
			int pGridY = (int) Math.floor((Math.abs(p.y - minY) / granularityY));

			density[pGridX][pGridY]++;

		}
		return density;
	}

	private HashMap<Integer, APOI> readPoints() {
		Strategy.categoryIDMap = FileOperator.readCategoryID(CATEGORY_ID_PATH);
		// source database
		Strategy.dbExternal = new H2DB(DB_NAME_SOURCE, DB_NAME_TARGET);
		Strategy.dbInMemory = new DBInMemory();
		DBInMemory.pois = readFromGeneratedDB(DB_NAME_SOURCE);
		return DBInMemory.pois;
	}

	/**
	 * Copy from CrawlerTest
	 * 
	 * @param dbNameSource
	 * @return
	 */
	private HashMap<Integer, APOI> readFromGeneratedDB(String dbNameSource) {
		HashMap<Integer, APOI> map = new HashMap<Integer, APOI>();
		H2DB h2 = (H2DB) Strategy.dbExternal;
		String dbName = Strategy.dbExternal.dbNameSource;
		// TODO check sql
		try {
			Connection conn = h2.getConnection(dbName);
			Statement stat = conn.createStatement();

			String sql = "SELECT * FROM item";
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
					// print query result to console
					logger.debug("itemID: " + itemID);
					logger.debug("latitude: " + latitude);
					logger.debug("longitude: " + longitude);
					logger.debug("--------------------------");
					APOI poi = new APOI(itemID, title, city, state, longitude, latitude, rating, distance, null, numCrawled);
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

}
