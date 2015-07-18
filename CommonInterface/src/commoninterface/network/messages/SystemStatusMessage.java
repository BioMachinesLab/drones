package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class SystemStatusMessage extends Message {
	private static final long serialVersionUID = -3082091690910649178L;
	private String message = null;

	public SystemStatusMessage(String message, String senderHostname) {
		super(senderHostname);			

		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public Message getCopy() {
		return new SystemStatusMessage(message, message);
	}
}
