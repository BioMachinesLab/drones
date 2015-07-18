package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class LogMessage extends Message {
	private static final long serialVersionUID = 7925900141790370630L;
	private String log;

	public LogMessage(String log, String senderHostname) {
		super(senderHostname);
		this.log = log;
	}
	
	public String getLog() {
		return log;
	}
	
	@Override
	public Message getCopy() {
		return new LogMessage(log, senderHostname);
	}
	

}
