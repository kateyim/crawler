import java.util.ArrayList;

import com.vividsolutions.jts.geom.Envelope;

import mo.umac.uscensus.USDensity;


public class ParitionTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void testReadPartOfDensity(){
		Envelope wholeEnvelope;
		Envelope partEnvelope;
		ArrayList<double[]> densityAll;
		USDensity.readPartOfDensity(densityAll, wholeEnvelope, partEnvelope);
		
		
	}

}
