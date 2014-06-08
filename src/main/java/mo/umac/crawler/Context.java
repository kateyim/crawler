/**
 * 
 */
package mo.umac.crawler;

import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author kate
 * 
 */
public class Context {
	private Strategy crawlerStrategy;

	public Context(Strategy crawlerStrategy) {
		this.crawlerStrategy = crawlerStrategy;
	}

	public void callCrawling(LinkedList<String> listNameStates, List<String> listCategoryNames) {
		this.crawlerStrategy.callCrawling(listNameStates, listCategoryNames);
	}
	
	public void callCrawlingSingle(String state, int category, String query, Envelope envelope){
		this.crawlerStrategy.callCrawlingSingle(state, category, query, envelope);
	}
}
