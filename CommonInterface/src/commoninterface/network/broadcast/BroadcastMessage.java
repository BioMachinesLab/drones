package commoninterface.network.broadcast;

import commoninterface.RobotCI;

public abstract class BroadcastMessage {
	
	public static final String MESSAGE_SEPARATOR = ";";
	protected RobotCI robot;
	protected long updateTime = Long.MAX_VALUE;
	protected String identifier;
	
	public BroadcastMessage(RobotCI robot, long updateTime, String identifier) {
		this.robot = robot;
		this.updateTime = updateTime;
		this.identifier = identifier;
	}

	protected abstract String getMessage();
		
	public String encode() {
		String msg = getMessage();
		if(msg != null) {
			return identifier+MESSAGE_SEPARATOR+msg;
		}
		return null;
	}
	
	public long getUpdateTimeInMiliseconds() {
		return updateTime;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
}
