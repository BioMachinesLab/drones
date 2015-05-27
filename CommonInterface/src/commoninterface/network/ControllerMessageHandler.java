package commoninterface.network;

import commoninterface.RobotCI;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;

public class ControllerMessageHandler extends MessageHandler {

	private RobotCI robot;

	public ControllerMessageHandler(RobotCI c) {
		this.robot = c;
	}

	@Override
	protected Message processMessage(Message m) {
		Message response = null;

		for (MessageProvider p : robot.getMessageProviders()) {
			response = p.getMessage(m);

			if (response != null)
				break;
		}

		if (response == null) {

			String sResponse = "No message provider for the current request (";

			if (m instanceof InformationRequest) {
				InformationRequest ir = (InformationRequest) m;
				sResponse += ir.getMessageTypeQuery() + ")";
			} else {
				sResponse += m.getClass().getSimpleName() + ")";
			}

			response = new SystemStatusMessage(sResponse,
					robot.getNetworkAddress());
		}

		return response;
	}

}