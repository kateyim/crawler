package mo.umac.db.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

/**
 * Just by accessing it with GeoTools it will be "spatially enabled". And any
 * tables you create through the GeoTools DataStore api will be spatially
 * indexed, etc...
 * 
 * {@link http://docs.geotools.org/latest/userguide/library/jdbc/h2.html}
 * 
 * @author kate
 * 
 */
public class GeotoolsH2 {

	/**
	 * reference a database file named “geotools” located in the current working
	 * directory
	 */
	private void creating1() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "h2");
		params.put("database", "geotools");

		try {
			DataStore datastore = DataStoreFinder.getDataStore(params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * create a connection to H2 in “embedded” mode. One limitation to this
	 * approach is that it only allows for a single java process to access the
	 * database at any one time.
	 */
	private void creating2() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "h2");
		params.put("database", "/abs/path/to/geotools");

		try {
			DataStore datastore = DataStoreFinder.getDataStore(params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * H2 also offers a server mode in which access to the underlying database
	 * is made via traditional client-server TCP connection
	 */
	private void creating3() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dbtype", "h2");
		params.put("host", "localhost");
		params.put("port", 9902);
		params.put("database", "geotools");
		params.put("passwd", "geotools");
		params.put("passwd", "geotools");

		try {
			DataStore datastore = DataStoreFinder.getDataStore(params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
