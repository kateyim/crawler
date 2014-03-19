package mo.umac.crawler.online;

import mo.umac.metadata.yahoolocal.AQuery;

import com.vividsolutions.jts.geom.Coordinate;

public class OnlineYahooLocalQuery extends AQuery {

    private String appid;
    
    private int start;
    
    /**
     * TODO radius unit?
     */
    private double radius;

    public OnlineYahooLocalQuery(String query, int topK, String state,
	    int category, Coordinate point) {
	super(point, state, category, query, topK);

    }

    public OnlineYahooLocalQuery(String query, int topK, String state,
	    int category, Coordinate point, String appid, int start,
	    double radius) {
	super(point, state, category, query, topK);
	this.appid = appid;
	this.start = start;
	this.radius = radius;
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

    public double getRadius() {
	return radius;
    }

    public void setRadius(double radius) {
	this.radius = radius;
    }

}
