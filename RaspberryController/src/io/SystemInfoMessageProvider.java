package io;

import java.io.IOException;
import java.text.ParseException;

import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemInformationsMessage;
import network.messages.SystemStatusMessage;
import dataObjects.SystemInformationsData;

public class SystemInfoMessageProvider implements MessageProvider {
	
	private Message msg = null;
	
	public SystemInfoMessageProvider() {
		try {
			msg = new SystemInformationsMessage();
		} catch (Exception | Error e) {
			System.err.println("Error fetching informations from system!");
			msg = new SystemStatusMessage("Error fetching informations from system!");
		}
	}
	
	@Override
	public Message getMessage(Message request) {
		if(request instanceof InformationRequest) {
			InformationRequest infoRequest = (InformationRequest)request;
			if(infoRequest.getMessageTypeQuery() == MessageType.SYSTEM_INFO) {
				
				if(msg instanceof SystemInformationsMessage) {
					((SystemInformationsMessage)msg).update();
				}
				
				return msg;
			}
		}
		
		return null;
	}
}