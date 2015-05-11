package commoninterface.utils;

import java.io.Serializable;

import commoninterface.mathutils.Vector2d;
import commoninterface.utils.jcoord.*;

public class CoordinateUtilities implements Serializable {

	/**
	 * The reference coordinate is located in Lisbon, Portugal. I'm not sure what's 
	 * the consequence if the reference coordinate is in a different UTM zone!
	 * 
	 * @miguelduarte42
	 */
	private final static UTMRef REFERENCE_UTM = new LatLon(38.756112, -9.153242).toUTMRef();
	
	public static Vector2d GPSToCartesian(LatLon coordinate) {
		
		Vector2d result = new Vector2d();
		
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
	 * @return a Vector2d with Lat in Y and Lon in X
	 * 
	 * @author miguelduarte42
	 */
	public static LatLon cartesianToGPS(Vector2d cartesian) {
		
		UTMRef resultUTM = new UTMRef(
				REFERENCE_UTM.getEasting()+cartesian.getX(),
				REFERENCE_UTM.getNorthing()+cartesian.getY(),
				REFERENCE_UTM.getLatZone(),
				REFERENCE_UTM.getLngZone());
		
		return resultUTM.toLatLng();
	}
	
	public static LatLon cartesianToGPS(double x, double y) {
		return cartesianToGPS(new Vector2d(x,y));
	}
	
	public static double angleInDegrees(LatLon latLon1, LatLon latLon2) {
		
		double lat1 = Math.toRadians(latLon1.getLat());
		double lat2 = Math.toRadians(latLon2.getLat());
		double lon1 = Math.toRadians(latLon1.getLon());
		double lon2 = Math.toRadians(latLon2.getLon());
		
		double result = Math.atan2(
				Math.sin(lon2-lon1)*Math.cos(lat2),
				Math.cos(lat1)*Math.sin(lat2)-Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)
			);
	
		return Math.toDegrees(result);
	}
	
	public static double distanceInMeters(LatLon coord1, LatLon coord2) {
		return coord1.distance(coord2)*1000;
	}
	
	public static void main(String[] args) {
		//Test cases
		LatLon o = new LatLon(38.765078, -9.093461);
		Vector2d oCart = GPSToCartesian(o);
		System.out.println(oCart.getX()+" "+oCart.getY());
		
		Vector2d tr = new Vector2d(10,10);
		Vector2d tl = new Vector2d(-10,10);
		Vector2d b = new Vector2d(0,-10);
		Vector2d r = new Vector2d(10,0);
		Vector2d l = new Vector2d(-10,0);
		
		System.out.println();
		System.out.println((cartesianToGPS(tl)));
		System.out.println((cartesianToGPS(tr)));
		System.out.println((cartesianToGPS(b)));
		System.out.println((cartesianToGPS(r)));
		System.out.println((cartesianToGPS(l)));
		System.out.println();
		System.out.println(GPSToCartesian(cartesianToGPS(tl)));
		System.out.println(GPSToCartesian(cartesianToGPS(tr)));
		System.out.println(GPSToCartesian(cartesianToGPS(b)));
		System.out.println(GPSToCartesian(cartesianToGPS(r)));
		System.out.println(GPSToCartesian(cartesianToGPS(l)));
		System.out.println();
		System.out.println(angleInDegrees(o, cartesianToGPS(tl)));
		System.out.println(angleInDegrees(o, cartesianToGPS(tr)));
		System.out.println(angleInDegrees(o, cartesianToGPS(b)));
		System.out.println(angleInDegrees(o, cartesianToGPS(r)));
		System.out.println(angleInDegrees(o, cartesianToGPS(l)));
		
	}
	
}