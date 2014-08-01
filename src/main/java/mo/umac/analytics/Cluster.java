package mo.umac.analytics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import utils.MaximalRectangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class Cluster {

	protected static Logger logger = Logger.getLogger(Cluster.class.getName());

	public static int numGridX;
	public static int numGridY;
	public static Envelope envelope;
	public static double granularityX;
	public static double granularityY;
	public static ArrayList<double[]> density;
	public static boolean[][] tag;
	public static double EPSILON_A = 0.000001;

	public static ArrayList<Envelope> cluster(double gX, double gY, Envelope e, ArrayList<double[]> d, double a) {
		logger.info("clustering...");
		density = d;
		envelope = e;
		granularityX = gX;
		granularityY = gY;

		numGridX = (int) Math.ceil(envelope.getWidth() / granularityX);
		numGridY = (int) Math.ceil(envelope.getHeight() / granularityY);
		if (logger.isDebugEnabled()) {
			logger.debug("numGridX = " + numGridX + ", numGridY = " + numGridY);
		}
		initTag();
		//
		Coordinate seed = getTheDensest();
		if (logger.isDebugEnabled()) {
			logger.debug("seed = " + seed.toString() + ", density = " + getDensity(seed));
		}
		Envelope denseGrid = expandFromMiddle(seed, a);
		Envelope denseRegion = converseEnvelope(denseGrid);
		//
		ArrayList<Envelope> list = partition(envelope, denseRegion);
		// ArrayList<Envelope> list = new ArrayList<Envelope>();
		list.add(denseRegion);
		return list;
	}

	public static ArrayList<Envelope> cluster(double gX, double gY, Envelope e, ArrayList<double[]> d, double a, int loop) {
		logger.info("clustering...");
		density = d;
		envelope = e;
		granularityX = gX;
		granularityY = gY;

		numGridX = (int) Math.ceil(envelope.getWidth() / granularityX);
		numGridY = (int) Math.ceil(envelope.getHeight() / granularityY);
		if (logger.isDebugEnabled()) {
			logger.debug("numGridX = " + numGridX + ", numGridY = " + numGridY);
		}
		initTag();
		//
		ArrayList<Envelope> list = new ArrayList<Envelope>();
		for (int i = 0; i < loop; i++) {
			Coordinate seed = getTheDensest();
			if (logger.isDebugEnabled()) {
				logger.debug("seed = " + seed.toString() + ", density = " + getDensity(seed));
			}
			Envelope denseGrid = expandFromMiddle(seed, a);
			Envelope denseRegion = converseEnvelope(denseGrid);

			list.add(denseRegion);

		}
		return list;
	}

	public static ArrayList<Envelope> clusterZero(double gX, double gY, Envelope e, ArrayList<double[]> d, double a, int loop) {
		logger.info("clustering...");
		density = d;
		envelope = e;
		granularityX = gX;
		granularityY = gY;

		numGridX = (int) Math.ceil(envelope.getWidth() / granularityX);
		numGridY = (int) Math.ceil(envelope.getHeight() / granularityY);
		if (logger.isDebugEnabled()) {
			logger.debug("numGridX = " + numGridX + ", numGridY = " + numGridY);
		}
		initTag();
		// double[][] densitiesForRectangle = rewriteDesity();
		// cluster zeros: starting from the four corners of the envelope
		ArrayList<Envelope> list0 = new ArrayList<Envelope>();
		// 4 corner seeds
		for (int i = 0; i < numGridX; i += numGridX - 1) {
			for (int j = 0; j < numGridY; j += numGridY - 1) {
				// for testing
				Coordinate seed = new Coordinate(i, j);
				// Envelope zeroGrid = expandFromCorner(numGridX, numGridY, seed, EPSILON_A);
				Envelope zeroGrid = findMaxRectangleFromCorner(seed);
				if (logger.isDebugEnabled()) {
					logger.debug("zeroGrid: " + zeroGrid.toString());
				}
				Envelope zeroRegion = converseEnvelope(zeroGrid);
				list0.add(zeroRegion);
			}
		}

		ArrayList<Envelope> list = new ArrayList<Envelope>();
		list.addAll(list0);
		return list;
	}

	public static ArrayList<Envelope> clusterDensest(double gX, double gY, Envelope e, ArrayList<double[]> d, double a, int loop) {
		logger.info("clustering...");
		density = d;
		envelope = e;
		granularityX = gX;
		granularityY = gY;

		numGridX = (int) Math.ceil(envelope.getWidth() / granularityX);
		numGridY = (int) Math.ceil(envelope.getHeight() / granularityY);
		if (logger.isDebugEnabled()) {
			logger.debug("numGridX = " + numGridX + ", numGridY = " + numGridY);
		}
		initTag();
		// cluster densities
		ArrayList<Envelope> listDense = new ArrayList<Envelope>();
		for (int i = 0; i < loop; i++) {
			Coordinate seed = getTheDensest();
			if (logger.isDebugEnabled()) {
				logger.debug("seed = " + seed.toString() + ", density = " + getDensity(seed));
			}
			Envelope denseGrid = expandFromMiddle(seed, a);
//			listDense.add(denseGrid);
			Envelope denseRegion = converseEnvelope(denseGrid);
			listDense.add(denseRegion);
		}
		ArrayList<Envelope> list = new ArrayList<Envelope>();
		list.addAll(listDense);
		return list;
	}

	/**
	 * For finding the rectangles
	 * 
	 * @param density2
	 */
	private static double[][] rewriteDesity() {
		int numRow = density.size();
		int numCol = density.get(0).length;
		double[][] densitiesForRectangle = new double[numRow][numCol];
		for (int i = 0; i < density.size(); i++) {
			double[] aRow = density.get(i);
			for (int j = 0; j < aRow.length; j++) {
				double den = aRow[j];
				if (den > 0) {
					densitiesForRectangle[i][j] = 0;
				} else {
					densitiesForRectangle[i][j] = 1;
				}
			}
		}
		return densitiesForRectangle;
	}

	private static double getDensity(Coordinate c) {
		double v = density.get((int) c.x)[(int) c.y];
		return v;
	}

	private static void initTag() {
		int rowN = density.size();
		int colN = density.get(0).length;
		tag = new boolean[rowN][colN];
		if (logger.isDebugEnabled()) {
			logger.debug("tag: [" + rowN + "], [" + colN + "]");
		}
		for (int i = 0; i < rowN; i++) {
			for (int j = 0; j < colN; j++) {
				tag[i][j] = false;
			}
		}
	}

	private static Coordinate getTheDensest() {
		double max = -1;
		int xDen = 0;
		int yDen = 0;
		for (int i = 0; i < density.size(); i++) {
			double[] aRow = density.get(i);
			// double length = aRow.length;
			for (int j = 0; j < numGridY; j++) {
				if (max < aRow[j] && tag[i][j] == false) {
					max = aRow[j];
					xDen = i;
					yDen = j;
				}
			}
		}
		return new Coordinate(xDen, yDen);
	}

	private static Envelope expandFromMiddle(Coordinate seed, double a) {
		double xLeft = seed.x;
		double xRight = seed.x;
		double yLeft = seed.y;
		double yRight = seed.y;
		ArrayList<Coordinate> borderGrid = new ArrayList<Coordinate>();

		Queue<Coordinate> queue = new LinkedList<Coordinate>();
		queue.add(seed);
		while (!queue.isEmpty()) {
			Coordinate one = queue.poll();
			ArrayList<Coordinate> udlrList = upDownLeftRight(density, one);
			for (int i = 0; i < udlrList.size(); i++) {
				Coordinate neighbor = udlrList.get(i);
				if (tag[(int) neighbor.x][(int) neighbor.y] == false) {
					// simple similarity function
					double densityNeighbor = getDensity(neighbor);
					double densitySeed = getDensity(seed);
					double similarity = Math.abs(densityNeighbor - densitySeed) / densitySeed;
					if (similarity <= a) {
						tag[(int) neighbor.x][(int) neighbor.y] = true;
						queue.add(neighbor);
						if (xLeft > neighbor.x) {
							xLeft = neighbor.x;
						}
						if (xRight < neighbor.x) {
							xRight = neighbor.x;
						}
						if (yLeft > neighbor.y) {
							yLeft = neighbor.y;
						}
						if (yRight < neighbor.y) {
							yRight = neighbor.y;
						}
					} else {
						borderGrid.add(neighbor);
					}
				}
			}
		}
		return new Envelope(xLeft, xRight, yLeft, yRight);
	}

	/**
	 * The similarity computation is different from the method expandFromMiddle()
	 * 
	 * @param seed
	 * @param a
	 * @return
	 */
	private static Envelope expandFromCorner(int numGridX, int numGridY, Coordinate seed, double a) {
		// if (logger.isDebugEnabled()) {
		// logger.debug("seed = " + seed.toString());
		// }
		ArrayList<Coordinate> borderGrid = new ArrayList<Coordinate>();
		// add at 2014-5-20
		double[] border = new double[numGridX];
		for (int i = 0; i < numGridY; i++) {
			if ((int) seed.x == 0) {
				border[i] = numGridX - 1;
			} else {
				border[i] = 0;
			}
		}
		// FIXME fill the values
		double[][] borders = new double[numGridY][numGridX];
		for (int i = 0; i < numGridY; i++) {
			for (int j = 0; j < numGridX; j++) {
				borders[i][j] = 0;
			}
		}
		// if (logger.isDebugEnabled()) {
		// logger.debug("seed.x = " + seed.x);
		// logger.debug("initialize border to " + border[0]);
		// }
		// for the seed:
		double densitySeed = getDensity(seed);
		if (densitySeed <= a) {
			tag[(int) seed.x][(int) seed.y] = true;
		} else {
			logger.error("The density of the corner is not 0!");
		}

		Queue<Coordinate> queue = new LinkedList<Coordinate>();
		queue.add(seed);
		while (!queue.isEmpty()) {
			Coordinate one = queue.poll();
			ArrayList<Coordinate> udlrList = upDownLeftRight(density, one);
			for (int i = 0; i < udlrList.size(); i++) {
				Coordinate neighbor = udlrList.get(i);

				if (tag[(int) neighbor.x][(int) neighbor.y] == false) { // not visited before
					// simple similarity function
					double densityNeighbor = getDensity(neighbor);
					// if (logger.isDebugEnabled()) {
					// logger.debug("neighbor: " + neighbor.toString());
					// logger.debug("densityNeighbor = " + densityNeighbor);
					// }
					//
					tag[(int) neighbor.x][(int) neighbor.y] = true;
					if (densityNeighbor <= a) {
						queue.add(neighbor);
					} else {
						borderGrid.add(neighbor);
						//
						if ((int) seed.x == 0) { // expand to the right
							if (border[(int) neighbor.y] > neighbor.x) {
								border[(int) neighbor.y] = neighbor.x;
								//
								// logger.debug("border[(int) neighbor.x] = " + border[(int) neighbor.x]);
							}
						} else { // expand to the left
							if (border[(int) neighbor.y] < neighbor.x) {
								border[(int) neighbor.y] = neighbor.x;
								// logger.debug("border[(int) neighbor.x] = " + border[(int) neighbor.x]);
							}
						}
					}
				}
			}
		}
		// if (logger.isDebugEnabled()) {
		// logger.debug("printing border[]:");
		// for (int i = 0; i < border.length; i++) {
		// logger.debug(i + ": " + border[i]);
		// }
		// logger.debug("end printing border[]:");
		// }

		// return new Envelope(minX, maxX, minY, maxY);
		return findLargestRectangle(numGridX, numGridY, borders);
	}

	/**
	 * cut off to a part containing the seed grid.
	 * 
	 * @param seed
	 * @param densitiesForRectangle
	 * @return
	 */
	private static Envelope findMaxRectangleFromCorner(Coordinate seed) {
		// extract one part

		// reserve the the farthest point p, then the area from the seed to p containing all 0;
		ArrayList<Coordinate> zeroAreaList = new ArrayList<Coordinate>();

		// seed is 0,0
		if ((int) seed.x == 0 && (int) seed.y == 0) {
			for (int i = 0; i < numGridX; i++) {
				for (int j = 0; j < numGridY; j++) {
					Coordinate farthestPoint = new Coordinate(i, j);
					boolean allZero = checkAllZero(seed, farthestPoint);
					if (allZero) {
						Coordinate c = new Coordinate(i, j);
						zeroAreaList.add(c);
					}
				}
			}
		}

		// seed is 0,numGridY-1
		if ((int) seed.x == 0 && (int) seed.y == numGridY - 1) {
			for (int i = 0; i < numGridX; i++) {
				for (int j = numGridY - 1; j >= 0; j--) {
					Coordinate farthestPoint = new Coordinate(i, j);
					boolean allZero = checkAllZero(seed, farthestPoint);
					if (allZero) {
						Coordinate c = new Coordinate(i, j);
						zeroAreaList.add(c);
					}
				}
			}
		}
		// seed is numGridX-1,0
		if ((int) seed.x == numGridX - 1 && (int) seed.y == 0) {
			for (int i = numGridX - 1; i >= 0; i--) {
				for (int j = 0; j < numGridY; j++) {
					Coordinate farthestPoint = new Coordinate(i, j);
					boolean allZero = checkAllZero(seed, farthestPoint);
					if (allZero) {
						Coordinate c = new Coordinate(i, j);
						zeroAreaList.add(c);
					}
				}
			}
		}
		// seed is numGridX-1,numGridY-1
		if ((int) seed.x == numGridX - 1 && (int) seed.y == numGridY - 1) {
			for (int i = numGridX - 1; i >= 0; i--) {
				for (int j = numGridY - 1; j >= 0; j--) {
					Coordinate farthestPoint = new Coordinate(i, j);
					boolean allZero = checkAllZero(seed, farthestPoint);
					if (allZero) {
						Coordinate c = new Coordinate(i, j);
						zeroAreaList.add(c);
					}
				}
			}
		}

		double maxArea = 0;
		Coordinate maxCoordiante = new Coordinate();
		// find the max area
		for (int k = 0; k < zeroAreaList.size(); k++) {
			Coordinate c = zeroAreaList.get(k);
			double area = area(seed, c);
			if (area >= maxArea) {
				maxArea = area;
				maxCoordiante = c;
			}
		}

		// TODO why maxCoordiante not update
		Envelope envelope = new Envelope(seed, maxCoordiante);
		return envelope;
	}

	private static double area(Coordinate seed, Coordinate c) {
		double length = Math.abs(seed.x - c.x);
		double width = Math.abs(seed.y - c.y);
		return length * width;
	}

	private static boolean checkAllZero(Coordinate seed, Coordinate farthestPoint) {
		int minX = numGridX;
		int minY = numGridY;
		int maxX = 0;
		int maxY = 0;
		if (seed.x <= farthestPoint.x) {
			minX = (int) seed.x;
			maxX = (int) farthestPoint.x;
		} else {
			minX = (int) farthestPoint.x;
			maxX = (int) seed.x;
		}
		if (seed.y <= farthestPoint.y) {
			minY = (int) seed.y;
			maxY = (int) farthestPoint.y;
		} else {
			minY = (int) farthestPoint.y;
			maxY = (int) seed.y;
		}

		for (int i = minX; i <= maxX; i++) {
			for (int j = minY; j <= maxY; j++) {
				if (density.get(i)[j] != 0) {
					return false;
				}
			}
		}
		return true;
	}

	private static Envelope findLargestRectangle(int numGridX, int numGridY, double[][] borders) {
		MaximalRectangle mr = new MaximalRectangle();
		try {
			Envelope envelope = mr.rectangle(numGridY, numGridX, borders);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return envelope;
	}

	private static Envelope converseEnvelope(Envelope denseGrid) {
		double x1 = envelope.getMinX() + denseGrid.getMinX() * granularityX;
		double x2 = envelope.getMinX() + (denseGrid.getMaxX() + 1) * granularityX;
		double y1 = envelope.getMinY() + denseGrid.getMinY() * granularityY;
		double y2 = envelope.getMinY() + (denseGrid.getMaxY() + 1) * granularityY;
		Envelope envelope = new Envelope(x1, x2, y1, y2);
		return envelope;
	}

	private static ArrayList<Coordinate> upDownLeftRight(ArrayList<double[]> density, Coordinate seed) {
		ArrayList<Coordinate> udlrList = new ArrayList<Coordinate>();
		int xSeed = (int) seed.x;
		int ySeed = (int) seed.y;
		for (int i = -1; i <= 1; i = i + 2) {
			int x = xSeed + i;
			if (x >= 0 && x < numGridX) {
				Coordinate c = new Coordinate(x, ySeed);
				udlrList.add(c);
			}
		}
		for (int i = -1; i <= 1; i = i + 2) {
			int y = ySeed + i;
			if (y >= 0 && y < numGridY) {
				Coordinate c = new Coordinate(xSeed, y);
				udlrList.add(c);
			}
		}
		return udlrList;
	}

	/**
	 * begin from the 4 corners
	 */
	private void discardZero() {

	}

	private static ArrayList<Envelope> partition(Envelope entireRegion, Envelope denseRegion) {
		ArrayList<Envelope> sparseRegion = new ArrayList<Envelope>();
		Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), denseRegion.getMinY(), entireRegion.getMaxY());
		Envelope e2 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
		Envelope e3 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
		Envelope e4 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
		sparseRegion.add(e1);
		sparseRegion.add(e2);
		sparseRegion.add(e3);
		sparseRegion.add(e4);
		return sparseRegion;
	}

	/**
	 * @param entireRegion
	 * @param denseRegionList
	 *            : changed to partitioned dense regions
	 * @return
	 */
	private static ArrayList<Envelope> partitionOverlap(Envelope entireRegion, ArrayList<Envelope> denseRegionList) {
		ArrayList<Envelope> sparseRegion = new ArrayList<Envelope>();
		ArrayList<Envelope> newDenseRegion = new ArrayList<Envelope>();
		Envelope denseRegion = denseRegionList.get(0);
		denseRegionList.clear();
		if (entireRegion.contains(denseRegion)) {
			Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), denseRegion.getMinY(), entireRegion.getMaxY());
			Envelope e2 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
			Envelope e3 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
			Envelope e4 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
			sparseRegion.add(e1);
			sparseRegion.add(e2);
			sparseRegion.add(e3);
			sparseRegion.add(e4);
		} else {
			double x1 = entireRegion.getMinX();
			double x2 = denseRegion.getMinX();
			double y1 = denseRegion.getMinY();
			double y2 = entireRegion.getMaxY();

			// LEFT, RIGHT, UP, DOWN
			boolean[] flags = { false, false, false, false };
			if (entireRegion.getMinX() > denseRegion.getMinX()) {
				flags[0] = true;
			}
			if (entireRegion.getMaxX() < denseRegion.getMaxX()) {
				flags[1] = true;
			}
			if (entireRegion.getMinY() > denseRegion.getMinY()) {
				flags[2] = true;
			}
			if (entireRegion.getMaxY() < denseRegion.getMaxY()) {
				flags[3] = true;
			}

			if (flags[0] & !flags[1] & !flags[2] & !flags[3]) {
				// return DIRECTION.LEFT;
				Envelope e2 = new Envelope(entireRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
				Envelope e3 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				Envelope e4 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
				sparseRegion.add(e2);
				sparseRegion.add(e3);
				sparseRegion.add(e4);
				Envelope d1 = new Envelope(denseRegion.getMinX(), entireRegion.getMinX(), denseRegion.getMinY(), denseRegion.getMaxY());
				Envelope d11 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), denseRegion.getMinY(), denseRegion.getMaxY());
				newDenseRegion.add(d1);
				newDenseRegion.add(d11);

			}
			if (!flags[0] & flags[1] & !flags[2] & !flags[3]) {
				// return DIRECTION.RIGHT;
				Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), denseRegion.getMinY(), entireRegion.getMaxY());
				Envelope e2 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
				Envelope e4 = new Envelope(entireRegion.getMinX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
				sparseRegion.add(e1);
				sparseRegion.add(e2);
				sparseRegion.add(e4);
				Envelope d3 = new Envelope(entireRegion.getMaxX(), denseRegion.getMaxX(), denseRegion.getMinY(), denseRegion.getMaxY());
				Envelope d33 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMinY(), denseRegion.getMaxY());
				newDenseRegion.add(d3);
				newDenseRegion.add(d33);
			}
			if (!flags[0] & !flags[1] & flags[2] & !flags[3]) {
				// return DIRECTION.UP;
				Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), denseRegion.getMinY(), entireRegion.getMaxY());
				Envelope e3 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				Envelope e4 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
				sparseRegion.add(e1);
				sparseRegion.add(e3);
				sparseRegion.add(e4);
				Envelope d2 = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMaxY(), denseRegion.getMaxY());
				Envelope d22 = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMaxY());
				newDenseRegion.add(d2);
				newDenseRegion.add(d22);
			}
			if (!flags[0] & !flags[1] & !flags[2] & flags[3]) {
				// return DIRECTION.DOWN;
				Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), entireRegion.getMinY(), entireRegion.getMaxY());
				Envelope e2 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
				Envelope e3 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				sparseRegion.add(e1);
				sparseRegion.add(e2);
				sparseRegion.add(e3);
				Envelope d4 = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMinY());
				Envelope d44 = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				newDenseRegion.add(d4);
				newDenseRegion.add(d44);
			}
			if (flags[0] & !flags[1] & flags[2] & !flags[3]) {
				// left-up
				Envelope e3 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), entireRegion.getMaxY());
				Envelope e4 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
				sparseRegion.add(e3);
				sparseRegion.add(e4);
				Envelope d = new Envelope(denseRegion.getMinX(), entireRegion.getMinX(), denseRegion.getMinY(), denseRegion.getMaxY());
				Envelope dd = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMaxY());
				Envelope ddd = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMaxY(), denseRegion.getMaxY());
				newDenseRegion.add(d);
				newDenseRegion.add(dd);
				newDenseRegion.add(ddd);

			}
			if (flags[0] & !flags[1] & !flags[2] & flags[3]) {
				// left-down
				Envelope e2 = new Envelope(entireRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
				Envelope e3 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				sparseRegion.add(e2);
				sparseRegion.add(e3);
				Envelope d = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMinY());
				Envelope dd = new Envelope(denseRegion.getMinX(), entireRegion.getMinX(), entireRegion.getMinY(), denseRegion.getMaxY());
				Envelope ddd = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				newDenseRegion.add(d);
				newDenseRegion.add(dd);
				newDenseRegion.add(ddd);

			}
			if (!flags[0] & flags[1] & flags[2] & !flags[3]) {
				// right-up
				Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), denseRegion.getMinY(), entireRegion.getMaxY());
				Envelope e4 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
				sparseRegion.add(e1);
				sparseRegion.add(e4);
				Envelope d = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMaxY(), denseRegion.getMaxY());
				Envelope dd = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMaxY());
				Envelope ddd = new Envelope(entireRegion.getMaxX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMaxY());
				newDenseRegion.add(d);
				newDenseRegion.add(dd);
				newDenseRegion.add(ddd);

			}
			if (!flags[0] & flags[1] & !flags[2] & flags[3]) {
				// right-down
				Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), entireRegion.getMinY(), entireRegion.getMaxY());
				Envelope e2 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
				sparseRegion.add(e1);
				sparseRegion.add(e2);
				Envelope d = new Envelope(entireRegion.getMaxX(), denseRegion.getMaxX(), denseRegion.getMinY(), denseRegion.getMaxY());
				Envelope dd = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMinY());
				Envelope ddd = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				newDenseRegion.add(d);
				newDenseRegion.add(dd);
				newDenseRegion.add(ddd);
			}
			if (flags[0] & !flags[1] & flags[2] & flags[3]) {
				// left-3
				Envelope e1 = new Envelope(denseRegion.getMaxX(), entireRegion.getMaxX(), entireRegion.getMinY(), entireRegion.getMaxY());
				sparseRegion.add(e1);
				Envelope d1 = new Envelope(denseRegion.getMinX(), entireRegion.getMinX(), entireRegion.getMinY(), denseRegion.getMaxY());
				Envelope d2 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMaxY(), denseRegion.getMaxY());
				Envelope d3 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMinY(), entireRegion.getMaxY());
				Envelope d4 = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMinY());
				newDenseRegion.add(d1);
				newDenseRegion.add(d2);
				newDenseRegion.add(d3);
				newDenseRegion.add(d4);
			}
			if (!flags[0] & flags[1] & flags[2] & flags[3]) {
				// right-3
				Envelope e1 = new Envelope(entireRegion.getMinX(), denseRegion.getMinX(), entireRegion.getMinY(), denseRegion.getMaxY());
				sparseRegion.add(e1);
				Envelope d1 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), entireRegion.getMinY(), entireRegion.getMaxY());
				Envelope d2 = new Envelope(denseRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMaxY(), denseRegion.getMaxY());
				Envelope d3 = new Envelope(entireRegion.getMaxX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMaxY());
				Envelope d4 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMinY());
				newDenseRegion.add(d1);
				newDenseRegion.add(d2);
				newDenseRegion.add(d3);
				newDenseRegion.add(d4);
			}
			if (flags[0] & flags[1] & flags[2] & !flags[3]) {
				// up-3
				Envelope e1 = new Envelope(entireRegion.getMinX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMinY());
				sparseRegion.add(e1);
				//
				Envelope d1 = new Envelope(denseRegion.getMinX(), entireRegion.getMinX(), denseRegion.getMinY(), denseRegion.getMaxY());
				Envelope d2 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), entireRegion.getMaxY(), denseRegion.getMaxY());
				Envelope d3 = new Envelope(entireRegion.getMaxX(), denseRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMaxY());
				Envelope d4 = new Envelope(entireRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMaxY());
				newDenseRegion.add(d1);
				newDenseRegion.add(d2);
				newDenseRegion.add(d3);
				newDenseRegion.add(d4);
			}
			if (flags[0] & flags[1] & !flags[2] & flags[3]) {
				// down-3
				Envelope e1 = new Envelope(entireRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMaxY(), entireRegion.getMaxY());
				sparseRegion.add(e1);
				//
				Envelope d1 = new Envelope(denseRegion.getMinX(), entireRegion.getMinX(), entireRegion.getMinY(), denseRegion.getMaxY());
				Envelope d2 = new Envelope(entireRegion.getMinX(), entireRegion.getMaxX(), entireRegion.getMinY(), denseRegion.getMaxY());
				Envelope d3 = new Envelope(entireRegion.getMinX(), denseRegion.getMaxX(), denseRegion.getMinY(), denseRegion.getMaxY());
				Envelope d4 = new Envelope(denseRegion.getMinX(), entireRegion.getMaxX(), denseRegion.getMinY(), entireRegion.getMinY());
				newDenseRegion.add(d1);
				newDenseRegion.add(d2);
				newDenseRegion.add(d3);
				newDenseRegion.add(d4);

			}
			if (flags[0] & flags[1] & flags[2] & flags[3]) {
				// no sparse, only dense
				newDenseRegion.add(denseRegion);

			}
			denseRegionList.addAll(newDenseRegion);
		}

		return sparseRegion;
	}

	// enum DIRECTION {
	// LEFT, RIGHT, UP, DOWN, LEFT_DOWN, LEFT_UP, RIGHT_DOWN, RIGHT_UP
	// }

	/**
	 * @param entireRegion
	 * @param denseRegion
	 * @return
	 */
	public static ArrayList<Envelope> partition(Envelope entireRegion, ArrayList<Envelope> denseRegion) {
		ArrayList<Envelope> allRegion = new ArrayList<Envelope>();

		ArrayList<Envelope> finalSparseRegion = new ArrayList<Envelope>();
		ArrayList<Envelope> finalDenseRegion = new ArrayList<Envelope>();

		Queue<Envelope> queueDense = new LinkedList<Envelope>();
		for (int i = 0; i < denseRegion.size(); i++) {
			Envelope clone = new Envelope(denseRegion.get(i));
			queueDense.add(clone);
		}
		Queue<Envelope> queueSparse = new LinkedList<Envelope>();
		queueSparse.add(entireRegion);
		finalSparseRegion.add(entireRegion);
		//
		while (!queueSparse.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("queueSparse'size = " + queueSparse.size());
			}
			Envelope sparse = queueSparse.poll();
			if (logger.isDebugEnabled()) {
				logger.debug("sparse = " + sparse.toString());
			}
			//
			boolean intersectOne = false;
			ArrayList<Envelope> sparseRegionParts = null;
			ArrayList<Envelope> denseRegionList = new ArrayList<Envelope>();
			if (logger.isDebugEnabled()) {
				logger.debug("queueDense'size = " + queueDense.size());
			}
			int i = 0;
			while (!queueDense.isEmpty()) {
				Envelope dense = queueDense.poll();

				if (logger.isDebugEnabled()) {
					logger.debug("dense = " + dense.toString());
				}
				if (sparse.intersects(dense)) {
					if (logger.isDebugEnabled()) {
						logger.debug("intersect with " + dense.toString());
					}
					intersectOne = true;
					denseRegionList.clear();
					denseRegionList.add(dense);
					sparseRegionParts = partitionOverlap(sparse, denseRegionList);
					if (logger.isDebugEnabled()) {
						logger.debug("after partition... ");
						logger.debug("sparseRegionParts... ");
						for (int t = 0; t < sparseRegionParts.size(); t++) {
							logger.debug(sparseRegionParts.get(t).toString());
						}
						logger.debug("denseRegionList... ");
						for (int t = 0; t < denseRegionList.size(); t++) {
							logger.debug(denseRegionList.get(t).toString());
						}
					}
					queueSparse.addAll(sparseRegionParts);
					queueDense.remove(dense);
					if (logger.isDebugEnabled()) {
						logger.debug("queueSparse.addAll(sparseRegionParts);");
						logger.debug("queueDense.remove(dense): " + dense.toString());
					}

					if (denseRegionList.size() != 0) {
						queueDense.addAll(denseRegionList);
						finalDenseRegion.addAll(denseRegionList);
						if (logger.isDebugEnabled()) {
							logger.debug("queueDense.addAll(denseRegionList);");
							logger.debug("finalDenseRegion.addAll(denseRegionList);");
						}
					} else {
						finalDenseRegion.add(dense);
						if (logger.isDebugEnabled()) {
							logger.debug("finalDenseRegion.add(dense);");
						}
					}
					break;
				} else {
					queueDense.add(dense);
					if (logger.isDebugEnabled()) {
						logger.debug("queueDense.add(dense);");
					}
				}
				i++;
				if (i >= queueDense.size()) {
					break;
				}
			}
			if (intersectOne) {
				finalSparseRegion.remove(0);
				finalSparseRegion.addAll(sparseRegionParts);
				if (logger.isDebugEnabled()) {
					logger.debug("intersect: ");
					logger.debug("finalSparseRegion.remove(0);");
					logger.debug("finalSparseRegion.addAll(sparseRegionParts);");
				}
				//
			} else {
				finalSparseRegion.add(sparse);
				if (logger.isDebugEnabled()) {
					logger.debug("intersect no: ");
					logger.debug("finalSparseRegion.add(sparse);");
				}
			}
		}
		allRegion.addAll(finalDenseRegion);
		allRegion.addAll(finalSparseRegion);
		return allRegion;
	}

}
