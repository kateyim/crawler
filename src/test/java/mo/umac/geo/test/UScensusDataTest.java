package mo.umac.geo.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import mo.umac.crawler.Client;
import mo.umac.geo.UScensusData;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geotools.data.shapefile.dbf.DbaseFileReader;

import com.vividsolutions.jts.geom.Envelope;

public class UScensusDataTest {

	public static Logger logger = Logger
			.getLogger(UScensusData.class.getName());

	public static void main(String[] args) {
		DOMConfigurator.configure(Client.LOG_PROPERTY_PATH);
		UScensusDataTest test = new UScensusDataTest();
		test.testContaining(UScensusData.STATE_SHP_FILE_NAME,
				UScensusData.STATE_DBF_FILE_NAME);
	}

	public void testMBR() {
		LinkedList<Envelope> envelopeStates = (LinkedList<Envelope>) UScensusData
				.MBR(UScensusData.STATE_SHP_FILE_NAME);
	}

	public void testDBF() {
		LinkedList<String> nameStates = (LinkedList<String>) UScensusData
				.stateName(UScensusData.STATE_DBF_FILE_NAME);
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
		LinkedList<Envelope> envelopeStates = (LinkedList<Envelope>) UScensusData
				.MBR(shpFileName);

		/* The latitude index in the .dbf file */
		int latIndex = 12;

		/* The longitude index in the .dbf file */
		int lonIndex = 13;

		List<String> stateNameList = new LinkedList<String>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(dbfFileName);
			DbaseFileReader dbfReader = new DbaseFileReader(fis.getChannel(),
					false, Charset.forName("ISO-8859-1"));

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
				logger.debug("city=" + fields[UScensusData.NAME_INDEX]
						+ ", lat=" + fields[latIndex] + ", lon="
						+ fields[lonIndex]);
				logger.debug("[" + envelope.getMinX() + ","
						+ envelope.getMaxX() + "," + envelope.getMinY() + ","
						+ envelope.getMaxY() + "]");
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
