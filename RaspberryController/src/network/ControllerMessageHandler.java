package network;

import network.messages.InformationRequest;
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

		if (response == null) {
			
			String sResponse = "No message provider for the current request (";
			
			if(m instanceof InformationRequest) {
				InformationRequest ir = (InformationRequest)m;
				sResponse+=ir.getMessageTypeQuery() + ")";
			} else {
				sResponse+=request.getClass().getSimpleName() + ")";
			}
			
			response = new SystemStatusMessage(sResponse);
		}

		pendingConnections[currentIndex].sendData(response);
	}
}