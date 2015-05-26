package network.server.messages;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServerMessage {
	public static enum MessageType {
		DRONES_INFORMATION_REQUEST, SERVER_INFORMATIONS_REQUEST, DRONES_INFORMATION_RESPONSE, SERVER_INFORMATIONS_RESPONSE
	}

	private MessageType type;

	private String time;

	public ServerMessage(MessageType type) {
		this.type = type;

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		this.time = sdf.format(Calendar.getInstance().getTime());
		Calendar.getInstance();
	}

	public ServerMessage() {
		this(null);
	}

	public String getTime() {
		return time;
	}

	public MessageType getMessageType() {
		return type;
	}

	public void setMessageType(MessageType type) {
		this.type = type;
	}
}
