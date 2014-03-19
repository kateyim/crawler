package mo.umac.rtree.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import mo.umac.db.yahoolocal.H2DB;
import mo.umac.db.yahoolocal.Postgresql;
import mo.umac.main.CrawlerStrategy;
import mo.umac.main.MainCrawlerYahooLocal;
import mo.umac.rtree.MyRTree;

import org.apache.log4j.xml.DOMConfigurator;
import org.postgis.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class MyRTreeTest {

	public String dbName = "test";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure(MainCrawlerYahooLocal.LOG_PROPERTY_PATH);

		MyRTreeTest test = new MyRTreeTest();

		// test 1
		test.testKNNQuery();
		// testing 2
		// test.testContainsEnvelope();
		// test 3
		// test.testContainsPoint();

	}

	public void testKNNQuery() {
		List<Coordinate> coors = generatePoints();

		// List<Coordinate> coors = importPoints();

		// System.out.println(coors.size());
		// for (int i = 0; i < coors.size(); i++) {
		// Coordinate coor = coors.get(i);
		// System.out.println("[" + coor.x + ", " + coor.y + "]");
		// }

		// test.storeInPostgresql(coors);

		MyRTree rtree = new MyRTree(coors);
		Coordinate searchPoint = new Coordinate(30.6, 45.0);
		int k = 10;
		List<Integer> results = rtree.searchNN(searchPoint, k);
		System.out.println("result size = " + results.size());
		// for (int i = 0; i < results.size(); i++) {
		// System.out.println(results.get(i).toString());
		// }

		for (int i = 0; i < results.size(); i++) {
			int id = results.get(i);
			Coordinate p = coors.get(id);
			System.out.println(id + ":" + p.toString());
		}
	}

	public void testContainsEnvelope() {
		// MyRTree rtree = new MyRTree();
		int i = 0;
		Envelope e1 = new Envelope(2, 4, 0, 10);
		MyRTree.rtree.addRectangle(i++, e1);

		e1 = new Envelope(3, 6, 0, 10);
		MyRTree.rtree.addRectangle(i++, e1);

		e1 = new Envelope(7, 9, 3, 5);
		MyRTree.rtree.addRectangle(i++, e1);

		e1 = new Envelope(8, 10, 1, 4);
		MyRTree.rtree.addRectangle(i++, e1);

		e1 = new Envelope(8, 10, 6, 7);
		MyRTree.rtree.addRectangle(i++, e1);

		// 1. whether the first two have been merged?
		// 2. whether the envelope below is covered by the previous rectangle?

		e1 = new Envelope(8.1, 8.5, 2, 4.5);

		boolean contain = MyRTree.rtree.contains(e1);
		System.out.println(contain);
		// boolean b = rtree.contains(e1);
		// System.out.println(b);
	}

	public void testContainsPoint() {
		int i = 0;
		Envelope e1 = new Envelope(2, 4, 0, 10);
		MyRTree.rtree.addRectangle(i++, e1);
		CrawlerStrategy.rectangleId++;

		e1 = new Envelope(3, 6, 0, 10);
		MyRTree.rtree.addRectangle(i++, e1);
		CrawlerStrategy.rectangleId++;

		e1 = new Envelope(7, 9, 3, 5);
		MyRTree.rtree.addRectangle(i++, e1);
		CrawlerStrategy.rectangleId++;

		e1 = new Envelope(8, 10, 1, 4);
		MyRTree.rtree.addRectangle(i++, e1);
		CrawlerStrategy.rectangleId++;

		e1 = new Envelope(8, 10, 6, 7);
		MyRTree.rtree.addRectangle(i++, e1);
		CrawlerStrategy.rectangleId++;

		// 1. whether the first two have been merged?
		// 2. whether the envelope below is covered by the previous rectangle?

		Coordinate p = new Coordinate(7, 2.5);

		boolean contain = MyRTree.rtree.contains(p);
		System.out.println(contain);
	}

	private List<Coordinate> importPoints() {
		Postgresql post = new Postgresql();
		List<Coordinate> coords = new ArrayList<Coordinate>();
		Connection conn = post.connect(dbName);
		Statement s;
		try {
			s = conn.createStatement();
			ResultSet r = s.executeQuery("select * from points");
			while (r.next()) {
				int id = r.getInt(1);
				float x = r.getFloat(2);
				float y = r.getFloat(3);
				Coordinate coor = new Coordinate(x, y);
				coords.add(coor);
			}
			s.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return coords;
	}

	public List<Coordinate> generatePoints() {
		int numPoint = 100;
		double x = 1.0;
		double y = 1.0;
		List list = new ArrayList<Coordinate>();
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < numPoint; i++) {
			x = random.nextDouble() * 100;
			y = random.nextDouble() * 100;
			Coordinate coordinate = new Coordinate(x, y);
			list.add(coordinate);
		}
		return list;
	}

	public void storeInPostgresql(List<Coordinate> coors) {
		Postgresql postgresql = new Postgresql();
		postgresql.DB_NAME = "test";

		String sql = "create table if not exists points (itemid int primary key, " + "longitude float, latitude float)";
		String sqlAddGeom = "SELECT AddGeometryColumn ('points','geom'," + 4326 + ",'POINT',2)";
		postgresql.createTable(sql, sqlAddGeom);
		String sqlPrep = "insert into points (itemid, longitude, latitude, geom) values (?,?,?,ST_SetSRID(ST_MakePoint(?, ?), 4326))";
		exportData(sqlPrep, coors);

	}

	public void exportData(String sqlPrep, List<Coordinate> coors) {
		Postgresql postgresql = new Postgresql();

		Connection con = postgresql.connect(dbName);
		// prepared statement
		PreparedStatement prepItem;
		try {
			con.setAutoCommit(false);
			prepItem = con.prepareStatement(sqlPrep);
			for (int i = 0; i < coors.size(); i++) {
				Coordinate coor = coors.get(i);
				//
				prepItem.setInt(1, i);
				prepItem.setFloat(2, (float) coor.x);
				prepItem.setFloat(3, (float) coor.y);
				prepItem.setFloat(4, (float) coor.x);
				prepItem.setFloat(5, (float) coor.y);

				prepItem.addBatch();
				if (i % 50 == 0) {
					prepItem.executeBatch();
				}
			}
			prepItem.executeBatch();
			con.commit();
			prepItem.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
