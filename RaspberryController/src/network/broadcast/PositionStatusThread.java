package network.broadcast;

import commoninterfaceimpl.RealAquaticDroneCI;
import dataObjects.GPSData;

public class PositionStatusThread extends BroadcastStatusThread {
	
	private static final int UPDATE_TIME = 1*1000; //1 sec
	private static final String IDENTIFIER = "GPS";
	private RealAquaticDroneCI drone;
	
	public PositionStatusThread(BroadcastHandler handler) {
		super(handler, IDENTIFIER, UPDATE_TIME);
		this.drone = handler.getDrone();
	}

	@Override
	public String createMessage() {
		
		double latitude = drone.getGPSLatitude();
		double longitude = drone.getGPSLongitude();
		double orientation = drone.getCompassOrientationInDegrees();
		
		return latitude+MESSAGE_SEPARATOR+longitude+MESSAGE_SEPARATOR+orientation;
	}
	
	public static GPSData decode(String address, String message) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		
		GPSData gpsData = new GPSData();
		gpsData.setDroneAddress(address);
		
		if(split.length == 4) {
			gpsData.setLatitudeDecimal(Double.parseDouble(split[1]));
			gpsData.setLongitudeDecimal(Double.parseDouble(split[2]));
			gpsData.setOrientation(Double.parseDouble(split[3]));
			
			return gpsData;
		}
		return null;
	}
}