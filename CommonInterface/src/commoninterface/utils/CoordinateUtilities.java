package commoninterface.utils;

import objects.Waypoint;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

public class CoordinateUtilities {

	/**
	 * The reference coordinate is located in Lisbon, Portugal. I'm not sure what's 
	 * the consequence if the reference coordinate is in a different UTM zone!
	 * 
	 * @miguelduarte42
	 */
	private final static UTMRef REFERENCE_UTM = new LatLng(38.765078, -9.093461).toUTMRef();
	
	public static double[] GPSToCartesian(double lat, double lon) {
		
		double[] result = {0,0};
		
		LatLng coordinate = new LatLng(lat,lon);
		UTMRef utmCoordinate = coordinate.toUTMRef();
		result[0] = utmCoordinate.getEasting() - REFERENCE_UTM.getEasting();
		result[1] = utmCoordinate.getNorthing() - REFERENCE_UTM.getNorthing();
		
		return result;
	}
	
	/**
	 * This method calculates the GPS coordinate in a cartesian coordinate system
	 * with the origin at a specific reference GPS coordinate (x=0,y=0).
	 * 
	 * Not entirely sure if this is accurate for all situations.
	 * If the reference coordinate and the new coordinate are in different
	 * UTM zones, for instance, it might be a problem.
	 * 
	 * @miguelduarte42
	 */
	public static double[] cartesianToGPS(double x, double y) {
		
		double[] result = {0,0};
		
		UTMRef resultUTM = new UTMRef(
				REFERENCE_UTM.getEasting()+x,
				REFERENCE_UTM.getNorthing()+y,
				REFERENCE_UTM.getLatZone(),
				REFERENCE_UTM.getLngZone());
		
		LatLng resultGPS = resultUTM.toLatLng();
		
		result[0] = resultGPS.getLat();
		result[1] = resultGPS.getLng();
		
		return result;
	}
	
	public static double angleInDegrees(double lat1, double lon1, double lat2, double lon2) {
		
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		lon1 = Math.toRadians(lon1);
		lon2 = Math.toRadians(lon2);
		
		double result = Math.atan2(Math.sin(lon2-lon1)*Math.cos(lat2), Math.cos(lat1)*Math.sin(lat2)-Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1));
	
		return Math.toDegrees(result);
	}
	
	public static double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
		LatLng coord1 = new LatLng(lat1, lon1);
		LatLng coord2 = new LatLng(lat2, lon2);
		return coord1.distance(coord2)*1000;
	}
	
}