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

	protected static Logger logger = Logger.getLogger(DensityMap.class.getName());

	private double granularityX;

	private double granularityY;

	private int numGridX;

	private int numGridY;

	private int numGrid;

	/**
	 * The longitude and latitude of the entire envelope
	 */
	private Envelope boardEnvelope;

	/**
	 * begin from 0 to numGridX-1
	 */
	private Grid[][] grids;

	private ArrayList<Envelope> denseRegions = new ArrayList<Envelope>();
	private ArrayList<Envelope> zeroRegions = new ArrayList<Envelope>();

	public DensityMap(double granularityX, double granularityY, Envelope envelope, ArrayList<double[]> density) {
		this.granularityX = granularityX;
		this.granularityY = granularityY;
		this.boardEnvelope = envelope;
		// FIXME complemented (Do all field variables are useful?)
		init();
	}

	private void init() {
		// TODO
	}

	/**
	 * combineDensityMap, first find the most dense region, begin to expand;
	 * </p> then find the most dense region in the remained area, begin to expand.
	 * </p> Iterate this process for 3-4 times.
	 * </p> At last partition the rest regions.
	 * 
	 * @param alpha
	 *            is the threshold for computing similarity
	 * @return final results
	 */
	public ArrayList<Envelope> cluster(double alpha, int numIteration) {
		boolean stop = false;
		int c = 1;
		// clone this grid map for sorting the order.
		ArrayList<Grid> sortedMap = clone(grids);
		// TODO lack the mapping relationship to the original grids (for tagging unvisited and visited)
		sortDensityMap(sortedMap);

		while (!stop) {

			if (c >= numIteration) {
				stop = true;
			}

			Grid seed = findTheDensest(sortedMap);
			ArrayList<Envelope> denseRegion = expandFromMiddle(seed, alpha);

			// treeDense.addRectangle(c, denseRegion);
			denseRegions.addAll(denseRegion);
			c++;
		}
		// TODO Dealing with the zero grids, delete zero grids from the 4 corners of the rectangle.
		// Because using a big rectangle to bound the whole region is reasonable but add extra spaces.
		// for (int i = 0; i <= numGridX - 1; i = i + numGridX - 1) {
		// for (int j = 0; j <= numGridY - 1; j = j + numGridY - 1) {
		// Grid seed0 = grids[i][j];
		// Envelope zeroGridBoard = expandFromBoard(seed0);
		// tree0.addRectangle(c, zeroGridBoard);
		//
		// }
		// }

		ArrayList<Envelope> clusterRegion = partition();
		return clusterRegion;
	}

	/**
	 * Partition the whole region into rectangle. Reserve the dense regions, delete the 0 regions.
	 * </p> Use the following data:
	 * </p> tree0
	 * </p> treeDense
	 * </p> grids
	 * </p> boardEnvelope, numGridX, numGridY, granularityX, granularityY
	 * 
	 * @return in longitude and latitude
	 */
	private ArrayList<Envelope> partition() {
		// FIXME how !!!

		// TODO delete 0 regions
		return null;
	}

	private Envelope expandFromBoard(Grid seed) {

		double x1Board = seed.x;
		double x2Board = seed.x;
		double y1Board = seed.y;
		double y2Board = seed.y;

		// right
		Queue<Grid> q = new LinkedList<Grid>();
		if (seed.flag == Flag.UNVISITED) {
			// TODO
		}
		while (!q.isEmpty()) {
			Grid g = q.poll();
			ArrayList<Grid> udlrList = upDownLeftRight(g);
			for (int i = 0; i < udlrList.size(); i++) {
				Grid neighbor = udlrList.get(i);
				if (neighbor.flag == Flag.UNVISITED) {
					if (neighbor.density == 0) {
						neighbor.setFlag(Flag.ZERO);
						q.add(neighbor);

						switch (i) {
						case 0: // the left one
							x1Board--;
							break;
						case 1: // the right one
							x2Board++;
							break;
						case 2:
							// the up one
							y1Board--;
							break;
						case 3:// the down one
							y2Board++;
							break;
						}

					} else {
						neighbor.setFlag(Flag.BORDER);
					}
				}
			}
		}

		// FIXME find the maximum rectangle containing all 0 inside [x1Board, x2Board] * [y1Board, y2Board].
		Envelope envelope = new Envelope();
		return envelope;
	}

	/**
	 * Find a dense region from the center point
	 * 
	 * @param centerGrid
	 * @param alpha
	 * @return a clustered envelope
	 */
	private ArrayList<Envelope> expandFromMiddle(Grid seed, double alpha) {
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
					double similarity = Math.abs((neighbor.density - seed.density) / seed.density);
					if (similarity <= alpha) {
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
	
	private ArrayList<Envelope> densityEnvelopes(ArrayList<Grid> borderGrid, double xLeft, double xRight, double yLeft, double yRight){
		ArrayList<Envelope> list = new ArrayList<Envelope>();
		// FIXME yanhui here
		
		return list;
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
	 * Find the most dense grid, eliminate the dense regions already found by previous steps.
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

	public int getNumGrid() {
		return numGrid;
	}

	public void setNumGrid(int numGrid) {
		this.numGrid = numGrid;
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

	public Grid(Grid anotherGrid) {
		this.x = anotherGrid.x;
		this.y = anotherGrid.y;
		this.density = anotherGrid.density;
	}

}
