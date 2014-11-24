package objects;

import java.io.Serializable;

public class Waypoint implements Serializable{
	
	private double latitude;
	private double longitude;
	private double orientation;
	private String name = "";
	
	public Waypoint(double lat, double lon) {
		this.latitude = lat;
		this.longitude = lon;
	}
	
	public Waypoint(double lat, double lon, double orientation) {
		this(lat,lon);
		this.orientation = orientation;
	}
	
	public Waypoint(double lat, double lon, double orientation, String name) {
		this(lat, lon, orientation);
		this.name = name;
	}
	
	public double getLatitude(){
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public double getOrientation() {
		return orientation;
	}
	
	public String getName() {
		return name;
	}
}