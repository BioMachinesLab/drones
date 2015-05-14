package commoninterface.network.broadcast;

import java.util.LinkedList;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.objects.Entity;
import commoninterface.objects.RobotLocation;
import commoninterface.objects.SharedDroneLocation;
import commoninterface.utils.jcoord.LatLon;

public class SharedDroneBroadcastMessage extends BroadcastMessage {

	public static final String IDENTIFIER = "SH_DRONE";
	private static final int UPDATE_TIME = 1*1000; //1 sec
	private SharedDroneLocation currentLocation = null;
	private RobotCI robot;
	
	public SharedDroneBroadcastMessage(RobotCI robot) {
		super(UPDATE_TIME, IDENTIFIER);
		this.robot = robot;
	}
	
	@Override
	public String getMessage() {
		
		LatLon latLon = currentLocation.getLatLon();
		
		if(latLon != null) {
			return currentLocation.getName()+MESSAGE_SEPARATOR+
					currentLocation.getObserverAddress()+MESSAGE_SEPARATOR+
					latLon.getLat()+MESSAGE_SEPARATOR+
					latLon.getLon()+MESSAGE_SEPARATOR+
					currentLocation.getDroneType().name();
		}
		return null;
	}
	
	public static SharedDroneLocation decode(String address, String message) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		
		if(split.length == 6) {
			return new SharedDroneLocation(
				split[1],
				split[2],
				new LatLon(
						Double.parseDouble(split[3]),
						Double.parseDouble(split[4])
				),
				AquaticDroneCI.DroneType.valueOf(split[5])
			);
		}
		return null;
	}
	
	@Override
	public String[] encode() {
		
		LinkedList<String> tempMessages = new LinkedList<String>();
		
		
		for(Entity e : robot.getEntities()) {
			if(e instanceof SharedDroneLocation) {
				SharedDroneLocation loc = (SharedDroneLocation)e;
				if(loc.getDroneType() == DroneType.ENEMY && robot.getNetworkAddress().equals(loc.getObserverAddress())) {
					currentLocation = loc;
					String msg = getMessage();
					if(msg != null)
						tempMessages.add(identifier+MESSAGE_SEPARATOR+msg);
				}
			}
		}
		
		if(tempMessages.isEmpty())
			return null;
		
		String[] messages = new String[tempMessages.size()];
		
		for(int i = 0 ; i < messages.length ; i++)
			messages[i] = tempMessages.get(i);
		
		return messages;
	}

}
