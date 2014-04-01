import java.util.ArrayList;

import mo.umac.crawler.MainYahoo;
import mo.umac.uscensus.USDensity;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class DensityListTest {
	private static Logger logger = Logger.getLogger(USDensity.class.getName());

	private static boolean debug = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MainYahoo.shutdownLogs(debug);
		DOMConfigurator.configure(MainYahoo.LOG_PROPERTY_PATH);
		Envelope envelope = new Envelope(0.5, 3.3, 0.5, 3.3);
		double granularityX = 0.5;
		double granularityY = 0.5;
		ArrayList<Coordinate[]> roadList = new ArrayList<Coordinate[]>();
		// Coordinate[] c1 = { new Coordinate(0.6, 0.7), new Coordinate(0.8, 2.1) };
		// Coordinate[] c2 = { new Coordinate(1.2, 0.6), new Coordinate(2.2, 0.8), new Coordinate(1.9, 2.6) };
		// roadList.add(c1);
		// roadList.add(c2);
		Coordinate[] c3 = { new Coordinate(0.6, 0.7), new Coordinate(0.6, 2.1) };
		roadList.add(c3);
		USDensity.densityList(envelope, granularityX, granularityY, roadList);
	}

}
