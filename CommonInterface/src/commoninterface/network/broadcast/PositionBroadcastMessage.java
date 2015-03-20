package commoninterface.network.broadcast;

import objects.DroneLocation;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.jcoord.LatLon;

public class PositionBroadcastMessage extends BroadcastMessage {

	public static final String IDENTIFIER = "GPS";
	private static final int UPDATE_TIME = 1*1000; //1 sec
	private AquaticDroneCI drone;
	
	public PositionBroadcastMessage(AquaticDroneCI drone) {
		super(drone, UPDATE_TIME, IDENTIFIER);
		this.drone = drone;
	}
	
	@Override
	public String getMessage() {
		
		LatLon latLon = drone.getGPSLatLon();
		
		if(latLon != null) {
			double orientation = drone.getCompassOrientationInDegrees();
			return latLon.getLat()+MESSAGE_SEPARATOR+latLon.getLon()+MESSAGE_SEPARATOR+orientation;
		}
		return null;
	}
	
	public static DroneLocation decode(String address, String message) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		
		if(split.length == 4) {
			return new DroneLocation(
				Double.parseDouble(split[1]),
				Double.parseDouble(split[2]),
				Double.parseDouble(split[3]),
				address
			);
		}
		return null;
	}

}