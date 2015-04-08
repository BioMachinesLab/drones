package network.messages;

import network.messages.Message;

public interface MessageProvider {
	
	public Message getMessage(Message request);

}
