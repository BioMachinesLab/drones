package dataObjects;

import java.io.Serializable;

import objects.DroneLocation;

import org.joda.time.LocalDateTime;

import utils.Nmea0183ToDecimalConverter;

import commoninterface.network.NetworkUtils;

/**
 * Check http://aprs.gids.nl/nmea/ for more informations about the fields
 * 
 * @author Vasco Craveiro Costa
 * 
 */
public class GPSData implements Serializable {
	private static final long serialVersionUID = -2077018378793167250L;
	private final static int NUMBER_OF_PARAMETERS = 15;
	private long PRINT_NUMBER = 0;
	
	private String address;
	
	// Coordinates
	private double latitudeDecimal;
	private double longitudeDecimal;
	private String latitude;
	private String longitude;
	private double altitude;

	// GPS Fix
	private boolean fix;
	private int fixType; // 1=Not available, 2=2D, 3=3D
	private int numberOfSatellitesInView;
	private int numberOfSatellitesInUse;
	private double HDOP;
	private double PDOP;
	private double VDOP;
	private int GPSSourceType; // 0=no fix, 1=GPS fix, 2=Dif. GPS fix

	// Navigation Info
	private double groundSpeedKnts;
	private double groundSpeedKmh;
	private double orientation; // Compass info, real north, not magnetic one

	// Time Information
	private LocalDateTime date;

	// General Methods
	public GPSData() {
		address = NetworkUtils.getAddress();
		latitude = null;
		longitude = null;
		altitude = 0;

		fix = false;
		fixType = -1;
		numberOfSatellitesInView = -1;
		numberOfSatellitesInUse = -1;
		HDOP = -1;
		PDOP = -1;
		VDOP = -1;
		GPSSourceType = -1;

		groundSpeedKnts = -1;
		groundSpeedKmh = -1;
		orientation = -1;

		date = new LocalDateTime(1, 1, 1, 1, 1, 1, 1);
	}

	public int getNumberOfParameters() {
		return NUMBER_OF_PARAMETERS;
	}

	// Getters And Setters
	public String getLatitude() {
		return latitude;
	}
	
	public void setLatitude(String latitude) {
		
		this.latitude = latitude;
		
		double lat = Double.parseDouble(latitude.substring(0,latitude.length()-1));
		char latPos = latitude.charAt(latitude.length()-1);
		lat = Nmea0183ToDecimalConverter.convertLatitudeToDecimal(lat, latPos);
		
		this.latitudeDecimal = lat;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		
		this.longitude = longitude;
		
		double lon = Double.parseDouble(longitude.substring(0,longitude.length()-1));
		char lonPos = longitude.charAt(longitude.length()-1);
		
		lon = Nmea0183ToDecimalConverter.convertLongitudeToDecimal(lon, lonPos);		
		
		this.longitudeDecimal = lon;
	}
	
	public void setLatitudeDecimal(double lat) {
		this.latitudeDecimal = lat;
	}
	
	public void setLongitudeDecimal(double lon) {
		this.longitudeDecimal = lon;
	}
	
	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public boolean isFix() {
		return fix;
	}

	public void setFix(boolean hasFix) {
		this.fix = hasFix;
	}

	public int getFixType() {
		return fixType;
	}

	public void setFixType(int fixType) {
		this.fixType = fixType;
	}

	public int getNumberOfSatellitesInView() {
		return numberOfSatellitesInView;
	}

	public void setNumberOfSatellitesInView(int numberOfSatellitesInView) {
		this.numberOfSatellitesInView = numberOfSatellitesInView;
	}

	public int getNumberOfSatellitesInUse() {
		return numberOfSatellitesInUse;
	}

	public void setNumberOfSatellitesInUse(int numberOfSatellitesInUse) {
		this.numberOfSatellitesInUse = numberOfSatellitesInUse;
	}

	public double getHDOP() {
		return HDOP;
	}

	public void setHDOP(double hDOP) {
		HDOP = hDOP;
	}

	public double getPDOP() {
		return PDOP;
	}

	public void setPDOP(double pDOP) {
		PDOP = pDOP;
	}

	public double getVDOP() {
		return VDOP;
	}

	public void setVDOP(double vDOP) {
		VDOP = vDOP;
	}

	public int getGPSSourceType() {
		return GPSSourceType;
	}

	public void setGPSSourceType(int GPSSourceType) {
		this.GPSSourceType = GPSSourceType;
	}

	public double getGroundSpeedKnts() {
		return groundSpeedKnts;
	}

	public void setGroundSpeedKnts(double groundSpeedKnts) {
		this.groundSpeedKnts = groundSpeedKnts;
	}

	public double getGroundSpeedKmh() {
		return groundSpeedKmh;
	}

	public void setGroundSpeedKmh(double groundSpeedKmh) {
		this.groundSpeedKmh = groundSpeedKmh;
	}

	public double getOrientation() {
		return orientation;
	}

	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	public double getLatitudeDecimal() {
		return latitudeDecimal;
	}
	
	public double getLongitudeDecimal() {
		return longitudeDecimal;
	}

	@Override
	public String toString() {
		String str = "\n\n############# " + PRINT_NUMBER + " #############\n";
		str += "--- Coordinates ---\n";
		str += "Latitude: " + latitude + "\n";
		str += "Longitude: " + longitude + "\n";
		str += "Altitude: " + altitude + "\n";

		str += "\n--- Fix ---\n";
		str += "Has fix: " + fix + "\n";
		str += "Fix Type: " + fixType + "\n";
		str += "Satelittes in View: " + numberOfSatellitesInView + "\n";
		str += "Satelittes in Use: " + numberOfSatellitesInUse + "\n";
		str += "HDOP: " + HDOP + "\n";
		str += "PDOP: " + PDOP + "\n";
		str += "VDOP: " + VDOP + "\n";
		str += "GPS Source Type: " + GPSSourceType + "\n";
		str += "Time: " + date + "\n";

		str += "\n--- Navigation ---\n";
		str += "Ground Speed (Km/h): " + groundSpeedKmh + "\n";
		str += "Ground Speed (Knots): " + groundSpeedKnts + "\n";
		str += "Orientation: " + orientation + "\n";

		PRINT_NUMBER++;
		return str;
	}

	public DroneLocation toDroneLocation() {
		return new DroneLocation(latitudeDecimal, longitudeDecimal, orientation, address);
	}

	public String getDroneAddress() {
		return address;
	}
}
