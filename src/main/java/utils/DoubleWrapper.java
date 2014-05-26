package utils;

/**
 * {@link http://forums.whirlpool.net.au/archive/1361541}
 * 
 * @author kate
 */
public class DoubleWrapper {
	private double a;
	private double b;

	public DoubleWrapper(double a, double b) {
		this.a = a;
		this.b = b;
	}
	
	// public boolean equals(Object o) {
	// if (o instanceof DoubleWrapper) {
	// DoubleWrapper other = (DoubleWrapper) o;
	// return other.a == a && other.b == b;
	// }
	// return false;
	// }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof DoubleWrapper))
			return false;

		DoubleWrapper key = DoubleWrapper.class.cast(obj);

		if (a == key.a && b == key.b)
			return true;

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + (int) (Double.doubleToLongBits(a) ^ (Double.doubleToLongBits(a) >>> 32));
		hash = 79 * hash + (int) (Double.doubleToLongBits(b) ^ (Double.doubleToLongBits(b) >>> 32));
		return hash;
	}

	// public int hashCode() {
	// long aBits = Double.doubleToLongBits(a) >> 13;
	// long bBits = Double.doubleToLongBits(b);
	// return (int) (aBits ^ (aBits >>> 32) ^ bBits ^ (bBits >>> 32));
	// };

	// public int hashCode() {
	// long aBits = Double.doubleToLongBits(a) >> 13;
	// long bBits = Double.doubleToLongBits(b);
	// return (int) (aBits ^ (aBits >>> 32) ^ bBits ^ (bBits >>> 32));
	// };

	public static void main(String[] args) {
		double d1 = 12.365498712365;
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(d1)));
		long a1 = Double.doubleToLongBits(d1) >> 13;
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(a1)));
		System.out.println(a1);
	}
}
