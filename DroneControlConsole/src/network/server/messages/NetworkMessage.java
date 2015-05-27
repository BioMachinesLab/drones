package network.server.messages;

import dataObjects.DroneData;

/**
 * Wrapper for the network message, to be serialized with the Gson Library
 * 
 *
 */
public class NetworkMessage {
	public static enum MSG_TYPE {
		DronesInformationRequest, DronesInformationResponse, ServerStatusResponse, ServerMessage
	}

	private MSG_TYPE msgType;

	private DronesInformationRequest DIReq;
	private DronesInformationResponse DIResp;
	private ServerStatusResponse SSResp;
	private ServerMessage serverMsg;

	public void setMessage(ServerMessage message) {
		if (message instanceof DronesInformationRequest) {
			DIReq = (DronesInformationRequest) message;
			msgType = MSG_TYPE.DronesInformationRequest;
		} else if (message instanceof DronesInformationResponse) {
			DIResp = (DronesInformationResponse) message;
			msgType = MSG_TYPE.DronesInformationResponse;
		} else if (message instanceof ServerStatusResponse) {
			SSResp = (ServerStatusResponse) message;
			msgType = MSG_TYPE.ServerStatusResponse;
		} else {
			serverMsg = message;
			msgType = MSG_TYPE.ServerMessage;
		}
	}

	public ServerMessage getMessage() {
		switch (msgType) {
		case DronesInformationRequest:
			return DIReq;
		case DronesInformationResponse:
			return DIResp;
		case ServerStatusResponse:
			return SSResp;
		case ServerMessage:
			return serverMsg;
		default:
			return null;
		}
	}

	public MSG_TYPE getMsgType() {
		return msgType;
	}
}
