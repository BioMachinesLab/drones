package network;

import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemStatusMessage;
import main.Controller;

public class ControllerMessageHandler extends MessageHandler {
	
	private Controller controller;
	
	public ControllerMessageHandler(Controller c) {
		this.controller = c;
	}
	
	@Override
	protected void processMessage(Message m, ConnectionHandler c) {
		Message request = pendingMessages[currentIndex];
		Message response = null;
		
		for (MessageProvider p : controller.getMessageProviders()) {
			response = p.getMessage(request);
			if (response != null)
				break;
		}

		if (response == null)
			response = new SystemStatusMessage(
					"No message provider for the current request ("
							+ request.getClass().getSimpleName() + ")");

		pendingConnections[currentIndex].sendData(response);
	}
}