package commoninterface.utils;

import commoninterface.mathutils.Vector2d;

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
	
	public static Vector2d intersectLines(Vector2d p1, Vector2d p2, Vector2d p3, Vector2d p4) {
		double x1 = p1.x, x2 = p2.x, x3 = p3.x, x4 = p4.x;
		double y1 = p1.y, y2 = p2.y, y3 = p3.y, y4 = p4.y;
		double d = (y4-y3)*(x2-x1)-(x4-x3)*(y2-y1);
		
		if(d==0)// lines are parallel
			return null;
		
		double ua = ((x4-x3)*(y1-y3)-(y4-y3)*(x1-x3)) / d;
		double ub = ((x2-x1)*(y1-y3)-(y2-y1)*(x1-x3)) / d;
		
		if(ua >= 0 && ua <= 1 && ub >=0 && ub <=1)//point inside both lines
			return new Vector2d((x1 + ua*(x2 - x1)),(y1 + ua*(y2 - y1)));
		return null;
	}
	
	public static double modPI2(double angle) {
		while (angle < -Math.PI)
			angle += 2.0 * Math.PI;

		while (angle > Math.PI)
			angle -= 2.0 * Math.PI;

		return angle;
	}
}
