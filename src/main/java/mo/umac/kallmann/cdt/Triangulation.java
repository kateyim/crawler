package mo.umac.kallmann.cdt;

import java.util.ArrayList;
import java.util.HashMap;

import mo.umac.kallmann.cdt.LocateResult.LOC;

public class Triangulation {

	public static final int INIT_VALUE = -1;

	public int numVertices = 0;
	public int numEdges = 0;
	public int numFaces = 0;

	public HashMap<Integer, Vertice> verticeMap = new HashMap<Integer, Vertice>();
	public HashMap<Integer, Edge> edgeMap = new HashMap<Integer, Edge>();
	public HashMap<Integer, Face> faceMap = new HashMap<Integer, Face>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private void insertConstraint(Constraints c) {
		ArrayList<Vertice> verticeList = new ArrayList<Vertice>();
		for (int i = 0; i < c.verticeList.size(); i++) {
			Vertice p = verticeList.get(i);
			Vertice v;
			LocateResult lr = locatePoint(p);
			if (lr.location == LOC.VERTICE) {
				v = (Vertice) verticeMap.get(lr.elementID);
			} else if (lr.location == LOC.EDGE) {
				v = insertPointInEdge(lr.elementID, p);
			} else {
				v = insertPointInFace(lr.elementID, p);
			}
			numVertices++;
			verticeMap.put(numVertices, v);
			//
			verticeList.add(v);
		}
		for (int i = 0; i < verticeList.size() - 1; i++) {
			Vertice v1 = verticeList.get(i);
			Vertice v2 = verticeList.get(i + 1);
			insertSegment(v1, v2);
		}

	}

	private LocateResult locatePoint(Vertice p) {
		// TODO
		return null;
	}

	private Vertice insertPointInEdge(int edgeId, Vertice p) {
		Edge edge = edgeMap.get(edgeId);

		return null;
	}

	private Vertice insertPointInFace(int faceId, Vertice p) {
		Face face = faceMap.get(faceId);

		return null;
	}

	private void flipEdge() {

	}

	private void insertSegment(Vertice v1, Vertice v2) {

	}

}
