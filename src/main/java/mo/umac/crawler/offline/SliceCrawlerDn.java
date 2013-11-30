/**
 * 
 */
package mo.umac.crawler.offline;

import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Implement the d-dimensional upper bound proof for n-dimensional space
 * 
 * @author kate
 *
 */
public class SliceCrawlerDn  extends OfflineStrategy {

	public static Logger logger = Logger.getLogger(SliceCrawlerDn.class.getName());

	public SliceCrawlerDn() {
		super();
		logger.info("------------SliceCrawlerDn------------");
	}
	
	@Override
	public void crawl(String state, int category, String query, Envelope envelopeState) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @param state
	 * @param category
	 * @param query
	 * @param dimension
	 * @param boundaries
	 */
	public void crawl(String state, int category, String query, int dimension, List boundaries) {
		
	}

}
