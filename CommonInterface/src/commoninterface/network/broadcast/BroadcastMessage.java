package commoninterface.network.broadcast;

import commoninterface.AquaticDroneCI;

public abstract class BroadcastMessage {
	
	public static final String MESSAGE_SEPARATOR = ";";
	protected AquaticDroneCI drone;
	protected long updateTime = Long.MAX_VALUE;
	protected String identifier;
	
	public BroadcastMessage(AquaticDroneCI drone, long updateTime, String identifier) {
		this.drone = drone;
		this.updateTime = updateTime;
		this.identifier = identifier;
	}

	protected abstract String getMessage();
		
	public String encode() {
		return identifier+MESSAGE_SEPARATOR+getMessage();
	}
	
	public long getUpdateTimeInMiliseconds() {
		return updateTime;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
}
