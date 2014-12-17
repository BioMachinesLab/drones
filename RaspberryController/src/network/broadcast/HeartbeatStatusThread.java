package network.broadcast;

public class HeartbeatStatusThread extends BroadcastStatusThread {
	
	private static final int UPDATE_TIME = 5*1000; //5 sec
	private static final String IDENTIFIER = "HEARTBEAT";
	private static final long startTime = System.currentTimeMillis();
	
	public HeartbeatStatusThread(BroadcastHandler handler) {
		super(handler, IDENTIFIER, UPDATE_TIME);
	}

	@Override
	public String createMessage() {
		return ""+(System.currentTimeMillis()-startTime);
	}
	
	public static long decode(String message) {
		String[] split = message.split(MESSAGE_SEPARATOR);
		return Long.parseLong(split[1]);
	}
	
}