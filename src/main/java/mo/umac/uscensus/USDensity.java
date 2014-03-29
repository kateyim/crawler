/**
 * 
 */
package mo.umac.uscensus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;

import mo.umac.crawler.Main;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;

import utils.FileOperator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * split the map into a list of blocks with different densities.
 * 
 * @author kate
 */
public class USDensity {

	private static Logger logger = Logger.getLogger(USDensity.class.getName());

	// yanhui
	// NY: Env[-79.76259 : -71.777491, 40.477399 : 45.015865]
	private static final double NYLatitudeMin = 40.477399;
	private static final double NYLatitudeMax = 45.015865;
	private static final double NYLongitudeMin = -79.76259;
	private static final double NYLongitudeMax = -71.777491;

	// OK: // FIXME change these values
	private static final double OKLatitudeMin = 40.477399;
	private static final double OKLatitudeMax = 45.015865;
	private static final double OKLongitudeMin = -79.76259;
	private static final double OKLongitudeMax = -71.777491;

	// TODO state OT

	/**
	 * The name index in the .dbf file
	 */
	final static int NAME_INDEX = 5;

	/**
	 * The road information for the US states. </p> Downloaded from {@link http
	 * ://www.census.gov/cgi-bin/geo/shapefiles2013/layers.cgi}
	 */
	private static String ROAD_SHP_FILE = "../data-map/tl_2013_40_prisecroads/tl_2013_40_prisecroads.shp";

	private static final String ZIP_EXTENSION = "zip";

	private static final String SHP_EXTENSION = "shp";

	private static final String ZIP_FOLDER_PATH = "../data-map/us-road/new-york/";
	private static final String UN_ZIP_FOLDER_PATH = "../data-map/us-road/new-york-upzip/";

	private static String densityFile = "../data-experiment/densityMap-ny-0.001.txt";
	private static String clusterRegionFile = "../data-experiment/combinedDensity-ny.mbr";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean debug = true;
		shutdownLogs(debug);
		DOMConfigurator.configure(Main.LOG_PROPERTY_PATH);
		// for NY
		Envelope envelope = new Envelope(NYLongitudeMin, NYLongitudeMax, NYLatitudeMin, NYLatitudeMax);
		double granularityX = 0.001;
		double granularityY = 0.001;

		USDensity usDensity = new USDensity();

		/** compute the density on the map, run only once for a state folder */
		// // usDensity.unzip(ZIP_FOLDER_PATH, UN_ZIP_FOLDER_PATH);
		// ArrayList<Coordinate[]> roadList = usDensity.readRoad(UN_ZIP_FOLDER_PATH);
		// double[][] density1 = densityList(envelope, granularityX, granularityY, roadList);
		// usDensity.writeDensityToFile(density1, densityFile);
		/** End */

		/** cluster the regions, and then write to file */
		ArrayList<double[]> density = usDensity.readDensityFromFile(densityFile);
		// FIXME 2d array -> 1d array(only store non 0 values) -> TreeMap/HashMap 
		DensityMap map = new DensityMap(granularityX, granularityY, envelope, density);

		// double alpha1 = 0.1;
		// double alpha2 = 10;
		// // only find one density region
		// // yanhui
		// ArrayList<Envelope> testRegions = new ArrayList<Envelope>();
		// for (alpha1 = 0.1; alpha1 <= 1; alpha1 = alpha1 + 0.1) {
		// for (alpha2 = 0.1; alpha2 <= 1; alpha2 = alpha2 + 0.1) {
		// int numIteration = 1;
		// ArrayList<Envelope> clusteredRegion = map.cluster(numIteration,
		// alpha1, alpha2);
		// testRegions.addAll(clusteredRegion);
		// }
		// }
		// usDensity.writePartition(clusterRegionFile, testRegions);
	}

	private void unzip(String zipFolderPath, String unZipfolderPath) {
		logger.info("-----------unzip roads in " + zipFolderPath);
		ArrayList<Coordinate[]> roadList = new ArrayList<Coordinate[]>();
		//
		ArrayList<String> zipFileList = (ArrayList<String>) FileOperator
				.traverseFolder(zipFolderPath, ZIP_EXTENSION);
		for (int i = 0; i < zipFileList.size(); i++) {
			String aZipFile = zipFileList.get(i);
			// unzip it
			FileOperator.unzip(aZipFile, unZipfolderPath, "");
		}

	}

	public static void shutdownLogs(boolean debug) {
		if (!debug) {
			USDensity.logger.setLevel(Level.INFO);
			DensityMap.logger.setLevel(Level.INFO);
		} else {
			USDensity.logger.setLevel(Level.DEBUG);
			DensityMap.logger.setLevel(Level.DEBUG);
		}
	}

	/**
	 * Read all roads in the .shp file.
	 */
	private ArrayList<Coordinate[]> readRoad(String unzipFolderPath) {
		logger.info("-----------read roads in " + unzipFolderPath);
		// //
		// ArrayList<String> zipFileList = (ArrayList<String>) FileOperator
		// .traverseFolder(zipFolderPath, ZIP_EXTENSION);
		// for (int i = 0; i < zipFileList.size(); i++) {
		// String aZipFile = zipFileList.get(i);
		// // unzip it
		// FileOperator.unzip(aZipFile, unzipFolderPath, "");
		// }
		// /
		// traverse again
		ArrayList<Coordinate[]> roadList = new ArrayList<Coordinate[]>();
		ArrayList<String> shpFileList = (ArrayList<String>) FileOperator
				.traverseFolder(unzipFolderPath, SHP_EXTENSION);

		for (int i = 0; i < shpFileList.size(); i++) {
			String shpFile = shpFileList.get(i);
			try {
				ShpFiles shpFiles = new ShpFiles(shpFile);
				GeometryFactory gf = new GeometryFactory();
				ShapefileReader r = new ShapefileReader(shpFiles, true, true,
						gf);
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

	private void writeDensityToFile(double[][] density, String densityFile) {
		logger.info("--------------writing unit density to file");
		File file = new File(densityFile);
		try {
			if (file.exists()) {
				file.delete();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
			for (int i = 0; i < density.length; i++) {
				double[] ds = density[i];
				for (int j = 0; j < ds.length; j++) {
					double d = ds[j];
					bw.write(Double.toString(d));
					bw.write(";");
				}
				bw.newLine();
			}
			bw.close();
			logger.info("--------------finished writing");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<double[]> readDensityFromFile(String densityFile) {
		logger.info("readDensityFromFile...");
		ArrayList<double[]> density = new ArrayList<double[]>();
		File file = new File(densityFile);
		if (!file.exists()) {
			logger.error("densityFile does not exist");
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(densityFile)));
			String data = null;
			String[] splitArray;
			int i = 0;
			while ((data = br.readLine()) != null) {
				data = data.trim();
				splitArray = data.split(";");
				double[] densityRow = new double[splitArray.length];
				for (int j = 0; j < splitArray.length; j++) {
					String split = splitArray[j];
					double value = Double.parseDouble(split);
					densityRow[j] = value;
				}
				density.add(densityRow);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return density;
	}

	/**
	 * Compute the density in the map rewrite 2014-3-29
	 * 
	 * @param envelope
	 * @param granularityX
	 *            : the granularity of the grid in x-axis
	 * @param granularityY
	 *            : the granularity of the grid in y-axis
	 * @param roadList
	 *            : coordinates of the start points and the end points of each
	 *            line segment
	 * @return
	 */
	public static double[][] densityList(Envelope envelope, double granularityX,
			double granularityY, ArrayList<Coordinate[]> roadList) {
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
		double totalLength = 0.0;
		//
		for (int i = 0; i < roadList.size(); i++) {
			Coordinate[] aPartRoad = roadList.get(i);
			Coordinate p = aPartRoad[0];
			//
			int pGridX = (int) Math.floor((Math.abs(p.x - minX) / granularityX));
			int pGridY = (int) Math.floor((Math.abs(p.y - minY) / granularityY));
			//
			for (int j = 1; j < aPartRoad.length; j++) {
				Coordinate q = aPartRoad[j];
				int qGridX = (int) Math.floor(Math.abs(q.x - minX) / granularityX);
				int qGridY = (int) Math.floor(Math.abs(q.y - minY) / granularityY);

				if (logger.isDebugEnabled()) {
					logger.debug("----------a part of road-----------");
					logger.debug("p: " + p.toString());
					logger.debug("q: " + q.toString());
					logger.debug("p: [" + pGridX + "][" + pGridY + "]: ");
					logger.debug("q: [" + qGridX + "][" + qGridY + "]: ");
				}

				// This is the easiest case
				if (pGridX == qGridX && pGridY == qGridY) {
					// the p and the q point belong to the same small square
					double length = p.distance(q);
					totalLength += length;
					density[pGridX][pGridY] += length;
					if (logger.isDebugEnabled()) {
						logger.debug("case 0: in the same grid");
						logger.debug("p.distance(q) : " + p.toString() + "," + q.toString() + " = " + length);
					}
					p = new Coordinate(q);
					pGridX = qGridX;
					pGridY = qGridY;
					continue;
				}
				//
				double xDirect = 1;
				double yDirect = 1;
				double xLinePNeighbor;
				double xLineQNeighbor;
				double yLinePNeighbor;
				double yLineQNeighbor;
				if (p.x > q.x) {
					xDirect = -1;
					xLinePNeighbor = minX + pGridX * granularityX;
					xLineQNeighbor = minX + (qGridX + 1) * granularityX;
				} else {
					xDirect = 1;
					xLinePNeighbor = minX + (pGridX + 1) * granularityX;
					xLineQNeighbor = minX + qGridX * granularityX;
				}
				if (p.y >= q.y) {
					yDirect = -1;
					yLinePNeighbor = minY + pGridY * granularityY;
					yLineQNeighbor = minY + (qGridY + 1) * granularityY;
				} else {
					yDirect = 1;
					yLinePNeighbor = minY + (pGridY + 1) * granularityY;
					yLineQNeighbor = minY + qGridY * granularityY;
				}
				int numCrossGridX = Math.abs(qGridX - pGridX);
				int numCrossGridY = Math.abs(qGridY - pGridY);

				if (logger.isDebugEnabled()) {
					logger.debug("xDirect = " + xDirect);
					logger.debug("yDirect = " + yDirect);
					logger.debug("xLinePNeighbor = " + xLinePNeighbor);
					logger.debug("yLinePNeighbor = " + yLinePNeighbor);
					logger.debug("xLineQNeighbor = " + xLineQNeighbor);
					logger.debug("yLineQNeighbor = " + yLineQNeighbor);
					logger.debug("numCrossGridX = " + numCrossGridX);
					logger.debug("numCrossGridY = " + numCrossGridY);
				}
				//
				if (q.x == p.x) {
					for (int k2 = pGridY, ki = 0; ki < numCrossGridY; k2 += yDirect, ki++) {
						Coordinate pointOnLine = new Coordinate(q.x, yLinePNeighbor);
						double length = p.distance(pointOnLine);
						density[pGridX][k2] += length;
						totalLength += length;
						if (logger.isDebugEnabled()) {
							logger.debug("p: " + p.toString());
							logger.debug("pointOnLine = " + pointOnLine.toString());
							logger.debug("density[" + pGridX + "][" + k2 + "]: ");
						}
						p = new Coordinate(pointOnLine);
						yLinePNeighbor += yDirect * granularityY;
					}
					double length = p.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (logger.isDebugEnabled()) {
						logger.debug("p = " + p.toString());
						logger.debug("q = " + q.toString());
						logger.debug("density[" + qGridX + "][" + qGridY + "]");
					}
					continue;
				}
				// y = k*x + b
				double k = (q.y - p.y) / (q.x - p.x);
				double b = (q.x * p.y - q.y * p.x) / (q.x - p.x);
				if (logger.isDebugEnabled()) {
					logger.debug("k = " + k);
					logger.debug("b = " + b);
				}
				//
				Coordinate p1 = new Coordinate(p);
				Coordinate p2 = null;
				boolean p2OnLineY = false;
				for (int k1 = pGridX, k2 = pGridY, ki1 = 0, ki2 = 0; ki1 < numCrossGridX || ki2 < numCrossGridY;) {
					if (logger.isDebugEnabled()) {
						logger.debug("ki1 = " + ki1);
						logger.debug("ki2 = " + ki2);
					}
					// compute which line will be reached first
					double y = xLinePNeighbor * k + b;
					Coordinate pointOnLineX = new Coordinate(xLinePNeighbor, y);
					double distancePointLineX = p.distance(pointOnLineX);
					// the nearest point on Line Y
					double x = (yLinePNeighbor - b) / k;
					Coordinate pointOnLineY = new Coordinate(x, yLinePNeighbor);
					double distancePointLineY = p.distance(pointOnLineY);
					if (distancePointLineY > distancePointLineX) {
						p2OnLineY = false;
						p2 = pointOnLineX;
						// xLinePNeighbor += xDirect * granularityX;
					} else {
						// less than or equals to
						p2OnLineY = true;
						p2 = pointOnLineY;
						// yLinePNeighbor += yDirect * granularityY;
					}
					double length = p1.distance(p2);
					density[k1][k2] += length;
					totalLength += length;
					if (logger.isDebugEnabled()) {
						logger.debug("p1: " + p1.toString());
						logger.debug("p2 = " + p2.toString());
						logger.debug("density[" + k1 + "][" + k2 + "]: ");
						logger.debug("p2OnLineY: " + p2OnLineY);
					}
					p1 = new Coordinate(p2);
					if (p2OnLineY) {
						yLinePNeighbor += yDirect * granularityY;
						k2 += yDirect;
						ki2++;
					} else {
						xLinePNeighbor += xDirect * granularityX;
						k1 += xDirect;
						ki1++;
					}

				}
				double length = p2.distance(q);
				density[qGridX][qGridY] += length;
				totalLength += length;

				if (logger.isDebugEnabled()) {
					logger.debug("pointOnLine2: " + p2.toString());
					logger.debug("q = " + q.toString());
					logger.debug("density[" + qGridX + "][" + qGridY + "]");
				}

				p = new Coordinate(q);
				pGridX = qGridX;
				pGridY = qGridY;
			}
		}
		// double areaSquare = granularityX * granularityY;
		// for (int i = 0; i < countX; i++) {
		// for (int j = 0; j < countY; j++) {
		// density[i][j] = density[i][j] / totalLength;
		//
		// }
		// }
		return density;
	}

	private void writePartition(String clusterRegionFile,
			ArrayList<Envelope> clusteredRegion) {
		try {

			File file = new File(clusterRegionFile);

			if (file.exists()) {
				file.delete();
			}
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			bw.write(Integer.toString(clusteredRegion.size()));
			bw.newLine();

			for (int i = 0; i < clusteredRegion.size(); i++) {
				Envelope envelope = clusteredRegion.get(i);
				String s = envelope.getMinY() + ";" + envelope.getMinX() + ";"
						+ envelope.getMaxY() + ";" + envelope.getMaxX();
				bw.write(s);
				bw.newLine();
			}

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
