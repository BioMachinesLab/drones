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
		
	public String[] encode() {
		
		String[] messages = new String[1];
		
		String msg = getMessage();
		if(msg != null) {
			messages[0] = identifier+MESSAGE_SEPARATOR+msg;
		}
		
		if(messages[0] != null)
			return messages;
		
		return null;
	}
	
	public long getUpdateTimeInMiliseconds() {
		return updateTime;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
}
