package network.server.shared.messages;


public class ServerStatusRequest extends ServerMessage {
	public ServerStatusRequest() {
		super(MessageType.SERVER_INFORMATIONS_REQUEST);
	}
}
