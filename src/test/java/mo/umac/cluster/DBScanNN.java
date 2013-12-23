/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mo.umac.cluster;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import mo.umac.rtree.MyRTree;

import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * @author yyy
 */
public class DBScanNN {

	class Cluster {
		private int id;
		private Envelope envelope;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Envelope getEnvelope() {
			return envelope;
		}

		public void setEnvelope(Envelope envelope) {
			this.envelope = envelope;
		}
	}

	public static final int unvisited = -1;// unvisited
	public static final int visited = -2;// visited
	public static final int noisy = -3;// noisy
	// if the state does not equal to these tree values, then the point must be assigned to a cluster, and the state indicate the id of the cluster

	// TODO ? 2 dimensional grids - > 1-d representation?
	private Vector[] grids;
	private int[] states;

	private double epsilon;
	private int minPts;
	private List<Cluster> clusters;
	private int clusterID;

	private MyRTree rTree;

	// private RSTree rTree;

	// private List<Integer> noisyPoints = null;
	// private TreeSet noisyPoints;

	public DBScanNN(Vector[] grids, double epsi, int min) {
		this.grids = grids;
		this.states = new int[grids.length];

		for (int i = 0; i < this.grids.length; i++) {
			this.states[i] = DBScanNN.unvisited;// set every point to be unsupervised
		}

		this.epsilon = epsi;
		this.minPts = min;
		this.clusters = new ArrayList<Cluster>();

		this.clusterID = 0;

		// this.rTree = new RSTree(this.grids);
		// this.noisyPoints = new TreeSet();
	}

	public DBScanNN(Vector[] pois, double epsi) {
		this.grids = pois;
		this.states = new int[grids.length];

		for (int i = 0; i < this.grids.length; i++) {
			this.states[i] = DBScanNN.unvisited;// set every point to be unsupervised

		}

		this.epsilon = epsi;
		this.minPts = (int) (Math.log10(grids.length) / Math.log10(2));
		this.clusters = new ArrayList<Cluster>();

		this.clusterID = 0;

		this.rTree = new MyRTree(this.grids);
		// this.noisyPoints = new TreeSet();
	}

	public void clustering() {
		TreeSet neighbors = null;
		DBScanNNCluster tmpCluster = null;
		for (int i = 0; i < this.grids.length; i++) {

			if (this.states[i] == DBScanNN.unvisited) {
				this.states[i] = DBScanNN.visited;

				neighbors = findNeighbors(i);

				if (neighbors.size() < this.minPts) {
					this.states[i] = DBScanNN.noisy;
					// this.noisyPoints.add(i);
				} else {
					tmpCluster = new DBScanNNCluster(clusterID);
					this.clusterID++;
					this.clusters.add(tmpCluster);

					this.expandCluster(i, neighbors, tmpCluster);
				}
			}

			if (i % 10000 == 0) {
				System.out.println("Finish processing points " + i);
			}

		}

		System.out.println("Finally there are " + this.clusters.size() + " clusters!");
	}

	public TreeSet findNeighbors(int pointID) {

		TreeSet set = new TreeSet();

		List rList = this.rTree.rangeSearch(this.grids[pointID], (float) this.epsilon);

		int tmpID;
		for (int i = 0, size = rList.size(); i < size; i++) {
			tmpID = Integer.parseInt(String.valueOf(rList.get(i)));

			if (tmpID != pointID && this.states[tmpID] < 0) {
				set.add(tmpID);
			}
		}

		return set;

	}

	public void expandClusterByRatio(int pointID, TreeSet neighbors, DBScanNNCluster cluster) {

		TreeSet nNeighbors = null;
		this.insertInto(pointID, cluster);

		int tmpID;
		while (!neighbors.isEmpty()) {
			tmpID = Integer.parseInt(String.valueOf(neighbors.pollFirst()));

			if (this.states[tmpID] == DBScanNN.unvisited) {
				this.states[tmpID] = DBScanNN.visited;

				nNeighbors = this.findNeighbors(tmpID);

				if (nNeighbors.size() >= this.minPts) {
					neighbors.addAll(nNeighbors);
				}
			}

			// it means this point does not belong to any cluster
			// so noisy point may belongs to new cluster
			if (this.states[tmpID] < 0) {
				this.insertInto(tmpID, cluster);
			}
		}
	}

	public void expandCluster(int pointID, TreeSet neighbors, DBScanNNCluster cluster) {

		TreeSet nNeighbors = null;
		this.insertInto(pointID, cluster);

		int tmpID;
		while (!neighbors.isEmpty()) {
			tmpID = Integer.parseInt(String.valueOf(neighbors.pollFirst()));

			if (this.states[tmpID] == DBScanNN.unvisited) {
				this.states[tmpID] = DBScanNN.visited;

				nNeighbors = this.findNeighbors(tmpID);

				if (nNeighbors.size() >= this.minPts) {
					neighbors.addAll(nNeighbors);
				}
			}

			// it means this point does not belong to any cluster
			// so noisy point may belongs to new cluster
			if (this.states[tmpID] < 0) {
				this.insertInto(tmpID, cluster);
			}
		}
	}

	public List<Vector>[] getMapDatas() {
		List<Vector>[] datas = new List[this.clusters.size()];

		for (int i = 0; i < datas.length; i++) {
			datas[i] = this.getMapData(clusters.get(i).getIds());
		}

		return datas;
	}

	public List<Vector> getMapData(TIntArrayList ids) {
		List<Vector> mapData = new ArrayList<Vector>();
		for (int i = 0, size = ids.size(); i < size; i++) {
			mapData.add(this.grids[Integer.valueOf(String.valueOf(ids.get(i)))]);
		}

		return mapData;
	}

	public void insertInto(int pointID, DBScanNNCluster cluster) {
		cluster.addPoint(pointID);
		this.states[pointID] = cluster.getId();
	}

	public int getClusterID() {
		return clusterID;
	}

	public void setClusterID(int clusterID) {
		this.clusterID = clusterID;
	}

	public List<Cluster> getClusters() {
		return clusters;
	}

	public void setClusters(List<Cluster> clusters) {
		this.clusters = clusters;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public int getMinNum() {
		return minPts;
	}

	public void setMinNum(int minNum) {
		this.minPts = minNum;
	}

	public Vector[] getPoints() {
		return grids;
	}

	public void setPoints(Vector[] points) {
		this.grids = points;
	}

	public MyRTree getrTree() {
		return rTree;
	}

	public void setrTree(MyRTree rTree) {
		this.rTree = rTree;
	}

	public int[] getStates() {
		return states;
	}

	public void setStates(int[] states) {
		this.states = states;
	}

	public int getNumNoisyPoints() {

		int count = 0;

		for (int i = 0; i < this.states.length; i++) {
			if (this.states[i] == DBScanNN.noisy) {
				count++;
			}
		}

		return count;
	}

}
