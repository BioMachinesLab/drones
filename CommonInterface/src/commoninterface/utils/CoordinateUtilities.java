package commoninterface.utils;

import commoninterface.mathutils.Vector2d;

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
	
	public static Vector2d GPSToCartesian(double lat, double lon) {
		
		Vector2d result = new Vector2d();
		
		LatLng coordinate = new LatLng(lat,lon);
		UTMRef utmCoordinate = coordinate.toUTMRef();
		result.setX(utmCoordinate.getEasting() - REFERENCE_UTM.getEasting());
		result.setY(utmCoordinate.getNorthing() - REFERENCE_UTM.getNorthing());
		
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
	 * @return a Vector2d with Lat in X and Lon in Y (this might be
	 * counter-intuitive)
	 * 
	 * @author miguelduarte42
	 */
	public static Vector2d cartesianToGPS(double x, double y) {
		
		Vector2d result = new Vector2d();
		
		UTMRef resultUTM = new UTMRef(
				REFERENCE_UTM.getEasting()+x,
				REFERENCE_UTM.getNorthing()+y,
				REFERENCE_UTM.getLatZone(),
				REFERENCE_UTM.getLngZone());
		
		LatLng resultGPS = resultUTM.toLatLng();
		
		result.setX(resultGPS.getLat());
		result.setY(resultGPS.getLng());
		
		return result;
	}
	
	public static double angleInDegrees(Vector2d latLon1, Vector2d latLon2) {
		
		double lat1 = Math.toRadians(latLon1.getX());
		double lat2 = Math.toRadians(latLon2.getX());
		double lon1 = Math.toRadians(latLon1.getY());
		double lon2 = Math.toRadians(latLon2.getY());
		
		double result = Math.atan2(Math.sin(lon2-lon1)*Math.cos(lat2), Math.cos(lat1)*Math.sin(lat2)-Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1));
	
		return Math.toDegrees(result);
	}
	
	public static double distanceInMeters(Vector2d latLon1, Vector2d latLon2) {
		LatLng coord1 = new LatLng(latLon1.getX(), latLon1.getY());
		LatLng coord2 = new LatLng(latLon2.getX(), latLon2.getY());
		return coord1.distance(coord2)*1000;
	}
	
}