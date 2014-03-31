package mo.umac.analytics;

import mo.umac.crawler.Main;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Envelope;

public class Experiment {

	public static Logger logger = Logger.getLogger(Experiment.class.getName());

	public static String LOG_PROPERTY_PATH = "./log4j.xml";
	public static boolean debug = false;

	// used in offline algorithm
	public final static String DB_NAME_SOURCE = "../crawler-data/yahoolocal-h2/source/ny-prun";
	public final static String DB_NAME_TARGET = "../crawler-data/yahoolocal-h2/target/ny-prun-c-one";
	public final static String DB_NAME_CRAWL = "../crawler-data/yahoolocal-h2/crawl/datasets";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void generateDataUniform(String plainFileName, int n, Envelope envelope) {

	}

	public void generateDataSkew(String plainFileName, int n, Envelope envelope) {

	}

	/**
	 * Wrap data into the format which can be parsed by the crawler algorithm
	 * </p> write into .h2 file
	 */
	public void wrappedData(String plainFileName, String h2DBName) {

	}
	
	public void calling(String h2DBName, int k){
		
	}

}
