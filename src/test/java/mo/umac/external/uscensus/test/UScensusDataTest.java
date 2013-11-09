package mo.umac.external.uscensus.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mo.umac.crawler.MainCrawler;
import mo.umac.external.uscensus.UScensusData;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class UScensusDataTest {

	public static Logger logger = Logger.getLogger(UScensusDataTest.class.getName());

	public String roadFolder = "../data-map/tl_2013_36_prisecroads/";
	public String roadShpFile = "../data-map/tl_2013_40_prisecroads/tl_2013_40_prisecroads.shp";

	public static void main(String[] args) {
		DOMConfigurator.configure(MainCrawler.LOG_PROPERTY_PATH);
		UScensusDataTest test = new UScensusDataTest();
		// test.testContaining(UScensusData.STATE_SHP_FILE_NAME,
		// UScensusData.STATE_DBF_FILE_NAME);
		// test.getEnvelope();
		test.testDensity();
	}

	public void testMBR() {
		LinkedList<Envelope> envelopeStates = (LinkedList<Envelope>) UScensusData.MBR(UScensusData.STATE_SHP_FILE_NAME);
	}

	public void testDBF() {
		LinkedList<String> nameStates = (LinkedList<String>) UScensusData.stateName(UScensusData.STATE_DBF_FILE_NAME);
	}

	public void testRoadDensity() {
		// TODO
		UScensusData.readRoad(roadFolder);
	}

	public void testDensity() {
		double granularityX = 1;
		double granularityY = 1;
		Envelope envelope = new Envelope(4, 9, 7, 11);
		ArrayList<Coordinate[]> roadList = generateRoadList(envelope);
		double[][] density = UScensusData.densityList(envelope, granularityX, granularityY, roadList);
		for (int i = 0; i < density.length; i++) {
			double[] ds = density[i];
			for (int j = 0; j < ds.length; j++) {
				double d = ds[j];
				logger.debug(d);

			}

		}
	}

	public ArrayList<Coordinate[]> generateRoadList(Envelope envelope) {
		ArrayList<Coordinate[]> roadList = new ArrayList<Coordinate[]>();
		//
		Coordinate p1 = new Coordinate(6.8, 9.5);
		Coordinate q11 = new Coordinate(6.2, 9.2);
		Coordinate[] aRoad1 = { p1, q11 };
		roadList.add(aRoad1);
		//
		Coordinate p2 = new Coordinate(6.5, 10.5);
		Coordinate q21 = new Coordinate(7.5, 9.5);
		Coordinate[] aRoad2 = { p2, q21 };
		roadList.add(aRoad2);
		//
		Coordinate p3 = new Coordinate(5.2, 9.5);
		Coordinate q31 = new Coordinate(5.3, 10.6);
		Coordinate q32 = new Coordinate(6.3, 10.4);
		Coordinate q33 = new Coordinate(5.7, 8.8);
		Coordinate q34 = new Coordinate(7.4, 7.5);
		Coordinate q35 = new Coordinate(4.5, 8.6);
		Coordinate q36 = new Coordinate(5.5, 9.2);
		Coordinate[] aRoad3 = { p3, q31, q32, q33, q34, q35, q36 };
		roadList.add(aRoad3);
		return roadList;
	}

	public void testing() {
		File file = new File("mayshapefile.shp");

		try {
			Map connect = new HashMap();
			connect.put("url", file.toURL());

			DataStore dataStore = DataStoreFinder.getDataStore(connect);
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];

			System.out.println("Reading content " + typeName);

			FeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureCollection collection = featureSource.getFeatures();
			FeatureIterator iterator = collection.features();

			try {
				while (iterator.hasNext()) {
					// Feature feature = iterator.next();
					// Geometry sourceGeometry = feature.getDefaultGeometry();
				}
			} finally {
				iterator.close();
			}

		} catch (Throwable e) {
		}
	}

	public Envelope getEnvelope() {
		Envelope envelope = null;

		LinkedList<Envelope> allEnvelopeStates = (LinkedList<Envelope>) UScensusData.MBR(UScensusData.STATE_SHP_FILE_NAME);
		LinkedList<String> allNameStates = (LinkedList<String>) UScensusData.stateName(UScensusData.STATE_DBF_FILE_NAME);

		// select the specified states according to the listNameStates
		String specifiedName = "NY";
		for (int j = 0; j < allNameStates.size(); j++) {
			String name = allNameStates.get(j);
			if (name.equals(specifiedName)) {
				envelope = allEnvelopeStates.get(j);
			}
		}
		System.out.println(envelope.toString());
		return envelope;
	}

	/**
	 * Test whether the order of city names extracted from the dbfFile
	 * corresponding to the order of envelopes extracted from the shpFile.
	 * 
	 * @param shpFileName
	 * @param dbfFileName
	 * @return
	 */
	public List<String> testContaining(String shpFileName, String dbfFileName) {
		LinkedList<Envelope> envelopeStates = (LinkedList<Envelope>) UScensusData.MBR(shpFileName);

		/* The latitude index in the .dbf file */
		int latIndex = 12;

		/* The longitude index in the .dbf file */
		int lonIndex = 13;

		List<String> stateNameList = new LinkedList<String>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(dbfFileName);
			DbaseFileReader dbfReader = new DbaseFileReader(fis.getChannel(), false, Charset.forName("ISO-8859-1"));

			int i = 0;
			double lat = 0, lon = 0;
			while (dbfReader.hasNext()) {
				final Object[] fields = dbfReader.readEntry();
				stateNameList.add((String) fields[UScensusData.NAME_INDEX]);
				Envelope envelope = envelopeStates.get(i);
				lat = Double.parseDouble(fields[latIndex].toString());
				lon = Double.parseDouble(fields[lonIndex].toString());
				if (!envelope.contains(lon, lat)) {
					logger.debug("----------------Not Containing!");
				} else {
					logger.debug("----------------Containing");
				}
				logger.debug("city=" + fields[UScensusData.NAME_INDEX] + ", lat=" + fields[latIndex] + ", lon=" + fields[lonIndex]);
				logger.debug("[" + envelope.getMinX() + "," + envelope.getMaxX() + "," + envelope.getMinY() + "," + envelope.getMaxY() + "]");
				i++;
			}

			dbfReader.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stateNameList;
	}

}
