package mo.umac.external.uscensus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import mo.umac.external.uscensus.Grid.Flag;
import mo.umac.rtree.MyRTree;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Envelope;

public class DensityMap {

	protected static Logger logger = Logger.getLogger(DensityMap.class.getName());

	private double granularityX;

	private double granularityY;

	private int numGridX;

	private int numGridY;

	private int numGrid;

	private Envelope boardEnvelope;

	private Grid[][] grids;

	private static MyRTree treeTotal = new MyRTree();
	private static MyRTree treeDense = new MyRTree();
	private static MyRTree tree0 = new MyRTree();

	public DensityMap(double granularityX, double granularityY, Envelope boardEnvelope) {
		super();
		this.granularityX = granularityX;
		this.granularityY = granularityY;
		this.boardEnvelope = boardEnvelope;
	}

	public DensityMap(double granularityX, double granularityY, int numGrid, Envelope boardEnvelope, Grid[][] grids) {
		super();
		this.granularityX = granularityX;
		this.granularityY = granularityY;
		this.numGrid = numGrid;
		this.boardEnvelope = boardEnvelope;
		this.grids = grids;
	}

	public DensityMap(double granularityX, double granularityY, int numGridX, int numGridY, int numGrid, Envelope boardEnvelope, Grid[][] grids) {
		super();
		this.granularityX = granularityX;
		this.granularityY = granularityY;
		this.numGridX = numGridX;
		this.numGridY = numGridY;
		this.numGrid = numGrid;
		this.boardEnvelope = boardEnvelope;
		this.grids = grids;
	}

	/**
	 * combineDensityMap, first find the most dense region, begin to expand;
	 * </p>
	 * then find the most dense region in the remained area, begin to expand.
	 * </p>
	 * Iterate this process for 3-4 times.
	 * </p>
	 * At last partition the rest regions.
	 * 
	 * @param alpha
	 *            is the threshold for computing similarity
	 */
	public ArrayList<Envelope> cluster(double alpha, int numIteration) {
		boolean stop = false;
		int c = 0;

		ArrayList<Grid> sortedMap = clone(grids);
		sortDensityMap(sortedMap);

		while (!stop) {

			if (c < numIteration) {
				stop = true;
			}

			Grid seed = findTheDense(sortedMap);
			Envelope bigGridBoards = expandFromMiddle(seed, alpha);

			treeDense.addRectangle(c, bigGridBoards);
			c++;
		}
		// Dealing with the zero grids, delete zero grids from the 4 corners of the rectangle.
		// Because using a big rectangle to bound the whole region is reasonable but add extra spaces.
		for (int i = 0; i <= numGridX - 1; i = i + numGridX - 1) {
			for (int j = 0; j <= numGridY - 1; j = j + numGridY - 1) {
				Grid seed0 = grids[i][j];
				Envelope zeroGridBoard = expandFromBoard(seed0);
				tree0.addRectangle(c, zeroGridBoard);

			}
		}

		ArrayList<Envelope> clusterRegion = partition();
		return clusterRegion;
	}

	/**
	 * Directly delete tree0
	 * 
	 * @return in longitude and latitude
	 */
	private ArrayList<Envelope> partition() {
		// FIXME Auto-generated method stub
		return null;
	}

	private Envelope expandFromBoard(Grid seed) {

		double x1Board = seed.getxOrder();
		double x2Board = seed.getxOrder();
		double y1Board = seed.getyOrder();
		double y2Board = seed.getyOrder();

		// right
		Queue<Grid> q = new LinkedList<Grid>();
		if (seed.getFlag() == Flag.UNVISITED) {
			if (seed.getDensity() == 0) {
				seed.setFlag(Flag.ZERO);
				q.add(seed);
			}
		}
		while (!q.isEmpty()) {
			Grid g = q.poll();
			ArrayList<Grid> udlrList = upDownLeftRight(g);
			for (int i = 0; i < udlrList.size(); i++) {
				Grid neighbor = udlrList.get(i);
				if (neighbor.getFlag() == Flag.UNVISITED) {
					if (neighbor.getDensity() == 0) {
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
	 * @param centerGrid
	 * @param alpha
	 * @return the broader grids of the envelope
	 */
	private Envelope expandFromMiddle(Grid seed, double alpha) {
		double x1Board = seed.getxOrder();
		double x2Board = seed.getxOrder();
		double y1Board = seed.getyOrder();
		double y2Board = seed.getyOrder();

		ArrayList<Grid> borderGrid = new ArrayList<Grid>();

		Queue<Grid> queue = new LinkedList<Grid>();
		queue.add(seed);
		while (!queue.isEmpty()) {
			Grid oneGrid = queue.poll();
			ArrayList<Grid> udlrList = upDownLeftRight(oneGrid);
			for (int i = 0; i < udlrList.size(); i++) {
				Grid neighbor = udlrList.get(i);
				if (neighbor.getFlag() == Flag.UNVISITED) {
					if (neighbor.getDensity() == 0) {
						neighbor.setFlag(Flag.ZERO);
					} else {
						// TODO simple computation (change it)
						double similarity = Math.abs((neighbor.getDensity() - seed.getDensity()) / seed.getDensity());
						if (similarity > alpha) {
							neighbor.setFlag(Flag.VISITED);
							queue.add(neighbor);
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
							borderGrid.add(neighbor);
						}
					}
				}
			}
		}
		// TODO this rectangle is not accurate. change it later
		Envelope envelopeGrid = new Envelope(x1Board, x2Board, y1Board, y2Board);
		return envelopeGrid;
	}

	private ArrayList<Grid> upDownLeftRight(Grid seed) {
		// FIXME judge the board
		ArrayList<Grid> udlrList = new ArrayList<Grid>();
		int xOrder = seed.getxOrder();
		int yOrder = seed.getyOrder();
		for (int i = -1; i <= 1; i = i + 2) {
			int x = xOrder + i;
			Grid xGrid = grids[x][yOrder];
			udlrList.add(xGrid);
		}
		for (int i = -1; i <= 1; i = i + 2) {
			int y = yOrder + i;
			Grid yGrid = grids[xOrder][y];
			udlrList.add(yGrid);
		}
		return udlrList;
	}

	/**
	 * Partition the whole map by the dense regions.
	 * 
	 * @param zeroList
	 *            TODO
	 * 
	 * @return
	 */
	private ArrayList<Envelope> partition(ArrayList<Grid> zeroList) {
		// TODO

		// treeDense
		return null;
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
				Double oneDouble = new Double(one.getDensity());
				Double anotherDouble = new Double(another.getDensity());
				return oneDouble.compareTo(anotherDouble);
			}
		});

	}

	/**
	 * Find the most dense grid, eliminate the dense regions
	 * 
	 * @param sortedMap
	 * @param visitedList
	 *            TODO
	 * 
	 * @return
	 */
	private Grid findTheDense(ArrayList<Grid> sortedMap) {
		int size = sortedMap.size();
		for (int i = size - 1; i >= 0; i--) {
			Grid g = sortedMap.get(i);
			if (g.getFlag() == Flag.UNVISITED) {
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
