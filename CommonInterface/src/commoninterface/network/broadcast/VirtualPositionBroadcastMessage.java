package commoninterface.network.broadcast;

import commoninterface.ThymioCI;
import commoninterface.entities.PreyEntity;
import commoninterface.entities.ThymioEntity;
import commoninterface.mathutils.Vector2d;


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
			
			VirtualPositionType received = VirtualPositionType.valueOf(receivedType);
			
			switch(received) {
				case ROBOT:
					if(thymio.getNetworkAddress().equals(receivedAddress)){
						thymio.setVirtualPosition(receivedX, receivedY);
						thymio.setVirtualOrientation(receivedOrientation);
					}else{
						ThymioEntity te = new ThymioEntity(receivedAddress, new Vector2d(receivedX, receivedY));
						thymio.replaceEntity(te);
					}
					break;
				case PREY:
					PreyEntity pe = new PreyEntity(receivedAddress, new Vector2d(receivedX, receivedY));
					thymio.replaceEntity(pe);
					break;
				default:
			}
		}
		
	}
	
}
