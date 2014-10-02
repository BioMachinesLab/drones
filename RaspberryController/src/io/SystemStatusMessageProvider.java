package io;

import main.Controller;
import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemStatusMessage;

public class SystemStatusMessageProvider implements MessageProvider {
	
	private Controller controller;
	
	public SystemStatusMessageProvider(Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public Message getMessage(Message request) {
		
		if(request instanceof InformationRequest) {
			InformationRequest infoRequest = (InformationRequest)request;
			if(infoRequest.getMessageTypeQuery() == MessageType.SYSTEM_STATUS)
				return new SystemStatusMessage(controller.getStatus());
		}
		
		return null;
	}
}