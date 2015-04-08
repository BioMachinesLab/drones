package io;

import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemStatusMessage;

import commoninterface.RealRobotCI;

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