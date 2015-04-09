package io;

import commoninterface.RealRobotCI;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;
import commoninterface.network.messages.InformationRequest.MessageType;

public class SystemStatusMessageProvider implements MessageProvider {
	
	private RealRobotCI drone;
	
	public SystemStatusMessageProvider(RealRobotCI drone) {
		this.drone = drone;
	}
	
	@Override
	public Message getMessage(Message request) {
		
		if(request instanceof InformationRequest) {
			InformationRequest infoRequest = (InformationRequest)request;
			if(infoRequest.getMessageTypeQuery() == MessageType.SYSTEM_STATUS)
				return new SystemStatusMessage(drone.getStatus());
		}
		
		return null;
	}
}