package network.server.shared.messages;

import network.server.shared.dataObjects.ServerStatusData;

public class ServerStatusResponse extends ServerMessage {
	private ServerStatusData serverStatusData;

	public ServerStatusResponse() {
		super(MessageType.SERVER_INFORMATIONS_RESPONSE);
	}

	public void setServerStatusData(ServerStatusData serverStatusData) {
		this.serverStatusData = serverStatusData;
	}

	public ServerStatusData getServerStatusData() {
		return serverStatusData;
	}
}
