package commoninterface.network.broadcast;

import commoninterface.AquaticDroneCI;

public class HeartbeatBroadcastMessage extends BroadcastMessage {
	
	public static final String IDENTIFIER = "HEARTBEAT";
	private static final int UPDATE_TIME = 5*1000; //5 sec
	private static final long START_TIME = System.currentTimeMillis();
	
	public HeartbeatBroadcastMessage(AquaticDroneCI drone) {
		super(drone, UPDATE_TIME, IDENTIFIER);
	}
	
	@Override
	public String getMessage() {
		return ""+(System.currentTimeMillis()-START_TIME);
	}
	
	public static long decode(String message) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		return Long.parseLong(split[1]);
	}

}