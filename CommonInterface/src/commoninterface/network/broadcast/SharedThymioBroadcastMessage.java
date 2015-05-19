package commoninterface.network.broadcast;

import java.util.LinkedList;

import commoninterface.RobotCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.Entity;
import commoninterface.entities.ThymioSharedEntity;
import commoninterface.mathutils.Vector2d;


public class SharedThymioBroadcastMessage extends BroadcastMessage {

	public static final String IDENTIFIER = "THYMIO_SHARED_INFORMATION";
	private static final int UPDATE_TIME = 1*1000; //1 sec
	private ThymioSharedEntity currentLocation = null;
	private RobotCI robot;
	
	public SharedThymioBroadcastMessage(RobotCI robot) {
		super(UPDATE_TIME, IDENTIFIER);
		this.robot = robot;
	}

	@Override
	protected String getMessage() {
		Vector2d position = currentLocation.getPosition();
		
		if(position != null){
			return currentLocation.getName()+MESSAGE_SEPARATOR+
					currentLocation.getObserverAddress()+MESSAGE_SEPARATOR+
					position.getX()+MESSAGE_SEPARATOR+
					position.getY();
		}
		return null;
	}

	public static ThymioSharedEntity decode(String message) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		
		if(split.length == 5){
			return new ThymioSharedEntity(
					split[1], 
					split[2], 
					new Vector2d(
							Double.parseDouble(split[3]), 
							Double.parseDouble(split[4]))
					);
		}
		return null;
	}
	
	@Override
	public String[] encode() {
		LinkedList<String> tempMessages = new LinkedList<String>();
		
		for (Entity e : robot.getEntities()) {
			if(e instanceof ThymioSharedEntity){
				ThymioSharedEntity tse = (ThymioSharedEntity)e;
				if(robot.getNetworkAddress().equals(tse.getObserverAddress())) {
					currentLocation = tse;
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
