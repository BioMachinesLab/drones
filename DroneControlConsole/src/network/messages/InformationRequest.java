package network.messages;

public class InformationRequest extends Message {
	public static enum Message_Type {
		GPS, SYSTEM, BATTERY, COMPASS
	}

	private static final long serialVersionUID = 7787407138336393178L;
	private Message_Type type;

	public InformationRequest(Message_Type type) {
		super();
		this.type = type;
	}

	public Message_Type getMessageTypeQuery() {
		return type;
	}

}
