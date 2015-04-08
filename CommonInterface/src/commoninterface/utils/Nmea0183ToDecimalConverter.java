package commoninterface.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Nmea0183ToDecimalConverter {
	
	public static double convertLatitudeToDecimal(double lat, char latPos) {
		
		BigDecimal bd = new BigDecimal(lat);
		bd = bd.movePointLeft(2);

		BigDecimal degrees = getDegrees(lat);
		
		BigDecimal minutesAndSeconds = getMinutes(lat);
		
		BigDecimal decimal = degrees.add(minutesAndSeconds).setScale(5,
				RoundingMode.HALF_EVEN);

		lat =  decimal.doubleValue();
		
		if (lat > 0 && latPos == 'W' || latPos == 'S') {
			return lat * -1;
		} else {
			return lat;
		}
	}
	
	public static double convertLongitudeToDecimal(double lon, char lonPos) {
		
		BigDecimal bd = new BigDecimal(lon);
		bd = bd.movePointLeft(2);

		BigDecimal degrees = getDegrees(lon);
		
		BigDecimal minutesAndSeconds = getMinutes(lon);
		
		BigDecimal decimal = degrees.add(minutesAndSeconds).setScale(5,
				RoundingMode.HALF_EVEN);
		
		lon =  decimal.doubleValue();
		
		if (lon > 0 && lonPos == 'W' || lonPos == 'S') {
			return lon * -1;
		} else {
			return lon;
		}
	}


	private static BigDecimal getDegrees(double d) {
		BigDecimal bd = new BigDecimal(d);
		bd = bd.movePointLeft(2);

		return new BigDecimal(bd.intValue());
	}

	private static BigDecimal getMinutes(double d) {
		BigDecimal bd = new BigDecimal(d);
		bd = bd.movePointLeft(2);

		BigDecimal minutesBd = bd.subtract(new BigDecimal(bd.intValue()));
		minutesBd = minutesBd.movePointRight(2);

		BigDecimal minutes = new BigDecimal(
				(minutesBd.doubleValue() * 100) / 60).movePointLeft(2);

		return minutes;
	}

	public static void main(String[] args) {
		System.out.println(Nmea0183ToDecimalConverter.convertLatitudeToDecimal(3844.9374, 'N'));
		System.out.println(Nmea0183ToDecimalConverter.convertLongitudeToDecimal(00909.2119, 'W'));
	}
}
