package network.server.shared.messages;

public class ServerMessage {
	public static enum MessageType {
		COMMAND_MESSAGE,DRONES_MOTORS_SET, DRONES_INFORMATION_REQUEST, SERVER_INFORMATIONS_REQUEST, DRONES_INFORMATION_RESPONSE, SERVER_INFORMATIONS_RESPONSE
	}

	private MessageType type;

	public ServerMessage(MessageType type) {
		this.type = type;
	}

	public ServerMessage() {
		this(null);
	}

	public MessageType getMessageType() {
		return type;
	}

	public void setMessageType(MessageType type) {
		this.type = type;
	}
}
