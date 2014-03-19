package mo.umac.db.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import mo.umac.db.yahoolocal.H2DB;
import mo.umac.main.MainCrawlerYahooLocal;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.DeleteDbFiles;

/**
 * Example of using JDBC to access H2 Database, create table, insert data, query
 * to database. Also use prepared statement, batch insert, use time stamp to
 * insert date into database, use getTimestamp to get Date data type from
 * database.
 * 
 */
public class TestH2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestH2 test = new TestH2();
		test.example3();
	}

	private void example1() {
		DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");
		Connection conn;
		try {
			conn = ds.getConnection();
			conn.createStatement().executeUpdate("CREATE TABLE data (key VARCHAR(255) PRIMARY KEY, value VARCHAR(1023) )");
			// ... populate with data, test etc
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * I prefer to this method!
	 */
	private void example3() {
		try {

			// driver for H2 db get from http://www.h2database.com
			Class.forName("org.h2.Driver");

			// create database on memory
			// Connection con =
			// DriverManager.getConnection("jdbc:h2:mem:mytest", "sa", "");
			// Connection con =
			// DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test",
			// "sa", "");
			// create database using mixed mode
			Connection con = DriverManager.getConnection("jdbc:h2:file:../example-folder/example3;AUTO_SERVER=TRUE", "sa", "");

			Statement stat = con.createStatement();

			// create table
			stat.execute("CREATE TABLE ACTIVITY (ID INTEGER, STARTTIME datetime, ENDTIME datetime,  ACTIVITY_NAME VARCHAR(200),  PRIMARY KEY (ID))");

			// prepared statement
			PreparedStatement prep = con.prepareStatement("INSERT INTO ACTIVITY (ID, STARTTIME, ENDTIME, ACTIVITY_NAME) VALUES (?,?,?,?)");

			// insert 10 row data
			for (int i = 0; i < 10; i++) {
				prep.setLong(1, i);
				prep.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				prep.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				prep.setString(4, "Activity-" + i);

				// batch insert
				prep.addBatch();
			}
			con.setAutoCommit(false);
			prep.executeBatch();
			con.setAutoCommit(true);

			// query to database
			try {
				ResultSet rs = stat.executeQuery("Select STARTTIME, ENDTIME, ACTIVITY_NAME from ACTIVITY");
				while (rs.next()) {

					Date start = rs.getTimestamp(1);
					Date end = rs.getTimestamp(2);
					String activityName = rs.getString(3);

					// print query result to console
					System.out.println("activity: " + activityName);
					System.out.println("start: " + start);
					System.out.println("end: " + end);
					System.out.println("--------------------------");
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// close connection
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int checkExample3() {
		int num = 0;
		// Class.forName("org.h2.Driver");
		//
		// Connection con = DriverManager.getConnection(
		// "jdbc:h2:file:../example-folder/example3;AUTO_SERVER=TRUE",
		// "sa", "");
		//
		// Statement stat = con.createStatement();
		//
		// // create table
		// stat.execute("select count(*) from ACTIVITY");

		return num;
	}

	private void readOnlyMode() {
		try {
			Class.forName("org.h2.Driver");
			Connection con = DriverManager.getConnection("jdbc:h2:file:../example-folder/example3;AUTO_SERVER=TRUE", "sa", "");
			con.isReadOnly();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
	 * Version 1.0, and under the Eclipse Public License, Version 1.0
	 * (http://h2database.com/html/license.html). Initial Developer: H2 Group
	 * {@link http
	 * ://h2database.googlecode.com/svn/trunk/h2/src/test/org/h2/samples
	 * /HelloWorld.java}
	 */
	private void example4() {
		// delete the database named 'test' in the user home directory
		DeleteDbFiles.execute("~", "test", true);

		try {
			Class.forName("org.h2.Driver");
			// Connection conn = DriverManager.getConnection("jdbc:h2:~/test2");
			Connection conn = DriverManager.getConnection("jdbc:h2:~/test2");
			Statement stat = conn.createStatement();

			// this line would initialize the database
			// from the SQL script file 'init.sql'
			// stat.execute("runscript from 'init.sql'");

			stat.execute("create table test(id int primary key, name varchar(255))");
			stat.execute("insert into test values(1, 'Hello')");
			ResultSet rs;
			rs = stat.executeQuery("select * from test");
			while (rs.next()) {
				System.out.println(rs.getString("name"));
			}
			stat.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testRemoveDuplicate(){
		
	}

}
