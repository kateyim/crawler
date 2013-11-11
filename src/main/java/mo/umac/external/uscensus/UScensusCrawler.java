/**
 * 
 */
package mo.umac.external.uscensus;

/**
 * Crawl data from US census
 * 
 * @author kate
 * 
 */
public class UScensusCrawler {

	public static void crawlRoad() {
		String url = "http://www.census.gov/cgi-bin/geo/shapefiles2013/main";
		String folder = "../data-map/us-road/new-york/";
		crawler(url, folder);
	}

	/**
	 * @param url
	 * @param folder
	 */
	public static void crawler(String url, String folder) {
		// TODO 
	}

}
