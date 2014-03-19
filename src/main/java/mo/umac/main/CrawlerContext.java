/**
 * 
 */
package mo.umac.main;

import java.util.LinkedList;
import java.util.List;

/**
 * @author kate
 * 
 */
public class CrawlerContext {
	private CrawlerStrategy crawlerStrategy;

	public CrawlerContext(CrawlerStrategy crawlerStrategy) {
		this.crawlerStrategy = crawlerStrategy;
	}

	public void callCrawling(LinkedList<String> listNameStates, List<String> listCategoryNames) {
		this.crawlerStrategy.callCrawling(listNameStates, listCategoryNames);
	}
}
