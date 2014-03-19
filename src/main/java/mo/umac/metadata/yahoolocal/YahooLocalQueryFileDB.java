package mo.umac.metadata.yahoolocal;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.sql.Connection;

import mo.umac.spatial.Circle;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author Kate Yim
 * 
 */
public class YahooLocalQueryFileDB extends AQuery {
    private String subFolder;
    private String queryFile;
    private String resultsFile;
    private BufferedWriter queryOutput;
    private BufferedWriter resultsOutput;
    // add at 2013-06-05
    private Connection con;
    private Envelope envelopeState;
    private String appid;
    private int start;
    private Circle circle;
    private int numQueries;
    private String query;
    private int zip;
    private int results;
    private String state;
    private int category;

    public YahooLocalQueryFileDB(String subFolder, String queryFile,
	    BufferedWriter queryOutput, String resultsFile,
	    BufferedWriter resultsOutput, Envelope envelopeState, String appid,
	    String state, int category, int start, Circle circle,
	    int numQueries, String query, int zip, int results) {
	super();
	this.subFolder = subFolder;
	this.queryFile = queryFile;
	this.queryOutput = queryOutput;
	this.resultsFile = resultsFile;
	this.resultsOutput = resultsOutput;
	this.envelopeState = envelopeState;
	this.appid = appid;
	this.state = state;
	this.category = category;
	this.start = start;
	this.circle = circle;
	this.numQueries = numQueries;
	this.query = query;
	this.zip = zip;
	this.results = results;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("appid=");
	sb.append(appid);
	sb.append("&query=");
	sb.append(query);
	if (zip > 0) {
	    sb.append("&zip=");
	    sb.append(zip);
	}
	sb.append("&state=");
	sb.append(state);
	if (category > 0) {
	    sb.append("&category=");
	    sb.append(category);
	}
	sb.append("&results=");
	sb.append(results);
	sb.append("&start=");
	sb.append(start);
	sb.append("&latitude=");
	sb.append(circle.getCenter().y);
	sb.append("&longitude=");
	sb.append(circle.getCenter().x);
	sb.append("&radius=");
	sb.append(circle.getRadius());
	return sb.toString();
    }

    /**
     * @return
     */
    public String queryInfo() {
	StringBuffer sb = new StringBuffer();
	// double latitude = circle.getCenter().y;
	// double longitude = circle.getCenter().x;
	// double radius = circle.getRadius();
	String latitude = new BigDecimal(circle.getCenter().y).toPlainString();
	String longitude = new BigDecimal(circle.getCenter().x).toPlainString();
	String radius = new BigDecimal(circle.getRadius()).toPlainString();
	sb.append(query);
	sb.append(";");
	sb.append(Integer.toString(zip));
	sb.append(";");
	sb.append(Integer.toString(results));
	sb.append(";");
	sb.append(Integer.toString(start));
	sb.append(";");
	sb.append(latitude);
	sb.append(";");
	sb.append(longitude);
	sb.append(";");
	sb.append(radius);
	return sb.toString();
    }

    /**
     * Construct the query url according to the Yahoo Local API {@link http
     * ://developer.yahoo.com/search/local/V3/localSearch.html}.
     * 
     * @param appid
     *            Yahoo application id
     * @param query
     *            A keyword to search
     * @param zip
     * @param numOfResults
     * @param start
     *            The starting result position to return (1-based).
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     */
    public String toUrl() {
	StringBuffer sb = new StringBuffer();
	String head = "http://local.yahooapis.com/LocalSearchService/V3/localSearch?";
	sb.append(head);
	sb.append("appid=");
	sb.append(appid);
	sb.append("&query=");
	sb.append(query);
	if (zip > 0) {
	    sb.append("&zip=");
	    sb.append(zip);
	}
	sb.append("&state=");
	sb.append(state);
	if (category > 0) {
	    sb.append("&category=");
	    sb.append(category);
	}
	sb.append("&results=");
	sb.append(results);
	sb.append("&start=");
	sb.append(start);
	sb.append("&latitude=");
	String latitude = new BigDecimal(circle.getCenter().y).toPlainString();
	sb.append(latitude);
	sb.append("&longitude=");
	String longitude = new BigDecimal(circle.getCenter().x).toPlainString();
	sb.append(longitude);
	sb.append("&radius=");
	String radius = new BigDecimal(circle.getRadius()).toPlainString();
	sb.append(radius);
	return sb.toString();
    }

    public int getZip() {
	return zip;
    }

    public void setZip(int zip) {
	this.zip = zip;
    }

    public String getSubFolder() {
	return subFolder;
    }

    public void setSubFolder(String subFolder) {
	this.subFolder = subFolder;
    }

    public Envelope getEnvelopeState() {
	return envelopeState;
    }

    public void setEnvelopeState(Envelope envelopeState) {
	this.envelopeState = envelopeState;
    }

    public String getAppid() {
	return appid;
    }

    public void setAppid(String appid) {
	this.appid = appid;
    }

    public int getStart() {
	return start;
    }

    public void setStart(int start) {
	this.start = start;
    }

    public Circle getCircle() {
	return circle;
    }

    public void setCircle(Circle circle) {
	this.circle = circle;
    }

    public int getNumQueries() {
	return numQueries;
    }

    public void setNumQueries(int numQueries) {
	this.numQueries = numQueries;
    }

    public String getQuery() {
	return query;
    }

    public void setQuery(String query) {
	this.query = query;
    }

    public int getResults() {
	return results;
    }

    public void setResults(int results) {
	this.results = results;
    }

    public String getQueryFile() {
	return queryFile;
    }

    public void setQueryFile(String queryFile) {
	this.queryFile = queryFile;
    }

    public String getResultsFile() {
	return resultsFile;
    }

    public void setResultsFile(String resultsFile) {
	this.resultsFile = resultsFile;
    }

    public BufferedWriter getQueryOutput() {
	return queryOutput;
    }

    public void setQueryOutput(BufferedWriter queryOutput) {
	this.queryOutput = queryOutput;
    }

    public BufferedWriter getResultsOutput() {
	return resultsOutput;
    }

    public void setResultsOutput(BufferedWriter resultsOutput) {
	this.resultsOutput = resultsOutput;
    }

    public String getState() {
	return state;
    }

    public void setState(String stateName) {
	this.state = stateName;
    }

    public int getCategory() {
	return category;
    }

    public void setCategory(int category) {
	this.category = category;
    }

    public Connection getCon() {
	return con;
    }

    public void setCon(Connection con) {
	this.con = con;
    }

}
