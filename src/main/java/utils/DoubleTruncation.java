package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 
 * {@link http://www.cnblogs.com/nayitian/p/3214178.html}
 * 
 * @author Kate
 *
 */
public class DoubleTruncation {

	/**
     * The BigDecimal class provides operations for arithmetic, scale manipulation, rounding, comparison, hashing, and format conversion.
     * @param d
     * @return
     */
    public static double formatDouble(double d) {
        
        BigDecimal bg = new BigDecimal(d).setScale(10, RoundingMode.DOWN);
        
        return bg.doubleValue();
    }

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double d = 97.08901588721434;
		double newD = formatDouble(d);
		System.out.println(newD);
	}

}
