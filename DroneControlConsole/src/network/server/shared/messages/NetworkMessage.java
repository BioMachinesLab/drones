package network.server.shared.messages;

/**
 * Wrapper for the network message, to be serialized with the Gson Library
 * 
 *
 */
public class NetworkMessage {
	public static enum MSG_TYPE {
		DroneMotorsSet, DronesInformationRequest, DronesInformationResponse, ServerStatusResponse, ServerStatusRequest
	}

	private MSG_TYPE msgType;

	private DronesInformationRequest DIReq;
	private DronesInformationResponse DIResp;
	private ServerStatusResponse SSResp;
	private ServerStatusRequest SSReq;
	private DronesMotorsSet DMSet;

	public void setMessage(ServerMessage message) throws ClassNotFoundException {
		if (message instanceof DronesInformationRequest) {
			DIReq = (DronesInformationRequest) message;
			msgType = MSG_TYPE.DronesInformationRequest;
		} else if (message instanceof DronesInformationResponse) {
			DIResp = (DronesInformationResponse) message;
			msgType = MSG_TYPE.DronesInformationResponse;
		} else if (message instanceof ServerStatusResponse) {
			SSResp = (ServerStatusResponse) message;
			msgType = MSG_TYPE.ServerStatusResponse;
		} else if (message instanceof ServerStatusRequest) {
			SSReq = (ServerStatusRequest) message;
			msgType = MSG_TYPE.ServerStatusRequest;
		} else if (message instanceof DronesMotorsSet) {
			DMSet = (DronesMotorsSet) message;
			msgType = MSG_TYPE.DroneMotorsSet;
		} else {
			throw new ClassNotFoundException();
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
