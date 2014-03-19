package mo.umac.crawler.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import mo.umac.db.yahoolocal.H2DB;
import mo.umac.external.uscensus.UScensusData;
import mo.umac.main.MainCrawlerYahooLocal;
import mo.umac.spatial.ECEFLLA;
import mo.umac.spatial.GeoOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * We find that the number of points crawled is less than the total number of
 * points in the database. Check where is wrong!
 * 
 * @author kate
 * 
 */
public class CheckUnCrawledPoints {

    /**
     * Check the first error that the number of points crawled in the memory not
     * equals to the total number stored in the database.
     * 
     */
    public void compareMemeoryAndDB() {
	String file56922 = "./src/test/resources/crawlerTestFiles/56922";
	String file54738 = "./src/test/resources/crawlerTestFiles/54738";
	// FIMXE !!! check which id didn't write into the external db
	Set set56922 = new TreeSet();
	Set set54738 = new TreeSet();

	//
	File file = new File(file56922);
	String pre56922 = "INFO (mo.umac.crawler.offline.OfflineStrategy:149)- ";
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(new FileInputStream(
		    file56922)));
	    String data = null;
	    while ((data = br.readLine()) != null) {
		data = data.trim();
		String numString = data.substring(pre56922.length());
		int num = Integer.parseInt(numString);
		set56922.add(num);
	    }
	    br.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	System.out.println("set56922 = " + set56922.size());
	//
	numOfTuplesInExternalDB(set54738);
	System.out.println("set54738 = " + set54738.size());
	// find the differences
	Set setD = new TreeSet();
	Iterator it = set56922.iterator();
	while (it.hasNext()) {
	    int id = (Integer) it.next();
	    if (!set54738.contains(id)) {
		setD.add(id);
	    }
	}
	//
	System.out.println("number of missing ids is: " + setD.size());
	it = setD.iterator();
	while (it.hasNext()) {
	    int id = (Integer) it.next();
	    System.out.println(id);
	}

    }

    public int numOfTuplesInExternalDB(Set set) {
	if (set == null) {
	    set = new TreeSet();
	}
	H2DB h2db = new H2DB();
	String dbName = MainCrawlerYahooLocal.DB_NAME_TARGET;
	Connection conn = h2db.getConnection(dbName);
	try {
	    Statement stat = conn.createStatement();
	    String sql = "select distinct itemid from item";
	    java.sql.ResultSet rs = stat.executeQuery(sql);
	    while (rs.next()) {
		int i = rs.getInt(1);
		set.add(i);
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return set.size();
    }

    /**
     * For the second error: there are points not crawled by the upper bound
     * algorithm.
     * 
     * @return
     */
    public List findMissingPoints() {
	H2DB h2db = new H2DB();
	// source list
	List sourceList = new ArrayList<Integer>();
	String sourceDB = MainCrawlerYahooLocal.DB_NAME_SOURCE;
	Connection conn = h2db.getConnection(sourceDB);
	try {
	    Statement stat = conn.createStatement();
	    String sql = "SELECT itemid FROM item where state = '"
		    + "NY"
		    + "' and itemid in (select ITEMID from CATEGORY where CATEGORYNAME = '"
		    + "Restaurants" + "')";
	    java.sql.ResultSet rs = stat.executeQuery(sql);
	    while (rs.next()) {
		int i = rs.getInt(1);
		sourceList.add(i);
	    }
	    stat.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	System.out.println("source: " + sourceList.size());
	// target list
	Set targetSet = new HashSet<Integer>();
	String targetDB = MainCrawlerYahooLocal.DB_NAME_TARGET;
	conn = h2db.getConnection(targetDB);
	try {
	    Statement stat = conn.createStatement();
	    String sql = "select distinct itemid from item";
	    java.sql.ResultSet rs = stat.executeQuery(sql);
	    while (rs.next()) {
		int i = rs.getInt(1);
		targetSet.add(i);
	    }
	    stat.close();
	    conn.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	System.out.println("target: " + targetSet.size());
	// missing list
	List missingList = new ArrayList<Integer>();
	Iterator it = sourceList.iterator();
	while (it.hasNext()) {
	    int id = (Integer) it.next();
	    if (!targetSet.contains(id)) {
		missingList.add(id);
	    }
	}
	System.out.println("missing: " + missingList.size());
	return missingList;
    }

    public Map getCoordinates(List idList) {
	Map map = new HashMap<Integer, Coordinate>();
	H2DB h2db = new H2DB();
	// source list
	String sourceDB = MainCrawlerYahooLocal.DB_NAME_SOURCE;
	Connection conn = h2db.getConnection(sourceDB);
	try {
	    Statement stat = conn.createStatement();
	    for (int i = 0; i < idList.size(); i++) {
		int id = (Integer) idList.get(i);
		String sql = "SELECT LATITUDE, LONGITUDE FROM item where itemid = "
			+ id;
		java.sql.ResultSet rs = stat.executeQuery(sql);
		while (rs.next()) {
		    double latitude = rs.getDouble(1);
		    double longitude = rs.getDouble(2);
		    Coordinate lla = new Coordinate(longitude, latitude);
//		    Coordinate ecef = ECEFLLA.lla2ecef(lla);
		    map.put(id, lla);
		}

	    }
	    System.out.println("map: " + map.size());
	    stat.close();
	    conn.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return map;
    }

    public Envelope getMBR() {
	String stateName = "NY";

	LinkedList<Envelope> allEnvelopeStates = (LinkedList<Envelope>) UScensusData
		.MBR(UScensusData.STATE_SHP_FILE_NAME);
	LinkedList<String> allNameStates = (LinkedList<String>) UScensusData
		.stateName(UScensusData.STATE_DBF_FILE_NAME);

	// select the specified states according to the listNameStates
	for (int j = 0; j < allNameStates.size(); j++) {
	    String name = allNameStates.get(j);
	    if (name.equals(stateName)) {
		Envelope envelopeStateLLA = allEnvelopeStates.get(j);
//		Envelope envelopeStateECEF = GeoOperator
//			.lla2ecef(envelopeStateLLA);
		return envelopeStateLLA;
	    }
	}
	return null;
    }

    public void outOfBoundary(Envelope envelope, Map map) {
	System.out.println("testing contain");
	int num = 0;
	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Entry entry = (Entry) it.next();
	    int id = (Integer) entry.getKey();
	    Coordinate p = (Coordinate) entry.getValue();
	    if (envelope.contains(p)) {
//		System.out.println(id + ", [" + p.x + ", " + p.y + "]");
		num++;
	    }
	}
	System.out.println("containing: " + num);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	CheckUnCrawledPoints ccp = new CheckUnCrawledPoints();
	// ccp.compareMemeoryAndDB();
	// int number = ccp.numOfTuplesInExternalDB(null);
	// System.out.println(number);
	Envelope envelope = ccp.getMBR();
	System.out.println(envelope.toString());
	List missingList = ccp.findMissingPoints();
	Map map = ccp.getCoordinates(missingList);
	ccp.outOfBoundary(envelope, map);
	// Iterator it = map.entrySet().iterator();
	// while(it.hasNext()){
	// Entry entry = (Entry)it.next();
	// int id = (Integer)entry.getKey();
	// Coordinate coordinate = (Coordinate)entry.getValue();
	// System.out.println(id + ", [" + coordinate.x + ", " + coordinate.y +
	// "]");
	// }

    }

}
