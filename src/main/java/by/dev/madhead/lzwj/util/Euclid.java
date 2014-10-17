package by.dev.madhead.lzwj.util;

/**
 * Auxiliary class for computing greatest common delimiters and least common
 * multiples.
 * 
 * @author madhead
 * 
 */
public class Euclid {
	/**
	 * Computes greatest common delimiter for <code>a</code> and <code>b</code>.
	 * 
	 * @param a
	 * @param b
	 * @return greatest common delimiter for <code>a</code> and <code>b</code>.
	 */
	public static long GCD(long a, long b) {
		long tmp;

		while (b != 0) {
			tmp = b;
			b = a % b;
			a = tmp;
		}
		return Math.abs(a);
	}

	/**
	 * Computes least common multiple for <code>a</code> and <code>b</code>.
	 * 
	 * @param a
	 * @param b
	 * @return least common multiple for <code>a</code> and <code>b</code>.
	 */
	public static long LCM(long a, long b) {
		return Math.abs((a * b) / GCD(a, b));
	}
}
