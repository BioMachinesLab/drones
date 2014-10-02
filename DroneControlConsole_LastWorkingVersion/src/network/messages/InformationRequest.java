package network.messages;

public class InformationRequest extends Message {
	
	public static enum MessageType {
		GPS, SYSTEM_STATUS, BATTERY, COMPASS,SYSTEM_INFO
	}

	private static final long serialVersionUID = 7787407138336393178L;
	private MessageType type;

	public InformationRequest(MessageType type) {
		super();
		this.type = type;
	}

	public MessageType getMessageTypeQuery() {
		return type;
	}

}
