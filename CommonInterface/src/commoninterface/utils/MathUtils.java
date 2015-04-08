package commoninterface.utils;

public class MathUtils {
	/**
	 * Re-maps a number from one range to another. That is, a value of fromLow
	 * would get mapped to toLow, a value of fromHigh to toHigh, values
	 * in-between to values in-between, etc. Does not constrain values to within
	 * the range, because out-of-range values are sometimes intended and useful.
	 * The function also handles negative numbers well.
	 * 
	 * @param The value to be mapped on a new range
	 * @param Old minimum bound
	 * @param Old maximum bound
	 * @param New minimum bound
	 * @param New maximum bound
	 * @return The input value mapped on the new range
	 */
	public static double map(double x, double in_min, double in_max, double out_min,
			double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
}
