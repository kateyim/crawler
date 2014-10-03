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
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.openqa.selenium.net.EphemeralPortRangeDetector;

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

	public static Logger logger = Logger.getLogger(USDensity.class.getName());

	private static double epsilon = 0.000001;

	// NY: Env[-79.76259 : -71.777491, 40.477399 : 45.015865]
	private static final double NYLatitudeMin = 40.477399;
	private static final double NYLatitudeMax = 45.015865;
	private static final double NYLongitudeMin = -79.76259;
	private static final double NYLongitudeMax = -71.777491;

	/**
	 * The name index in the .dbf file
	 */
	final static int NAME_INDEX = 5;

	/**
	 * The road information for the US states.
	 * </p>
	 * Downloaded from {@link http://www.census.gov/cgi-bin/geo/shapefiles2013/layers.cgi}
	 */
	private static String ROAD_SHP_FILE = "../data-map/tl_2013_40_prisecroads/tl_2013_40_prisecroads.shp";

	private static final String ZIP_EXTENSION = "zip";

	private static final String SHP_EXTENSION = "shp";

	// for NY
	public static Envelope envelopeNY = new Envelope(NYLongitudeMin, NYLongitudeMax, NYLatitudeMin, NYLatitudeMax);
	private static double granularityX = 0.01;
	private static double granularityY = 0.01;

	private static boolean zipped = false;
	private static final String ZIP_FOLDER_PATH = "../data-map/us-road/new-york/";
	public static final String UN_ZIP_FOLDER_PATH = "../data-map/us-road/new-york-upzip/";

	private static String densityFile = "../data-experiment/partition/densityMap-ny-0.01";
	ArrayList<double[]> density;
	/************** NY *****************/
	private static String clusterRegionFilePre = "../data-experiment/partition/combinedDensity-ny-";
	private static String dentiestRegionFile = "../data-experiment/partition/combinedDensity-ny-0.mbr";
	public static String clusterRegionFile = "../data-experiment/partition/combinedDensity-ny-testing.mbr";

	/****************************** Computing the Density ************************************************/

	/**
	 * 
	 * Compute the density for NY, run only once for a state folder
	 * 
	 * 
	 */
	public static double[][] computeDensityInEachGrids() {
		if (zipped) {
			USDensity.unzip(ZIP_FOLDER_PATH, UN_ZIP_FOLDER_PATH);
		}
		ArrayList<Coordinate[]> roadList = USDensity.readRoad(UN_ZIP_FOLDER_PATH);
		double[][] density1 = densityList(envelopeNY, granularityX, granularityY, roadList);
		USDensity.writeDensityToFile(density1, densityFile);
		return density1;
	}

	/**
	 * Almost the same with computeDensityInEachGrids()
	 * 
	 * @param unZipFolderPath
	 * @param envelope
	 * @param granularityX
	 * @param granularityY
	 * @return
	 */
	public static double[][] computeDensityInEachGrids(String unZipFolderPath, Envelope envelope, double granularityX, double granularityY) {
		ArrayList<Coordinate[]> roadList = USDensity.readRoad(unZipFolderPath);
		double[][] density = densityList(envelope, granularityX, granularityY, roadList);
		return density;
	}

	public static void unzip(String zipFolderPath, String unZipfolderPath) {
		logger.info("-----------unzip roads in " + zipFolderPath);
		ArrayList<Coordinate[]> roadList = new ArrayList<Coordinate[]>();
		//
		ArrayList<String> zipFileList = (ArrayList<String>) FileOperator.traverseFolder(zipFolderPath, ZIP_EXTENSION);
		for (int i = 0; i < zipFileList.size(); i++) {
			String aZipFile = zipFileList.get(i);
			// unzip it
			FileOperator.unzip(aZipFile, unZipfolderPath, "");
		}

	}

	/**
	 * Read all roads in the .shp file.
	 */
	public static ArrayList<Coordinate[]> readRoad(String unzipFolderPath) {
//		logger.info("-----------read roads in " + unzipFolderPath);
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
	public static double[][] densityList(Envelope envelope, double granularityX, double granularityY, ArrayList<Coordinate[]> roadList) {
//		logger.info("-------------computing unit density-------------");
		double width = envelope.getWidth();
		double height = envelope.getHeight();
		double minX = envelope.getMinX();
		double minY = envelope.getMinY();

		// the number of grids, begin from 0;
		int countX = (int) Math.ceil(width / granularityX);
		int countY = (int) Math.ceil(height / granularityY);
//		logger.info("countX = " + countX);
//		logger.info("countY = " + countY);
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
		return density;
	}

	/**************************** End of computing the density *********************************************/

	/**************************** Begin Clusterings *********************************************/

	/**
	 * The object is to get the uniformed partitioned grids
	 * 
	 * Begin from the densest region, then partition, iteration
	 * 
	 * 
	 */
	public static ArrayList<Envelope> partitionBasedOnDense(int numDense, double a, double[][] densityAll, Envelope envelope, double granularityX,
			double granularityY) {
		// envelopeList: the real longitude & latitude (dividing by the gridsX and Y), not the number of grids
		Queue<Envelope> queue = new LinkedList<Envelope>();
		queue.add(envelopeNY);
		ArrayList<Envelope> results = new ArrayList<Envelope>();
		// only find the top densest in a region
		int findDense = 0;
		while (!queue.isEmpty() && findDense <= numDense) {
			Envelope partEnvelope = queue.poll();
			ArrayList<double[]> density = readPartOfDensity(densityAll, envelope, partEnvelope, granularityX, granularityY);
			boolean allZero = allZero(density);
			if (allZero) {
				results.add(partEnvelope);
				continue;
			}
			// denseEnvelope: long&lat
			Envelope denseEnvelope = Cluster.cluster(granularityX, granularityY, partEnvelope, density, a);
			results.add(denseEnvelope);
			findDense++;
			ArrayList<Envelope> partitionedRegions = Cluster.partition(partEnvelope, denseEnvelope);

			queue.addAll(partitionedRegions);
		}
		results.addAll(queue);
		return results;
	}

	/**
	 * Read parts of the densities from the density of the whole map
	 * 
	 * partEnvelope are real longitude and latitude
	 * 
	 * 
	 * @param densityAll
	 * @param partEnvelope
	 * @return
	 */
	public static ArrayList<double[]> readPartOfDensity(ArrayList<double[]> densityAll, Envelope wholeEnvelope, Envelope partEnvelope) {
		ArrayList<double[]> densityPart = new ArrayList<double[]>();
		int xBegin = (int) ((partEnvelope.getMinX() - wholeEnvelope.getMinX()) / granularityX);
		int xEnd = (int) Math.ceil((partEnvelope.getMaxX() - wholeEnvelope.getMinX()) / granularityX) - 1;
		int yBegin = (int) ((partEnvelope.getMinY() - wholeEnvelope.getMinY()) / granularityX);
		int yEnd = (int) Math.ceil((partEnvelope.getMaxY() - wholeEnvelope.getMinY()) / granularityX) - 1;
		int length = (int) (yEnd - yBegin) + 1;
		// logger.info("yBegin = " + yBegin);
		// logger.info("yEnd = " + yEnd);
		// logger.info("aRow.length = " + densityAll.get(0).length);
		for (int i = xBegin; i <= xEnd; i++) {
			double[] aRow = densityAll.get(i);
			double[] newARow = new double[length];
			for (int j = yBegin; j <= yEnd; j++) {
				newARow[j - yBegin] = aRow[j];
			}
			densityPart.add(newARow);
		}
		return densityPart;
	}

	/**
	 * Read parts of the densities from the density of the whole map
	 * 
	 * partEnvelope are real longitude and latitude
	 * 
	 * 
	 * @param densityAll
	 * @param partEnvelope
	 * @return
	 */
	public static ArrayList<double[]> readPartOfDensity(double[][] densityAll, Envelope wholeEnvelope, Envelope partEnvelope, double granularityX, double granularityY) {
		ArrayList<double[]> densityPart = new ArrayList<double[]>();
		int xBegin = (int) ((partEnvelope.getMinX() - wholeEnvelope.getMinX()) / granularityX);
		int xEnd = (int) Math.ceil((partEnvelope.getMaxX() - wholeEnvelope.getMinX()) / granularityX) - 1;
		int yBegin = (int) ((partEnvelope.getMinY() - wholeEnvelope.getMinY()) / granularityX);
		int yEnd = (int) Math.ceil((partEnvelope.getMaxY() - wholeEnvelope.getMinY()) / granularityX) - 1;
		int length = (int) (yEnd - yBegin) + 1;
//		logger.info("yBegin = " + yBegin);
//		logger.info("yEnd = " + yEnd);
//		logger.info("aRow.length = " + densityAll[0].length);
		for (int i = xBegin; i <= xEnd; i++) {
			double[] aRow = densityAll[i];
			double[] newARow = new double[length];
			for (int j = yBegin; j <= yEnd; j++) {
				newARow[j - yBegin] = aRow[j];
			}
			densityPart.add(newARow);
		}
		return densityPart;
	}

	/**
	 * cluster the regions, and then write to file
	 * 
	 * @param numDense
	 * @return
	 */
	public static ArrayList<Envelope> findDensestRegions(Envelope envelope, ArrayList<double[]> density, int numDense) {
		double a = 0.8;
		// String writingFileName = clusterRegionFilePre + a + "-" + numDense + ".mbr";
		ArrayList<Envelope> clusteredRegion = null;
		clusteredRegion = Cluster.clusterDensest(granularityX, granularityY, envelope, density, a, numDense);
		// USDensity.writePartition(writingFileName, clusteredRegion);
		return clusteredRegion;
	}

	/**
	 * If the density of all grids are 0
	 * 
	 * @param density
	 * @return
	 */
	private static boolean allZero(ArrayList<double[]> density) {
		int length;
		for (int i = 0; i < density.size(); i++) {
			length = density.get(0).length;
			for (int j = 0; j < length; j++) {
				if (Math.abs(density.get(i)[j]) > epsilon) {
					return false;
				}
			}
		}
		return true;
	}

	/**************************** End of CLusterings ********************************************/

	/**************************** File Operations ********************************************/
	public static void writeDensityToFile(double[][] density, String densityFile) {
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
					if (logger.isDebugEnabled()) {
						if (d != 0) {
							logger.debug("writing: " + i + ", " + j + " = " + d);
						}
					}
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

	/**
	 * The index begin from 0
	 * 
	 * @param densityFile
	 * @return
	 */
	public static ArrayList<double[]> readDensityFromFile(String densityFile) {
		logger.info("readDensityFromFile...");
		ArrayList<double[]> density = new ArrayList<double[]>();
		// File file = new File(densityFile);
		// if (!file.exists()) {
		// logger.error("densityFile does not exist");
		// }
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(densityFile)));
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
					if (logger.isDebugEnabled()) {
						if (value != 0) {
							logger.debug("reading: " + i + ", " + j + " = " + value);
						}
					}
				}
				i++;
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

	public static void writePartition(String clusterRegionFile, ArrayList<Envelope> clusteredRegion) {
		try {

			File file = new File(clusterRegionFile);

			if (file.exists()) {
				file.delete();
			}
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
			bw.write(Integer.toString(clusteredRegion.size()));
			bw.newLine();

			for (int i = 0; i < clusteredRegion.size(); i++) {
				Envelope envelope = clusteredRegion.get(i);
				String s = envelope.getMinY() + ";" + envelope.getMinX() + ";" + envelope.getMaxY() + ";" + envelope.getMaxX();
				bw.write(s);
				bw.newLine();
			}

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Envelope> readPartition(String clusterRegionFile) {
		ArrayList<Envelope> clusteredRegion = new ArrayList<Envelope>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(clusterRegionFile)));
			String data = null;
			String[] splitArray;
			int i = 0;
			double x1, x2, y1, y2;
			// String s = envelope.getMinY() + ";" + envelope.getMinX() + ";" +
			// envelope.getMaxY() + ";" + envelope.getMaxX();
			while ((data = br.readLine()) != null) {
				data = data.trim();
				if (logger.isDebugEnabled()) {
					logger.debug(data);
				}
				if (!data.contains(";")) {
					continue;
				}
				splitArray = data.split(";");
				// x1 = Double.parseDouble(splitArray[0]);
				// y1 = Double.parseDouble(splitArray[1]);
				// x2 = Double.parseDouble(splitArray[2]);
				// y2 = Double.parseDouble(splitArray[3]);
				y1 = Double.parseDouble(splitArray[0]);
				x1 = Double.parseDouble(splitArray[1]);
				y2 = Double.parseDouble(splitArray[2]);
				x2 = Double.parseDouble(splitArray[3]);

				Envelope e = new Envelope(x1, x2, y1, y2);
				clusteredRegion.add(e);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return clusteredRegion;
	}

	/**************************** End File Operations ********************************************/

	/****************************** Testing ************************************************/
	/**
	 * The partition method I used before
	 */
	public void forYahooNYBefore() {
		ArrayList<double[]> densityAll = USDensity.readDensityFromFile(densityFile);
		ArrayList<Envelope> envelopeList = addEnvelopeList();
		ArrayList<Envelope> results = new ArrayList<Envelope>();
		int numDense = 1;
		double a = 0.8;
		for (int i = 0; i < envelopeList.size(); i++) {
			Envelope e = envelopeList.get(i);
			ArrayList<double[]> density = readPartOfDensity(densityAll, envelopeNY, e);
			ArrayList<Envelope> denseEnvelopList = Cluster.clusterDensest(granularityX, granularityY, e, density, a, numDense);
			ArrayList<Envelope> partitionedRegions = Cluster.partition(e, denseEnvelopList);
			results.addAll(partitionedRegions);
		}
		// before writing, has changed to latitude & longitude
		USDensity.writePartition(clusterRegionFile, results);
	}

	/**
	 * The object is to get the uniformed partitioned grids
	 * 
	 * Begin from the densest region, then partition, iteration for twice
	 * 
	 * The results is bad. Still need to partition the empty first, see forYahooNYEmptyAndDenses
	 * 
	 */
	public void forYahooNYOnlyDenses() {
		ArrayList<double[]> densityAll = USDensity.readDensityFromFile(densityFile);
		// envelopeList: the real longitude & latitude (dividing by the gridsX and Y), not the number of grids
		Queue<Envelope> queue = new LinkedList<Envelope>();
		queue.add(envelopeNY);
		ArrayList<Envelope> results = new ArrayList<Envelope>();
		// only find the top densest in a region
		double a = 0.8;
		// the number of iteration
		int iteration = 2;
		int totalNum = 1;
		for (int i = 0; i < iteration; i++) {
			totalNum *= 4;
		}
		while (!queue.isEmpty() && queue.size() < totalNum) {
			logger.debug("totalNum = " + totalNum);
			Envelope partEnvelope = queue.poll();
			ArrayList<double[]> density = readPartOfDensity(densityAll, envelopeNY, partEnvelope);
			// denseEnvelope: long&lat
			Envelope denseEnvelope = Cluster.cluster(granularityX, granularityY, partEnvelope, density, a);
			results.add(denseEnvelope);
			ArrayList<Envelope> partitionedRegions = Cluster.partition(partEnvelope, denseEnvelope);

			queue.addAll(partitionedRegions);
		}
		results.addAll(queue);
		// before writing, has changed to latitude & longitude
		USDensity.writePartition(clusterRegionFile, results);
	}

	/**
	 * First get rid the empty regions, then cluster from the densest region
	 * 
	 * By hand
	 * 
	 */
	public void forYahooNYEmptyAndDenses() {
		ArrayList<double[]> densityAll = USDensity.readDensityFromFile(densityFile);
		// envelopeList: the real longitude & latitude (dividing by the gridsX and Y), not the number of grids
		ArrayList<Envelope> results = new ArrayList<Envelope>();
		// only find the top densest in a region
		double a = 0.8;
		// 1. from 0
		// ArrayList<double[]> density = readPartOfDensity(densityAll, envelope, partEnvelope);
		// ArrayList<Envelope> zeroEnvelopeList = Cluster.clusterZero(granularityX, granularityY, partEnvelope, density, iteration, 1);
		// partition these 0 from envelope
		// ArrayList<Envelope> zeroFilteredEnvelopeList = addEnvelopeListForNY();
		// ArrayList<Envelope> partitionedRegionsByZero = Cluster.partition(partEnvelope, zeroFilteredEnvelopeList);

		// 2-1. these from 0
		// 2-1. these from dense
		// denseEnvelope: long&lat
		// Envelope denseEnvelope = Cluster.cluster(granularityX, granularityY, partEnvelope, density, a);
		// ArrayList<Envelope> partitionedRegions = Cluster.partition(partEnvelope, denseEnvelope);

		// results.addAll(partitionedRegionsByZero);
		ArrayList<Envelope> envelopeList = addEnvelopeListForNY();
		// for 0
		// Envelope partEnvelope = envelope;
		Envelope partEnvelope = envelopeList.get(0);
		logger.info("partEnvelope = " + partEnvelope);
		ArrayList<double[]> density = readPartOfDensity(densityAll, envelopeNY, partEnvelope);
		ArrayList<Envelope> zeroEnvelopeList = Cluster.clusterZero(granularityX, granularityY, partEnvelope, density, 1, 1);
		results.addAll(zeroEnvelopeList);
		// for dense
		// for (int i = 0; i < envelopeList.size(); i++) {
		// Envelope e = envelopeList.get(i);
		// ArrayList<double[]> density = readPartOfDensity(densityAll, envelope, e);
		// Envelope denseEnvelope = Cluster.cluster(granularityX, granularityY, e, density, a);
		// ArrayList<Envelope> partitionedRegions = Cluster.partition(e, denseEnvelope);
		// results.add(denseEnvelope);
		// results.addAll(partitionedRegions);
		// }

		// before writing, has changed to latitude & longitude
		USDensity.writePartition(clusterRegionFile, results);
	}

	public static ArrayList<Envelope> addEnvelopeListForNY() {
		// 41.997399;-75.35259;40.477399;-73.24259
		Envelope e4 = new Envelope(-75.35259, -73.24259, 40.477399, 41.997399);
		ArrayList<Envelope> envelopeList = new ArrayList<Envelope>();
		envelopeList.add(e4);
		return envelopeList;
	}

	/**
	 * Add previous divided results.
	 * 
	 * 40.477399;-79.76259;41.997399;-75.35259
	 * 43.377399;-79.76259;45.017399;-76.66259000000001
	 * 41.297399;-73.24259;45.017399;-71.77259000000001
	 * 41.997399;-79.76259;43.377399;-76.66259000000001
	 * 41.997399;-76.66259000000001;45.017399;-73.24259
	 * 40.477399;-75.35259;41.997399;-73.24259
	 * 40.477399;-73.24259;41.297399;-71.77259000000001
	 */
	public static ArrayList<Envelope> addEnvelopeList() {
		// Envelope e1 = new Envelope(40.477399, -79.76259, 41.997399, -75.35259);
		// Envelope e2 = new Envelope(43.377399, -79.76259, 45.017399, -76.66259000000001);
		// Envelope e3 = new Envelope(41.297399, -73.24259, 45.017399, -71.77259000000001);
		Envelope e4 = new Envelope(-79.76259, -76.66259000000001, 41.997399, 43.377399);
		Envelope e5 = new Envelope(-76.66259000000001, -73.24259, 41.997399, 45.017399);
		Envelope e6 = new Envelope(-75.35259, -73.24259, 40.477399, 41.997399);
		Envelope e7 = new Envelope(-73.24259, -71.77259000000001, 40.477399, 41.297399);
		ArrayList<Envelope> envelopeList = new ArrayList<Envelope>();
		// These 3 regions contains only 0
		// envelopeList.add(e1);
		// envelopeList.add(e2);
		// envelopeList.add(e3);
		envelopeList.add(e4);
		envelopeList.add(e5);
		envelopeList.add(e6);
		envelopeList.add(e7);
		return envelopeList;
	}
	/****************************** End of Testing ************************************************/

}
