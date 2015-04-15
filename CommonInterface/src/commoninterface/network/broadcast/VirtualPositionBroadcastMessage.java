package commoninterface.network.broadcast;

import java.util.ArrayList;

import commoninterface.ThymioCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.objects.Entity;
import commoninterface.objects.PreyEntity;
import commoninterface.objects.ThymioEntity;


public class VirtualPositionBroadcastMessage extends BroadcastMessage {

	public enum VirtualPositionType {
		ROBOT,PREY
	}
	
	public static final String IDENTIFIER = "VIRTUAL_POSITION";
	private static final int UPDATE_TIME = 1*1000; //1 sec
	private VirtualPositionType type;
	private String address;
	private double xPosition;
	private double yPosition;
	private double orientation;
	
	public VirtualPositionBroadcastMessage(VirtualPositionType type, String address, double xPosition, double yPosition, double orientation) {
		super(UPDATE_TIME, IDENTIFIER);
		this.type = type;
		this.address = address;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.orientation = orientation;
	}

	@Override
	protected String getMessage() {
		return type + BroadcastMessage.MESSAGE_SEPARATOR + address + BroadcastMessage.MESSAGE_SEPARATOR + xPosition + BroadcastMessage.MESSAGE_SEPARATOR + yPosition + BroadcastMessage.MESSAGE_SEPARATOR + orientation;
	}
	
	public static void decode(String address, String message, ThymioCI thymio) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		
		if(split.length == 6){	
			String receivedType = split[1];
			String receivedAddress = split[2];
			double receivedX = Double.valueOf(split[3]);
			double receivedY = Double.valueOf(split[4]);
			double receivedOrientation = Double.valueOf(split[5]);
			
			if(receivedType.equals(VirtualPositionType.ROBOT.toString())){
				
				if(thymio.getNetworkAddress().equals(receivedAddress)){
					thymio.setVirtualPosition(receivedX, receivedY);
					thymio.setVirtualOrientation(receivedOrientation);
				}else{
					removeEntityByName(thymio.getEntities(), receivedAddress);
					thymio.getEntities().add(new ThymioEntity(receivedAddress, new Vector2d(receivedX, receivedY)));
				}
				
			}else if(receivedType.equals(VirtualPositionType.PREY.toString())){
				removeEntityByName(thymio.getEntities(), receivedAddress);
				thymio.getEntities().add(new PreyEntity(receivedAddress, new Vector2d(receivedX, receivedY)));
			}
			
		}
		
	}
	
	private static void removeEntityByName(ArrayList<Entity> entities, String receivedAddress){
		Entity entityToRemove = null;
		
		for (Entity e : entities) {
			if(e.getName().equals(receivedAddress)){
				entityToRemove = e;
				break;
			}
		}
		
		if(entityToRemove != null)
			entities.remove(entityToRemove);
	}
	
}
