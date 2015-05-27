package io;

import commoninterface.RobotCI;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.InformationRequest.MessageType;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;

public class SystemStatusMessageProvider implements MessageProvider {

	private RobotCI robot;

	public SystemStatusMessageProvider(RobotCI robot) {
		this.robot = robot;
	}

	@Override
	public Message getMessage(Message request) {

		if (request instanceof InformationRequest) {
			InformationRequest infoRequest = (InformationRequest) request;
			if (infoRequest.getMessageTypeQuery() == MessageType.SYSTEM_STATUS)
				return new SystemStatusMessage(robot.getStatus(),
						robot.getNetworkAddress());
		}

		return null;
	}
}