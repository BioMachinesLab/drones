package commoninterface.network.broadcast;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.RobotLocation;
import commoninterface.utils.jcoord.LatLon;

public class PositionBroadcastMessage extends BroadcastMessage {

	public static final String IDENTIFIER = "GPS";
	private static final int UPDATE_TIME = 1*1000; //1 sec
	private AquaticDroneCI drone;
	
	public PositionBroadcastMessage(RobotCI robot) {
		super(UPDATE_TIME, IDENTIFIER);
		this.drone = (AquaticDroneCI) robot;
	}
	
	@Override
	public String getMessage() {
		
		LatLon latLon = drone.getGPSLatLon();
		
		if(latLon != null) {
			double orientation = drone.getCompassOrientationInDegrees();
			return drone.getNetworkAddress()+MESSAGE_SEPARATOR+
					latLon.getLat()+MESSAGE_SEPARATOR+
					latLon.getLon()+MESSAGE_SEPARATOR+
					orientation+MESSAGE_SEPARATOR+
					drone.getDroneType().name();
		}
		return null;
	}
	
	public static RobotLocation decode(String message) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		
		if(split.length == 6) {
			return new RobotLocation(
				split[1],
				new LatLon(
						Double.parseDouble(split[2]),
						Double.parseDouble(split[3])
				),
				Double.parseDouble(split[4]),
				AquaticDroneCI.DroneType.valueOf(split[5])
			);
		}
		return null;
	}

}
