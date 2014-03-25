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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;

import mo.umac.crawler.Main;

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

	private static boolean log = true;

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
	 * The road information for the US states. </p> Downloaded from {@link http://www.census.gov/cgi-bin/geo/shapefiles2013/layers.cgi}
	 */
	private static String ROAD_SHP_FILE = "../data-map/tl_2013_40_prisecroads/tl_2013_40_prisecroads.shp";

	private static final String ZIP_EXTENSION = "zip";

	private static final String SHP_EXTENSION = "shp";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure(Main.LOG_PROPERTY_PATH);
		// for NY
		Envelope envelope = new Envelope(NYLongitudeMin, NYLongitudeMax, NYLatitudeMin, NYLatitudeMax);
		String zipFolderPath = "../data-map/us-road/new-york/";
		String densityFile = zipFolderPath + "densityMap.mbr";
		double granularityX = 0.08;
		double granularityY = 0.04;

		USDensity usDensity = new USDensity();

		/** compute the density on the map, run only once for a state folder */
//		String unZipfolderPath = "../data-map/us-road/new-york-upzip/";
//		ArrayList<Coordinate[]> roadList = usDensity.readRoad(zipFolderPath, unZipfolderPath);
//		double[][] density1 = usDensity.densityList(envelope, granularityX, granularityY, roadList);
//		usDensity.writeDensityToFile(density1, densityFile);
		/** End */

		/** cluster the regions, and then write to file */
		ArrayList<double[]> density = usDensity.readDensityFromFile(densityFile);
		DensityMap map = new DensityMap(granularityX, granularityY, envelope, density);
		String clusterRegionFile = zipFolderPath + "combinedDensity.txt";
		double alpha1 = 0.1;
		double alpha2 = 10;
		// only find one density region
		int numIteration = 1;
		ArrayList<Envelope> clusteredRegion = map.cluster(numIteration, alpha1, alpha2);
		usDensity.writePartition(clusterRegionFile, clusteredRegion);
	}

	/**
	 * Read all roads in the .shp file.
	 */
	private ArrayList<Coordinate[]> readRoad(String zipFolderPath, String unzipFolderPath) {
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

	private void writeDensityToFile(double[][] density, String densityFile) {
		logger.info("--------------writing unit density to file");
		File file = new File(densityFile);
		try {
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<double[]> readDensityFromFile(String densityFile) {
		ArrayList<double[]> density = new ArrayList<double[]>();
		File file = new File(densityFile);
		if (!file.exists()) {
			logger.error("densityFile does not exist");
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(densityFile)));
			String data = null;
			String[] splitArray;
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
	 * Compute the density in the map
	 * 
	 * @param envelope
	 * @param granularityX
	 *            : the granularity of the grid in x-axis
	 * @param granularityY
	 *            : the granularity of the grid in y-axis
	 * @param roadList
	 *            : coordinates of the start points and the end points of each line segment
	 * @return
	 */
	private double[][] densityList(Envelope envelope, double granularityX, double granularityY, ArrayList<Coordinate[]> roadList) {
		logger.info("-------------computing unit density-------------");
		double width = envelope.getWidth();
		double height = envelope.getHeight();
		double minX = envelope.getMinX();
		double minY = envelope.getMinY();

		int countX = (int) Math.ceil(width / granularityX);
		int countY = (int) Math.ceil(height / granularityY);
		// initialize to 0.0;
		double[][] density = new double[countX + 1][countY + 1];
		double totalLength = 0.0;
		//
		for (int i = 0; i < roadList.size(); i++) {
			Coordinate[] aPartRoad = roadList.get(i);
			Coordinate p = aPartRoad[0];
			int pGridX = (int) Math.ceil(Math.abs(p.x - minX) / granularityX);
			int pGridY = (int) Math.ceil(Math.abs(p.y - minY) / granularityY);

			for (int j = 1; j < aPartRoad.length; j++) {
				Coordinate q = aPartRoad[j];
				int qGridX = (int) Math.ceil(Math.abs(q.x - minX) / granularityX);
				int qGridY = (int) Math.ceil(Math.abs(q.y - minY) / granularityY);

				if (log) {
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
					density[pGridX][pGridY] += length;
					totalLength += length;
					if (log) {
						logger.debug("case 0: in the same grid");
						logger.debug("p.distance(q) : " + p.toString() + "," + q.toString() + " = " + length);
					}
					p = new Coordinate(q);
					pGridX = qGridX;
					pGridY = qGridY;
					continue;
				}
				// Now we are going to deal with the situation when the p and q point of this road are located on two different grids
				// slope of the line
				double slope = (q.y - p.y) / (q.x - p.x);
				if (log) {
					logger.debug("slope = " + slope);
				}
				// flag
				double xDirect = 1;
				double yDirect = 1;
				double xLine;
				double xLineLast;
				double yLine;
				double yLineLast;
				if (p.x > q.x) {
					xDirect = -1;
					xLine = minX + (pGridX - 1) * granularityX;
					xLineLast = minX + qGridX * granularityX;
				} else {
					xDirect = 1;
					xLine = minX + pGridX * granularityX;
					xLineLast = minX + (qGridX - 1) * granularityX;
				}
				if (p.y > q.y) {
					yDirect = -1;
					yLine = minY + (pGridY - 1) * granularityY;
					yLineLast = minY + qGridY * granularityY;
				} else {
					yDirect = 1;
					yLine = minY + pGridY * granularityY;
					yLineLast = minY + (qGridY - 1) * granularityY;
				}
				//
				int numCrossGridX = Math.abs(qGridX - pGridX);
				int numCrossGridY = Math.abs(qGridY - pGridY);

				if (log) {
					logger.debug("xDirect = " + xDirect);
					logger.debug("yDirect = " + yDirect);
					logger.debug("xLine = " + xLine);
					logger.debug("yLine = " + yLine);
					logger.debug("xLineLast = " + xLineLast);
					logger.debug("yLineLast = " + yLineLast);
					logger.debug("numCrossGridX = " + numCrossGridX);
					logger.debug("numCrossGridY = " + numCrossGridY);
				}

				if (numCrossGridX == 0) {
					if (log) {
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
						if (log) {
							logger.debug("p: " + p.toString());
							logger.debug("pointOnLine = " + pointOnLine.toString());
							logger.debug("density[" + pGridX + "][" + k2 + "]: ");
						}
						p = new Coordinate(pointOnLine);
						//
						yLine += yDirect * granularityY;
					}
					double length = p.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (log) {
						logger.debug("p: " + p.toString());
						logger.debug("q = " + q.toString());
						logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
					}

				} else if (numCrossGridY == 0) {
					if (log) {
						logger.debug("case 2");
					}
					// case 2: this road only intersect with different grids on x-axis
					for (int k1 = pGridX, ki = 0; ki < numCrossGridX; k1 += xDirect, ki++) {
						double y = (xLine - p.x) * slope + p.y;
						Coordinate pointOnLine = new Coordinate(xLine, y);
						double length = p.distance(pointOnLine);
						density[k1][pGridY] += length;
						totalLength += length;
						if (log) {
							logger.debug("p: " + p.toString());
							logger.debug("pointOnLine = " + pointOnLine.toString());
							logger.debug("density[" + k1 + "][" + pGridY + "]: ");
						}
						p = new Coordinate(pointOnLine);

						xLine += xDirect * granularityX;
					}
					double length = p.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (log) {
						logger.debug("p: " + p.toString());
						logger.debug("q = " + q.toString());
						logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
					}
				} else {
					if (log) {
						logger.debug("case 3");
					}
					// case 3
					// the start point
					// the nearest point on Line X
					double y = (xLine - p.x) * slope + p.y;
					Coordinate pointOnLineX = new Coordinate(xLine, y);
					double distancePointLineX = p.distance(pointOnLineX);
					// the nearest point on Line Y
					double x = (yLine - p.y) / slope + p.x;
					Coordinate pointOnLineY = new Coordinate(x, yLine);
					double distancePointLineY = p.distance(pointOnLineY);
					//
					boolean pointOnLine2IsOnLineY = false;
					Coordinate pointOnLine1 = new Coordinate(p);
					Coordinate pointOnLine2;
					if (distancePointLineY > distancePointLineX) {
						pointOnLine2IsOnLineY = false;
						pointOnLine2 = pointOnLineX;
						xLine += xDirect * granularityX;
					} else {
						// less than or equals to
						pointOnLine2IsOnLineY = true;
						pointOnLine2 = pointOnLineY;
						yLine += yDirect * granularityY;
					}
					// the end point
					double yEnd = (xLineLast - p.x) * slope + p.y;
					Coordinate lastPointOnLineX = new Coordinate(xLineLast, yEnd);
					double distanceLastPointLineX = q.distance(lastPointOnLineX);
					// the nearest point on Line Y
					double xEnd = (yLineLast - p.y) / slope + p.x;
					Coordinate lastPointOnLineY = new Coordinate(xEnd, yLineLast);
					double distanceLastPointLineY = q.distance(lastPointOnLineY);
					//
					Coordinate lastPointOnLine;
					if (distanceLastPointLineY > distanceLastPointLineX) {
						lastPointOnLine = new Coordinate(lastPointOnLineX);
					} else {
						lastPointOnLine = new Coordinate(lastPointOnLineY);
					}

					//
					int k1 = pGridX, k2 = pGridY;
					while (true) {
						double length = pointOnLine1.distance(pointOnLine2);
						density[k1][k2] += length;
						totalLength += length;
						//
						if (log) {
							logger.debug("pointOnLine1: " + pointOnLine1.toString());
							logger.debug("pointOnLine2 = " + pointOnLine2.toString());
							logger.debug("density[" + k1 + "][" + k2 + "]: ");
							logger.debug("nextPointLineY: " + pointOnLine2IsOnLineY);
						}
						if (pointOnLine2.equals(lastPointOnLine)) {
							if (log) {
								logger.debug("reach to the near end point.");
							}
							break;
						}
						pointOnLine1 = new Coordinate(pointOnLine2);
						// next point on line 2
						pointOnLine2IsOnLineY = !pointOnLine2IsOnLineY;
						if (pointOnLine2IsOnLineY) {
							x = (yLine - p.y) / slope + p.x;
							pointOnLine2 = new Coordinate(x, yLine);
							yLine += yDirect * granularityY;
							k1 += xDirect;
						} else {
							y = (xLine - p.x) * slope + p.y;
							pointOnLine2 = new Coordinate(xLine, y);
							xLine += xDirect * granularityX;
							k2 += yDirect;
						}

					}
					double length = pointOnLine2.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (log) {
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

	private void writePartition(String clusterRegionFile, ArrayList<Envelope> clusteredRegion) {
		try {
			 
			File file = new File(clusterRegionFile);
 
			if (!file.exists()) {
				file.createNewFile();
			}
 
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
			bw.write(Integer.toString(clusteredRegion.size()));
			bw.newLine();
			
			for (int i = 0; i < clusteredRegion.size(); i++) {
				Envelope envelope = clusteredRegion.get(i);
//				String s = envelope.getMinX() + ";" + envelope.getMinY() + ";" + envelope.getMaxX() + ";" + envelope.getMaxY();
				String s = envelope.getMinY() + ";" + envelope.getMinX() + ";" + envelope.getMaxY() + ";" + envelope.getMaxX();
				bw.write(s);
				bw.newLine();
			}
			
			bw.close();
 
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
