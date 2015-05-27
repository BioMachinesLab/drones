package commoninterface.network.broadcast;

import commoninterface.RobotCI;

public class HeartbeatBroadcastMessage extends BroadcastMessage {

	public static final String IDENTIFIER = "HEARTBEAT";
	private static final int UPDATE_TIME = 5 * 1000; // 5 sec
	private static final long START_TIME = System.currentTimeMillis();
	private RobotCI robot;

	public HeartbeatBroadcastMessage(RobotCI robot) {
		super(UPDATE_TIME, IDENTIFIER);
		this.robot = robot;
	}

	@Override
	public String getMessage() {
		String message = "" + (System.currentTimeMillis() - START_TIME);
			return message + MESSAGE_SEPARATOR + robot.getNetworkAddress();
	}

	public static String[] decode(String message) {
		return message.split(MESSAGE_SEPARATOR);
	}
}