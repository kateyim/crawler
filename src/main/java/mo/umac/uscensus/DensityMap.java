package mo.umac.uscensus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import mo.umac.uscensus.Grid.Flag;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Envelope;

public class DensityMap {

	protected static Logger logger = Logger.getLogger(DensityMap.class
			.getName());

	private double granularityX;

	private double granularityY;

	private int numGridX;

	private int numGridY;

	/**
	 * The longitude and latitude of the entire envelope
	 */
	private Envelope boardEnvelope;

	/**
	 * begin from 0 to numGridX-1
	 */
	private Grid[][] grids;

	// private ArrayList<Envelope> denseRegions = new ArrayList<Envelope>();

	public DensityMap(double granularityX, double granularityY, Envelope envelope, ArrayList<double[]> density) {
		logger.info("Building DensityMap...");
		this.granularityX = granularityX;
		this.granularityY = granularityY;
		this.boardEnvelope = envelope;
		numGridX = (int) Math.ceil(envelope.getWidth() / granularityX);
		numGridY = (int) Math.ceil(envelope.getHeight() / granularityY);
		// if (logger.isDebugEnabled()) {
		// logger.debug("density.size() = " + density.size());
		// logger.debug("numGridX = " + numGridX);
		// logger.debug("numGridY = " + numGridY);
		// }
		grids = new Grid[numGridX][numGridY];
		for (int i = 0; i < density.size(); i++) {
			if (logger.isDebugEnabled()) {
				logger.debug(i);
			}
			double[] aRow = density.get(i);
			for (int j = 0; j < aRow.length; j++) {
				double d = aRow[j];
				grids[i][j] = new Grid(i, j, d, Grid.Flag.UNVISITED);
			}
		}
	}

	/**
	 * combineDensityMap, first find the most dense region, begin to expand;
	 * </p> then find the most dense region in the remained area, begin to
	 * expand. </p> Iterate this process for 3-4 times. </p> At last partition
	 * the rest regions.
	 * 
	 * @param numIteration
	 *            now : only works for numIteration = 1; can be extended later
	 * @param alpha1
	 *            TODO
	 * @param alpha2
	 *            TODO
	 * @return final results
	 */
	public ArrayList<Envelope> cluster(int numIteration, double alpha1,
			double alpha2) {
		// FIXME yanhui cluster
		int i = 1;
		// clone this grid map for sorting the order.
		// ArrayList<Grid> sortedMap = clone(grids);
		// TODO lack the mapping relationship to the original grids (for tagging
		// unvisited and visited)
		// sortDensityMap(sortedMap);

		Envelope entireRegion = new Envelope(0, numGridX, 0, numGridY);
		ArrayList<Envelope> clusterGrids = new ArrayList<Envelope>();
		Envelope denseRegion = null;
		while (i <= numIteration) {
			// Grid seed = findTheDensest(sortedMap);
			Grid seed = getTheDensest();
			if (logger.isDebugEnabled()) {
				logger.debug(seed.toString());
			}
			denseRegion = expandFromMiddle(seed, alpha1, alpha2);
			clusterGrids = partition(entireRegion, denseRegion);
			i++;
		}
		// coverting
		ArrayList<Envelope> clusterEnvelopes = new ArrayList<Envelope>();
		// FIXME cancel comment
		// for (int j = 0; j < clusterGrids.size(); j++) {
		// clusterEnvelopes.add(convert(clusterGrids.get(j)));
		// }
		clusterEnvelopes.add(convert(denseRegion));
		return clusterEnvelopes;

	}

	/**
	 * cluster to a rectangle
	 * 
	 * @param numIteration
	 * @param alpha1
	 * @param alpha2
	 * @return
	 */
	public ArrayList<Envelope> cluster2(int numIteration, double alpha1,
			double alpha2) {
		// FIXME yanhui cluster
		int i = 1;
		// clone this grid map for sorting the order.
		// ArrayList<Grid> sortedMap = clone(grids);
		// TODO lack the mapping relationship to the original grids (for tagging
		// unvisited and visited)
		// sortDensityMap(sortedMap);

		Envelope entireRegion = new Envelope(0, numGridX, 0, numGridY);
		ArrayList<Envelope> clusterGrids = new ArrayList<Envelope>();
		Envelope denseRegion = null;
		while (i <= numIteration) {
			// Grid seed = findTheDensest(sortedMap);
			Grid seed = getTheDensest();
			if (logger.isDebugEnabled()) {
				logger.debug(seed.toString());
			}
			// denseRegion = expandToARec(seed, alpha1, alpha2);
			clusterGrids = partition(entireRegion, denseRegion);
			i++;
		}
		// coverting
		ArrayList<Envelope> clusterEnvelopes = new ArrayList<Envelope>();
		for (int j = 0; j < clusterGrids.size(); j++) {
			clusterEnvelopes.add(convert(clusterGrids.get(j)));
		}
		clusterEnvelopes.add(convert(denseRegion));
		return clusterEnvelopes;

	}

	private Grid getTheDensest() {
		double max = -1;
		Grid grid = null;
		for (int i = 0; i < numGridX; i++) {
			for (int j = 0; j < numGridY; j++) {
				if (max < grids[i][j].density) {
					max = grids[i][j].density;
					grid = new Grid(grids[i][j]);
				}
			}
		}
		return grid;
	}

	/**
	 * Convert from grid to latitude and longitude
	 * 
	 * @param clusterGrids
	 * @return
	 */
	private Envelope convert(Envelope grid) {
		// TODO need check
		double minX = boardEnvelope.getMinX();
		double minY = boardEnvelope.getMinY();
		double x1 = minX + grid.getMinX() * granularityX;
		double x2 = x1 + granularityX;
		double y1 = minY + grid.getMinY() * granularityY;
		double y2 = y1 + granularityY;
		return new Envelope(x1, x2, y1, y2);
	}

	/**
	 * Partition the whole region into rectangle. Reserve the dense regions,
	 * delete the 0 regions. </p> Use the following data: </p> tree0 </p>
	 * treeDense </p> grids </p> boardEnvelope, numGridX, numGridY,
	 * granularityX, granularityY
	 * 
	 * @param denseRegion
	 * @return in grid
	 */
	private ArrayList<Envelope> partition(Envelope entireRegion,
			Envelope denseRegion) {
		ArrayList<Envelope> clusterGrids = new ArrayList<Envelope>();
		// TODO need check

		Envelope e1 = new Envelope(entireRegion.getMinX(),
				denseRegion.getMinX(), denseRegion.getMinY(),
				entireRegion.getMaxY());
		Envelope e2 = new Envelope(denseRegion.getMinX(),
				entireRegion.getMaxX(), denseRegion.getMaxY(),
				entireRegion.getMaxY());
		Envelope e3 = new Envelope(denseRegion.getMaxX(),
				entireRegion.getMaxX(), entireRegion.getMinY(),
				denseRegion.getMaxY());
		Envelope e4 = new Envelope(entireRegion.getMinX(),
				denseRegion.getMaxX(), entireRegion.getMinY(),
				denseRegion.getMinY());
		clusterGrids.add(e1);
		clusterGrids.add(e2);
		clusterGrids.add(e3);
		clusterGrids.add(e4);
		return clusterGrids;
	}

	/**
	 * Find a dense region from the center point
	 * 
	 * @param alpha1
	 *            TODO
	 * @param alpha2
	 *            TODO
	 * @param centerGrid
	 * @return a clustered envelope
	 */
	private Envelope expandFromMiddle(Grid seed, double alpha1, double alpha2) {
		// The most widespread boundaries.
		double xLeft = seed.x;
		double xRight = seed.x;
		double yLeft = seed.y;
		double yRight = seed.y;
		ArrayList<Grid> borderGrid = new ArrayList<Grid>();

		Queue<Grid> queue = new LinkedList<Grid>();
		queue.add(seed);
		while (!queue.isEmpty()) {
			Grid oneGrid = queue.poll();
			ArrayList<Grid> udlrList = upDownLeftRight(oneGrid);
			for (int i = 0; i < udlrList.size(); i++) {
				Grid neighbor = udlrList.get(i);
				if (neighbor.flag == Flag.UNVISITED) {
					// simple similarity function
					double similarity = Math
							.abs((neighbor.density - seed.density)
									/ seed.density);
					if (similarity <= alpha2 && similarity >= alpha1) {
						neighbor.flag = Flag.VISITED;
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
						neighbor.flag = Flag.BORDER;
						borderGrid.add(neighbor);
					}
				}
			}
		}
		return densityEnvelopes(borderGrid, xLeft, xRight, yLeft, yRight);
	}

	/**
	 * find rectangles inside the borderGrid </p> now simply return the boundary
	 * rectangle
	 * 
	 * @param borderGrid
	 * @param xLeft
	 * @param xRight
	 * @param yLeft
	 * @param yRight
	 * @return
	 */
	private Envelope densityEnvelopes(ArrayList<Grid> borderGrid, double xLeft,
			double xRight, double yLeft, double yRight) {
		Envelope envelope = new Envelope(xLeft, xRight, yLeft, yRight);
		return envelope;
	}

	/**
	 * Find 4 or less than 4 neighbor grids
	 * 
	 * @param seed
	 * @return
	 */
	private ArrayList<Grid> upDownLeftRight(Grid seed) {
		ArrayList<Grid> udlrList = new ArrayList<Grid>();
		int xSeed = seed.x;
		int ySeed = seed.y;
		for (int i = -1; i <= 1; i = i + 2) {
			int x = xSeed + i;
			if (x >= 0 && x < numGridX) {
				Grid xGrid = grids[x][ySeed];
				udlrList.add(xGrid);
			}
		}
		for (int i = -1; i <= 1; i = i + 2) {
			int y = ySeed + i;
			if (y >= 0 && y < numGridY) {
				Grid yGrid = grids[xSeed][y];
				udlrList.add(yGrid);
			}
		}
		return udlrList;
	}

	private ArrayList<Grid> clone(Grid[][] grids) {
		int row = grids.length;
		int col = grids[0].length;
		ArrayList<Grid> clonedMap = new ArrayList<Grid>();

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				Grid aGrid = grids[i][j];
				Grid clonedGrid = new Grid(aGrid);
				clonedMap.add(clonedGrid);
			}
		}
		return clonedMap;
	}

	/**
	 * @param sortedMap
	 */
	private void sortDensityMap(ArrayList<Grid> sortedMap) {

		Collections.sort(sortedMap, new Comparator<Grid>() {
			public int compare(Grid one, Grid another) {
				Double oneDouble = new Double(one.density);
				Double anotherDouble = new Double(another.density);
				return oneDouble.compareTo(anotherDouble);
			}
		});

	}

	/**
	 * Find the most dense grid, eliminate the dense regions already found by
	 * previous steps.
	 * 
	 * @param sortedMap
	 * @param visitedList
	 *            TODO
	 * @return
	 */
	private Grid findTheDensest(ArrayList<Grid> sortedMap) {
		int size = sortedMap.size();
		for (int i = size - 1; i >= 0; i--) {
			Grid g = sortedMap.get(i);
			if (g.flag == Flag.UNVISITED) {
				return g;
			}
		}
		return null;

	}

	public double getGranularityX() {
		return granularityX;
	}

	public void setGranularityX(double granularityX) {
		this.granularityX = granularityX;
	}

	public double getGranularityY() {
		return granularityY;
	}

	public void setGranularityY(double granularityY) {
		this.granularityY = granularityY;
	}

	public Envelope getBoardEnvelope() {
		return boardEnvelope;
	}

	public void setBoardEnvelope(Envelope boardEnvelope) {
		this.boardEnvelope = boardEnvelope;
	}

	public Grid[][] getGrids() {
		return grids;
	}

	public void setGrids(Grid[][] grids) {
		this.grids = grids;
	}

}

class Grid {

	public int x;

	public int y;

	public double density;

	public Flag flag = Flag.UNVISITED;

	public enum Flag {
		VISITED, UNVISITED, BORDER
	}

	public Grid() {

	}

	public Grid(int x, int y, double density, Flag flag) {
		super();
		this.x = x;
		this.y = y;
		this.density = density;
		this.flag = flag;
	}

	public Grid(Grid anotherGrid) {
		this.x = anotherGrid.x;
		this.y = anotherGrid.y;
		this.density = anotherGrid.density;
		this.flag = anotherGrid.flag;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Grid: ");
		sb.append("[" + x + " , " + y + "], ");
		sb.append("density = " + density);
		sb.append(", " + flag);
		return sb.toString();

	}

}
