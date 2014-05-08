import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;

import utils.GeoOperator;


public class TestIO {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TriangulationPoint p1 = new TPoint(811.304352505001, 1.4330045448045564E-7);
		TriangulationPoint p2 = new TPoint(945.1515520802576,17.451301523924748);
//		TriangulationPoint q = new TPoint(945.1515523802576,17.451301323924746);
		TriangulationPoint q = new TPoint(847.2137489947179,4.6819485781309425);
		boolean b = GeoOperator.pointOnLineSegment(p1, p2, q);
		System.out.println(b);
	}
	


}
