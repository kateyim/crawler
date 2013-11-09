/**
 * 
 */
package mo.umac.external.uscensus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mo.umac.utils.FileOperator;

import org.apache.log4j.Logger;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;

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

	private static final String ZIP_EXTENSION = ".zip";

	private static final String SHP_EXTENSION = ".shp";

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
	public static List<Coordinate[]> readRoad(String folderPath) {
		List<Coordinate[]> roadList = new ArrayList<Coordinate[]>();
		// FIXME test
		ArrayList<String> zipFileList = (ArrayList<String>) FileOperator.traverseFolder(folderPath, ZIP_EXTENSION);
		// FIXME if compressed, uncompress them

		// FIXME traverse again
		ArrayList<String> shpFileList = (ArrayList<String>) FileOperator.traverseFolder(folderPath, SHP_EXTENSION);

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

	public static double density() {
		return 0.0;
	}

	/**
	 * Compute the density in the map
	 * 
	 * @param envelope
	 * @param granularityX
	 * @param granularityY
	 * @param roadList
	 * @return
	 */
	public static double[][] densityList(Envelope envelope, double granularityX, double granularityY, ArrayList<Coordinate[]> roadList) {
		double width = envelope.getWidth();
		double height = envelope.getHeight();
		double minX = envelope.getMinX();
		double minY = envelope.getMinY();

		int countX = (int) Math.ceil(width / granularityX);
		int countY = (int) Math.ceil(height / granularityY);
		// initial to all 0.0;
		double[][] density = new double[countX + 1][countY + 1];
		double totalLength = 0.0;
		//
		for (int i = 0; i < roadList.size(); i++) {
			Coordinate[] aPartRoad = roadList.get(i);
			Coordinate p = aPartRoad[0];
			int pGridX = (int) Math.ceil(Math.abs(p.x - minX) / granularityX);
			int pGridY = (int) Math.ceil(Math.abs(p.y - minY) / granularityY);

			for (int j = 1; j < aPartRoad.length; j++) {

				if (logger.isDebugEnabled()) {
					logger.debug("----------a part of road-----------");
					logger.debug("p: " + p.toString());
					logger.debug("pGridX = " + pGridX);
					logger.debug("pGridY = " + pGridY);
				}
				Coordinate q = aPartRoad[j];
				int qGridX = (int) Math.ceil(Math.abs(q.x - minX) / granularityX);
				int qGridY = (int) Math.ceil(Math.abs(q.y - minY) / granularityY);

				if (logger.isDebugEnabled()) {
					logger.debug("q: " + q.toString());
					logger.debug("qGridX = " + qGridX);
					logger.debug("qGridY = " + qGridY);
				}

				// This is the easiest case
				if (pGridX == qGridX && pGridY == qGridY) {
					// the p and the q point belong to the same small square
					double length = p.distance(q);
					density[pGridX][pGridY] += length;
					totalLength += length;
					if (logger.isDebugEnabled()) {
						logger.debug("case 0: in the same grid");
						logger.debug("p.distance(q) : " + p.toString() + "," + q.toString() + " = " + length);
						logger.debug("density[" + pGridX + "][" + pGridY + "]: ");
					}
					p = new Coordinate(q);
					pGridX = qGridX;
					pGridY = qGridY;
					continue;
				}
				// Now we are going to deal with the situation when the p and q point of this road are located on two different grids
				// slope of the line
				double slope = (q.y - p.y) / (q.x - p.x);
				if (logger.isDebugEnabled()) {
					logger.debug("slope = " + slope);
				}
				// flag
				double xDirect = 1;
				double yDirect = 1;
				if (p.x > q.x) {
					xDirect = -1;
				}
				if (p.y > q.y) {
					yDirect = -1;
				}
				// the first line y
				double yLine;
				if (yDirect == 1) {
					yLine = minY + pGridY * granularityY;
				} else {
					yLine = minY + (pGridY - 1) * granularityY;
				}
				// the first line x
				double xLine;
				if (xDirect == 1) {
					xLine = minX + pGridX * granularityX;
				} else {
					xLine = minX + (pGridX - 1) * granularityX;
				}
				//
				int numCrossGridX = Math.abs(qGridX - pGridX);
				int numCrossGridY = Math.abs(qGridY - pGridY);

				if (logger.isDebugEnabled()) {
					logger.debug("xDirect = " + xDirect);
					logger.debug("yDirect = " + yDirect);
					logger.debug("yLine = " + yLine);
					logger.debug("xLine = " + xLine);
					logger.debug("numCrossGridX = " + numCrossGridX);
					logger.debug("numCrossGridY = " + numCrossGridY);
				}

				if (numCrossGridX == 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("case 1");
					}
					// case 1: this road only intersect with different grids on y-axis
					for (int k2 = pGridY, ki = 0; ki < numCrossGridY; k2 += yDirect, ki++) {
						// compute the intersect points
						double x = (yLine - p.y) / slope + p.x;
						Coordinate pointOnLine = new Coordinate(x, yLine);
						double length = p.distance(pointOnLine);
						density[pGridX][k2] += length;
						totalLength += length;
						if (logger.isDebugEnabled()) {
							logger.debug("p: " + p.toString());
							logger.debug("pointOnLine = " + pointOnLine.toString());
							logger.debug("density[" + pGridX + "][" + k2 + "]: ");
						}
						p = new Coordinate(pointOnLine);
						//
						yLine += k2 * granularityY;
					}
					double length = p.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (logger.isDebugEnabled()) {
						logger.debug("p: " + p.toString());
						logger.debug("q = " + q.toString());
						logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
					}

				} else if (numCrossGridY == 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("case 2");
					}
					// case 2: this road only intersect with different grids on x-axis
					for (int k1 = pGridX, ki = 0; ki < numCrossGridX; k1 += xDirect, ki++) {
						double y = (xLine - p.x) * slope + p.y;
						Coordinate pointOnLine = new Coordinate(xLine, y);
						double length = p.distance(pointOnLine);
						density[k1][pGridY] += length;
						totalLength += length;
						if (logger.isDebugEnabled()) {
							logger.debug("p: " + p.toString());
							logger.debug("pointOnLine = " + pointOnLine.toString());
							logger.debug("density[" + k1 + "][" + pGridY + "]: ");
						}
						p = new Coordinate(pointOnLine);

						xLine += k1 * granularityX;
					}
					double length = p.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (logger.isDebugEnabled()) {
						logger.debug("p: " + p.toString());
						logger.debug("q = " + q.toString());
						logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("case 3");
					}
					// case 3
					// the nearest point on Line Y and Line X
					double y = (xLine - p.x) * slope + p.y;
					Coordinate pointOnLineX = new Coordinate(xLine, y);
					double distancePointLineX = p.distance(pointOnLineX);

					double x = (yLine - p.y) / slope + p.x;
					Coordinate pointOnLineY = new Coordinate(x, yLine);
					double distancePointLineY = p.distance(pointOnLineY);

					boolean nextPointLineY = false;
					Coordinate pointOnLine1 = p;
					Coordinate pointOnLine2;
					if (distancePointLineY > distancePointLineX) {
						pointOnLine2 = pointOnLineX;
						nextPointLineY = true;
					} else {
						// less than or equals to
						pointOnLine2 = pointOnLineY;
					}

					for (int k1 = pGridX, k2 = pGridY, ki1 = 0, ki2 = 0; ki1 < numCrossGridX || ki2 < numCrossGridY;) {
						double length = pointOnLine1.distance(pointOnLine2);
						density[k1][k2] += length;
						totalLength += length;
						//
						if (logger.isDebugEnabled()) {
							logger.debug("pointOnLine1: " + pointOnLine1.toString());
							logger.debug("pointOnLine2 = " + pointOnLine2.toString());
							logger.debug("density[" + k1 + "][" + k2 + "]: ");
							logger.debug("nextPointLineY: " + nextPointLineY);
						}

						//
						pointOnLine1 = new Coordinate(pointOnLine2);

						if (nextPointLineY) {
							x = (yLine - p.y) / slope + p.x;
							pointOnLine2 = new Coordinate(x, yLine);
							yLine += k2 * granularityY;
							nextPointLineY = false;

							k2 += yDirect;
							ki2++;
						} else {
							y = (xLine - p.x) * slope + p.y;
							pointOnLine2 = new Coordinate(xLine, y);
							xLine += k1 * granularityX;
							nextPointLineY = true;

							k1 += xDirect;
							ki1++;
						}
					}
					double length = pointOnLine2.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (logger.isDebugEnabled()) {
						logger.debug("pointOnLine2: " + pointOnLine2.toString());
						logger.debug("q = " + q.toString());
						logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
					}
				}
				p = new Coordinate(q);
				pGridX = qGridX;
				pGridY = qGridY;
			}
		}
		// double areaSquare = granularityX * granularityY;
		for (int i = 0; i < countX; i++) {
			for (int j = 0; j < countY; j++) {
				density[i][j] = density[i][j] / totalLength;
			}
		}
		return density;
	}
}
