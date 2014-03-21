package mo.umac.uscensus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class UScensusData {

	/**
	 * The geometry information file (.shp) for the US states. It has been download from {@link http://www.census.gov/geo/maps-data/data/tiger.html}
	 */
	public static String STATE_SHP_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
	/**
	 * The geometry information file (.dbf) for the US states. It has been download from {@link http://www.census.gov/geo/maps-data/data/tiger.html}
	 */
	public static String STATE_DBF_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.dbf";

	/**
	 * Get the list of States and Equivalent Entities' name from .dbf file See an example here: {@link http ://docs.geotools.org/latest/userguide/library
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
				stateNameList.add((String) fields[USDensity.NAME_INDEX]);
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
	 * Get the minimum boundary rectangles from the .shp file.
	 * 
	 * @param shpFileName
	 *            .shp file
	 * @return An array contains all MBRs of the areas contained in the .shp file.
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

}
