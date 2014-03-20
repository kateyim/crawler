/**
 * 
 */
package mo.umac.uscensus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.apache.log4j.Logger;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;

import utils.FileOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Kate Yim
 * 
 */
public class UScensusData {
	protected static Logger logger = Logger.getLogger(UScensusData.class.getName());

	// public static Logger logger = Logger
	// .getLogger(UScensusData.class.getName());

	/* The name index in the .dbf file */
	public final static int NAME_INDEX = 5;

	/**
	 * The geometry information file (.shp) for the US states. It has been
	 * download from {@link http://www.census.gov/geo/maps-data/data/tiger.html}
	 */
	public static String STATE_SHP_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.shp";

	/**
	 * The geometry information file (.dbf) for the US states. It has been
	 * download from {@link http://www.census.gov/geo/maps-data/data/tiger.html}
	 */
	public static String STATE_DBF_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.dbf";

	/**
	 * The road information for the US states.
	 * </p>
	 * Downloaded from {@link http://www.census.gov/cgi-bin/geo/shapefiles2013/layers.cgi}
	 */
	public static String ROAD_SHP_FILE = "../data-map/tl_2013_40_prisecroads/tl_2013_40_prisecroads.shp";

	private static final String ZIP_EXTENSION = "zip";

	private static final String SHP_EXTENSION = "shp";

	/**
	 * Get the minimum boundary rectangles from the .shp file.
	 * 
	 * @param shpFileName
	 *            .shp file
	 * @return An array contains all MBRs of the areas contained in the .shp
	 *         file.
	 */
	public static List<Envelope> MBR(String shpFileName) {
		List<Envelope> envelopeList = new LinkedList<Envelope>();
		try {
			ShpFiles shpFiles = new ShpFiles(shpFileName);
			GeometryFactory gf = new GeometryFactory();
			ShapefileReader r = new ShapefileReader(shpFiles, true, true, gf);
			while (r.hasNext()) {
				Geometry shape = (Geometry) r.nextRecord().shape();
				Envelope envelope = shape.getBoundary().getEnvelopeInternal();
				envelopeList.add(envelope);
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
		return envelopeList;
	}

	/**
	 * Get the list of States and Equivalent Entities' name from .dbf file
	 * 
	 * See an example here: {@link http
	 * ://docs.geotools.org/latest/userguide/library
	 * /data/shape.html#reading-dbf}
	 * 
	 * @param dbfFileName
	 *            .dbf file
	 * @return An array contains all names in the .dbf file
	 */
	public static List<String> stateName(String dbfFileName) {
		List<String> stateNameList = new LinkedList<String>();
		FileInputStream fis;
		try {
			fis = new FileInputStream(dbfFileName);
			DbaseFileReader dbfReader = new DbaseFileReader(fis.getChannel(), false, Charset.forName("ISO-8859-1"));

			while (dbfReader.hasNext()) {
				final Object[] fields = dbfReader.readEntry();
				stateNameList.add((String) fields[NAME_INDEX]);
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

	/**
	 * Read all roads in the .shp file.
	 */
	public static ArrayList<Coordinate[]> readRoad(String zipFolderPath, String unzipFolderPath) {
		logger.info("-----------reading roads in " + zipFolderPath);
		ArrayList<Coordinate[]> roadList = new ArrayList<Coordinate[]>();
		//
		ArrayList<String> zipFileList = (ArrayList<String>) FileOperator.traverseFolder(zipFolderPath, ZIP_EXTENSION);
		for (int i = 0; i < zipFileList.size(); i++) {
			String aZipFile = zipFileList.get(i);
			// unzip it
			FileOperator.unzip(aZipFile, unzipFolderPath, "");
		}
		// traverse again
		ArrayList<String> shpFileList = (ArrayList<String>) FileOperator.traverseFolder(unzipFolderPath, SHP_EXTENSION);

		for (int i = 0; i < shpFileList.size(); i++) {
			String shpFile = shpFileList.get(i);
			try {
				ShpFiles shpFiles = new ShpFiles(shpFile);
				GeometryFactory gf = new GeometryFactory();
				ShapefileReader r = new ShapefileReader(shpFiles, true, true, gf);
				while (r.hasNext()) {
					Geometry shape = (Geometry) r.nextRecord().shape();
					Coordinate[] coordinates = shape.getCoordinates();
					roadList.add(coordinates);
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
		return roadList;
	}
}
