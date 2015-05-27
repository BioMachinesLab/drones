package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class LogMessage extends Message {
	private static final long serialVersionUID = 7925900141790370630L;
	private String log;

	public LogMessage(String log, String senderHostaname) {
		super(senderHostaname);
		this.log = log;
	}
	
	public String getLog() {
		return log;
	}
	

}
