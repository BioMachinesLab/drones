package network.messages;

import network.messages.Message;

public class LogMessage extends Message {
	private static final long serialVersionUID = 7925900141790370630L;
	private String log;

	public LogMessage(String log) {
		super();
		this.log = log;
	}
	
	public String getLog() {
		return log;
	}
	

}
