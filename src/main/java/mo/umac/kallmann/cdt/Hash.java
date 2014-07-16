package mo.umac.kallmann.cdt;

public class Hash {
	/**
	   * Returns an integer hash code representing the given double array value.
	   * 
	   * @param value the value to be hashed (may be null)
	   * @return the hash code
	   * @since 1.2
	   */
	  public static int hash(double[] value) {
	      if (value == null) {
	          return 0;
	      }
	      int result = value.length;
	      for (int i = 0; i < value.length; ++i) {
	          result = result * 31 + hash(value[i]);
	      }
	      return result;
	  }
	  /**
	   * Returns an integer hash code representing the given double value.
	   * 
	   * @param value the value to be hashed
	   * @return the hash code
	   */
	  public static int hash(double value) {
	      long bits = Double.doubleToLongBits(value);
	      return (int)(bits ^ (bits >>> 32));
	  }
}
