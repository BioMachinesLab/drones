package network.server.shared.messages;

public class CommandMessage extends ServerMessage {
	public static enum Action {
		START, STOP, DEPLOY, STOPALL, SETLOGSTAMP, DEPLOYENTITIES
	}

	private String[] payload = null;
	private Action messageAction;

	public CommandMessage() {
		super(MessageType.COMMAND_MESSAGE);
	}

	public void setPayload(String[] payload) {
		this.payload = payload;
	}

	public void setMessageAction(Action messageAction) {
		this.messageAction = messageAction;
	}

	public String[] getPayload() {
		return payload;
	}

	public Action getMessageAction() {
		return messageAction;
	}
}
