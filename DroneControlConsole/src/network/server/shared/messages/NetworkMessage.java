package network.server.shared.messages;

/**
 * Wrapper for the network message, to be serialized with the Gson Library
 * 
 *
 */
public class NetworkMessage {
	public static enum MSG_TYPE {
		DronesInformationRequest, DronesInformationResponse, ServerStatusResponse, ServerStatusRequest
	}

	private MSG_TYPE msgType;

	private DronesInformationRequest DIReq;
	private DronesInformationResponse DIResp;
	private ServerStatusResponse SSResp;
	private ServerStatusRequest SSReq;

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
			SSReq = (ServerStatusRequest) message;
			msgType = MSG_TYPE.ServerStatusRequest;
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
		case ServerStatusRequest:
			return SSReq;
		default:
			return null;
		}
	}

	public MSG_TYPE getMsgType() {
		return msgType;
	}
}
