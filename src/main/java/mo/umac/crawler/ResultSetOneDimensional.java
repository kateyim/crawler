/**
 * 
 */
package mo.umac.crawler;

import java.util.ArrayList;
import java.util.List;

import mo.umac.spatial.Circle;

import com.vividsolutions.jts.geom.LineSegment;

/**
 * @author kate
 */
public class ResultSetOneDimensional extends ResultSet {

	private LineSegment line;
	private List<APOI> leftPOIs = new ArrayList<APOI>();
	private List<APOI> rightPOIs = new ArrayList<APOI>();
	private List<APOI> onPOIs = new ArrayList<APOI>();

	private int numQueries;

	public void addAll(List oneList, List antherList) {
		oneList.addAll(antherList);
	}

	// I don't know why this isn't in Long...
	private static int compare(long a, long b) {
		return a < b ? -1 : a > b ? 1 : 0;
	}

	public int getNumQueries() {
		return numQueries;
	}

	public void setNumQueries(int numQueries) {
		this.numQueries = numQueries;
	}

	public List<APOI> getLeftPOIs() {
		return leftPOIs;
	}

	public void setLeftPOIs(List<APOI> leftPOIs) {
		this.leftPOIs = leftPOIs;
	}

	public List<APOI> getRightPOIs() {
		return rightPOIs;
	}

	public void setRightPOIs(List<APOI> rightPOIs) {
		this.rightPOIs = rightPOIs;
	}

	public List<APOI> getOnPOIs() {
		return onPOIs;
	}

	public void setOnPOIs(List<APOI> onLinePOI) {
		this.onPOIs = onLinePOI;
	}

	public LineSegment getLine() {
		return line;
	}

	public void setLine(LineSegment line) {
		this.line = line;
	}

}
