package mo.umac.external.uscensus.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
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
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class UScensusDataTest {

	public static Logger logger = Logger.getLogger(UScensusDataTest.class.getName());

	public String roadFile = "../data-map/tl_2013_36_prisecroads.zip";

	public static void main(String[] args) {
		DOMConfigurator.configure(MainCrawler.LOG_PROPERTY_PATH);
		UScensusDataTest test = new UScensusDataTest();
		// test.testContaining(UScensusData.STATE_SHP_FILE_NAME,
		// UScensusData.STATE_DBF_FILE_NAME);
		// test.getEnvelope();
		test.readRoad();
	}

	public void testMBR() {
		LinkedList<Envelope> envelopeStates = (LinkedList<Envelope>) UScensusData.MBR(UScensusData.STATE_SHP_FILE_NAME);
	}

	public void testDBF() {
		LinkedList<String> nameStates = (LinkedList<String>) UScensusData.stateName(UScensusData.STATE_DBF_FILE_NAME);
	}

	/**
	 * Parse .shp files of roads
	 */
	public void readRoad() {
		try {
			ShpFiles shpFiles = new ShpFiles(roadFile);
			GeometryFactory gf = new GeometryFactory();
			ShapefileReader r = new ShapefileReader(shpFiles, true, true, gf);
			while (r.hasNext()) {
				Geometry shape = (Geometry) r.nextRecord().shape();
				// FIXME read the file line by line
			}
			r.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ShapefileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
