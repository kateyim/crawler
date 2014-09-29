package mo.umac.uscensus;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

public class DrawInJOSM {

	public static void main(String[] args) {
		
		String roadFile = "../data-map/ny-roads.lines";
		ArrayList<Coordinate[]> roadList = USDensity.readRoad(USDensity.UN_ZIP_FOLDER_PATH);

	}

}
