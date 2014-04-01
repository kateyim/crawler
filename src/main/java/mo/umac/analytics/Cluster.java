package mo.umac.analytics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

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
		//
		// ArrayList<Envelope> list = partition(envelope, denseRegion);
		return list;
	}

	private static double getDensity(Coordinate c) {
		double v = density.get((int) c.x)[(int) c.y];
		return v;
	}

	private static void initTag() {
		int rowN = density.size();
		int colN = density.get(0).length;
		tag = new boolean[rowN][colN];
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

			// intersect with one edge

			// intersect with one corner

			denseRegionList.add(e);
		}

		return sparseRegion;
	}

	enum DIRECTION {
		LEFT, RIGHT, UP, DOWN, LEFT_DOWN, LEFT_UP, RIGHT_DOWN, RIGHT_UP
	}

	private static DIRECTION interDirection(Envelope entire, Envelope dense) {
		// LEFT, RIGHT, UP, DOWN
		boolean[] flags = { false, false, false, false };
		DIRECTION d;
		if (entire.getMinX() > dense.getMinX()) {
			flags[0] = true;
		}
		if (entire.getMaxX() < dense.getMaxX()) {
			flags[1] = true;
		}
		if (entire.getMinY() > dense.getMinY()) {
			flags[4] = true;
		}
		if (entire.getMaxY() < dense.getMaxY()) {
			flags[3] = true;
		}

		if (flags[0] & !flags[1] & !flags[2] & !flags[3]) {
			return DIRECTION.LEFT;
		}
		if (!flags[0] & flags[1] & !flags[2] & !flags[3]) {
			return DIRECTION.RIGHT;
		}
		if (!flags[0] & !flags[1] & flags[2] & !flags[3]) {
			return DIRECTION.UP;
		}
		if (!flags[0] & !flags[1] & !flags[2] & flags[3]) {
			return DIRECTION.DOWN;
		}

		return DIRECTION.LEFT;
	}

	public static ArrayList<Envelope> partition(Envelope entireRegion, ArrayList<Envelope> denseRegion) {
		ArrayList<Envelope> allRegion = new ArrayList<Envelope>();

		ArrayList<Envelope> finalSparseRegion = new ArrayList<Envelope>();
		ArrayList<Envelope> finalDenseRegion = new ArrayList<Envelope>();

		Queue<Envelope> queueDense = new LinkedList<Envelope>();
		for (int i = 0; i < denseRegion.size(); i++) {
			Envelope clone = new Envelope(denseRegion.get(i));
			finalDenseRegion.add(clone);
			queueDense.add(clone);
		}
		Queue<Envelope> queueSparse = new LinkedList<Envelope>();
		queueSparse.add(entireRegion);
		finalSparseRegion.add(entireRegion);
		//
		while (!queueSparse.isEmpty()) {
			Envelope sparse = queueSparse.poll();
			//
			int i = 0;
			boolean intersectOne = false;
			ArrayList<Envelope> sparseRegionParts = null;
			while (!queueDense.isEmpty()) {
				Envelope dense = queueDense.peek();
				if (sparse.intersects(dense)) {
					intersectOne = true;
					ArrayList<Envelope> denseRegionList = new ArrayList<Envelope>();
					denseRegionList.add(dense);
					sparseRegionParts = partitionOverlap(sparse, denseRegionList);
					queueSparse.addAll(sparseRegionParts);
					if (denseRegionList.size() != 0) {
						queueDense.remove(dense);
						queueDense.addAll(denseRegionList);
						finalDenseRegion.remove(i);
						finalDenseRegion.addAll(denseRegionList);
					}
					break;
				}
				i++;
			}
			if (intersectOne) {
				finalSparseRegion.remove(0);
				finalSparseRegion.addAll(sparseRegionParts);
			}
		}
		allRegion.addAll(finalDenseRegion);
		allRegion.addAll(finalSparseRegion);
		return allRegion;
	}

}
