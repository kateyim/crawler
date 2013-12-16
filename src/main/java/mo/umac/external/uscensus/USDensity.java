/**
 * 
 */
package mo.umac.external.uscensus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * split the map into a list of blocks with different densities.
 * 
 * @author kate
 * 
 */
public class USDensity {

	protected static Logger logger = Logger.getLogger(USDensity.class.getName());

	/**
	 * 
	 */
	public static void ClusterDensityMap(String densityFile, String combinedFile) {
		// TODO combineDensityMap

	}

	public static void writeDensityToFile(double[][] density, String densityFile) {
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

	public static ArrayList<double[]> readDensityFromFile(String densityFile) {
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
	 * @param granularityY
	 * @param roadList
	 * @return
	 */
	public static double[][] densityList(Envelope envelope, double granularityX, double granularityY, ArrayList<Coordinate[]> roadList) {
		logger.info("-------------computing unit density");
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
				Coordinate q = aPartRoad[j];
				int qGridX = (int) Math.ceil(Math.abs(q.x - minX) / granularityX);
				int qGridY = (int) Math.ceil(Math.abs(q.y - minY) / granularityY);

				if (UScensusData.logger.isDebugEnabled()) {
					UScensusData.logger.debug("----------a part of road-----------");
					UScensusData.logger.debug("p: " + p.toString());
					UScensusData.logger.debug("q: " + q.toString());
					UScensusData.logger.debug("p: [" + pGridX + "][" + pGridY + "]: ");
					UScensusData.logger.debug("q: [" + qGridX + "][" + qGridY + "]: ");
				}

				// This is the easiest case
				if (pGridX == qGridX && pGridY == qGridY) {
					// the p and the q point belong to the same small square
					double length = p.distance(q);
					density[pGridX][pGridY] += length;
					totalLength += length;
					if (UScensusData.logger.isDebugEnabled()) {
						UScensusData.logger.debug("case 0: in the same grid");
						UScensusData.logger.debug("p.distance(q) : " + p.toString() + "," + q.toString() + " = " + length);
					}
					p = new Coordinate(q);
					pGridX = qGridX;
					pGridY = qGridY;
					continue;
				}
				// Now we are going to deal with the situation when the p and q point of this road are located on two different grids
				// slope of the line
				double slope = (q.y - p.y) / (q.x - p.x);
				if (UScensusData.logger.isDebugEnabled()) {
					UScensusData.logger.debug("slope = " + slope);
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

				if (UScensusData.logger.isDebugEnabled()) {
					UScensusData.logger.debug("xDirect = " + xDirect);
					UScensusData.logger.debug("yDirect = " + yDirect);
					UScensusData.logger.debug("xLine = " + xLine);
					UScensusData.logger.debug("yLine = " + yLine);
					UScensusData.logger.debug("xLineLast = " + xLineLast);
					UScensusData.logger.debug("yLineLast = " + yLineLast);
					UScensusData.logger.debug("numCrossGridX = " + numCrossGridX);
					UScensusData.logger.debug("numCrossGridY = " + numCrossGridY);
				}

				if (numCrossGridX == 0) {
					if (UScensusData.logger.isDebugEnabled()) {
						UScensusData.logger.debug("case 1");
					}
					// case 1: this road only intersect with different grids on y-axis
					for (int k2 = pGridY, ki = 0; ki < numCrossGridY; k2 += yDirect, ki++) {
						// compute the intersect points
						double x = (yLine - p.y) / slope + p.x;
						Coordinate pointOnLine = new Coordinate(x, yLine);
						double length = p.distance(pointOnLine);
						density[pGridX][k2] += length;
						totalLength += length;
						if (UScensusData.logger.isDebugEnabled()) {
							UScensusData.logger.debug("p: " + p.toString());
							UScensusData.logger.debug("pointOnLine = " + pointOnLine.toString());
							UScensusData.logger.debug("density[" + pGridX + "][" + k2 + "]: ");
						}
						p = new Coordinate(pointOnLine);
						//
						yLine += yDirect * granularityY;
					}
					double length = p.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (UScensusData.logger.isDebugEnabled()) {
						UScensusData.logger.debug("p: " + p.toString());
						UScensusData.logger.debug("q = " + q.toString());
						UScensusData.logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
					}

				} else if (numCrossGridY == 0) {
					if (UScensusData.logger.isDebugEnabled()) {
						UScensusData.logger.debug("case 2");
					}
					// case 2: this road only intersect with different grids on x-axis
					for (int k1 = pGridX, ki = 0; ki < numCrossGridX; k1 += xDirect, ki++) {
						double y = (xLine - p.x) * slope + p.y;
						Coordinate pointOnLine = new Coordinate(xLine, y);
						double length = p.distance(pointOnLine);
						density[k1][pGridY] += length;
						totalLength += length;
						if (UScensusData.logger.isDebugEnabled()) {
							UScensusData.logger.debug("p: " + p.toString());
							UScensusData.logger.debug("pointOnLine = " + pointOnLine.toString());
							UScensusData.logger.debug("density[" + k1 + "][" + pGridY + "]: ");
						}
						p = new Coordinate(pointOnLine);

						xLine += xDirect * granularityX;
					}
					double length = p.distance(q);
					density[qGridX][qGridY] += length;
					totalLength += length;

					if (UScensusData.logger.isDebugEnabled()) {
						UScensusData.logger.debug("p: " + p.toString());
						UScensusData.logger.debug("q = " + q.toString());
						UScensusData.logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
					}
				} else {
					if (UScensusData.logger.isDebugEnabled()) {
						UScensusData.logger.debug("case 3");
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
						if (UScensusData.logger.isDebugEnabled()) {
							UScensusData.logger.debug("pointOnLine1: " + pointOnLine1.toString());
							UScensusData.logger.debug("pointOnLine2 = " + pointOnLine2.toString());
							UScensusData.logger.debug("density[" + k1 + "][" + k2 + "]: ");
							UScensusData.logger.debug("nextPointLineY: " + pointOnLine2IsOnLineY);
						}
						if (pointOnLine2.equals(lastPointOnLine)) {
							if (UScensusData.logger.isDebugEnabled()) {
								UScensusData.logger.debug("reach to the near end point.");
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

					if (UScensusData.logger.isDebugEnabled()) {
						UScensusData.logger.debug("pointOnLine2: " + pointOnLine2.toString());
						UScensusData.logger.debug("q = " + q.toString());
						UScensusData.logger.debug("density[" + qGridX + "][" + qGridY + "]: ");
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

	/**
	 * @param density
	 * @param granularityX
	 * @param granularityY
	 * @param alpha
	 *            similarity measurement
	 * @return
	 */
	public static ArrayList<Envelope> combineDensityMap(ArrayList<double[]> density, double granularityX, double granularityY, double alpha) {
		ArrayList<Envelope> combinedRegion = new ArrayList<Envelope>();
		// FIXME combineDensityMap
		int rowNum = density.size();
		for (int i = 0; i < rowNum; i++) {
			double[] aRow = density.get(i);
			for (int j = 0; j < aRow.length; j++) {
				double unitDensity = aRow[j];

				for (int k = 0; k < aRow.length; k++) {

				}

			}
		}
		return null;
	}

	public static void writePartition(String clusterRegionFile, ArrayList<Envelope> clusteredRegion) {
		// FIXME writePartition

	}

}
