package mo.umac.analytics;

import java.util.LinkedList;

import mo.umac.uscensus.UScensusData;

import com.vividsolutions.jts.geom.Envelope;

public class StateMBR {

	public static void main(String[] args) {
		StateMBR test = new StateMBR();
		String state = "OK";
		Envelope mbr = test.getStateMBR(state);

		System.out.println(state + ": " + mbr.toString());

	}

	public Envelope getStateMBR(String specifiedName) {
		UScensusData.STATE_SHP_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.shp";
		UScensusData.STATE_DBF_FILE_NAME = "./src/main/resources/UScensus/tl_2012_us_state/tl_2012_us_state.dbf";

		LinkedList<Envelope> allEnvelopeStates = (LinkedList<Envelope>) UScensusData.MBR(UScensusData.STATE_SHP_FILE_NAME);
		LinkedList<String> allNameStates = (LinkedList<String>) UScensusData.stateName(UScensusData.STATE_DBF_FILE_NAME);

		for (int j = 0; j < allNameStates.size(); j++) {
			String name = allNameStates.get(j);
			if (name.equals(specifiedName)) {
				return allEnvelopeStates.get(j);
			}
		}

		return null;
	}

}
