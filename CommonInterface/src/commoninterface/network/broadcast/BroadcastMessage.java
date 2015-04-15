package commoninterface.network.broadcast;


public abstract class BroadcastMessage {
	
	public static final String MESSAGE_SEPARATOR = ";";
	protected long updateTime = Long.MAX_VALUE;
	protected String identifier;
	
	public BroadcastMessage(long updateTime, String identifier) {
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
