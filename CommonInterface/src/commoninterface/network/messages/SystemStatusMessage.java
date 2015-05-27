package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class SystemStatusMessage extends Message {
	private static final long serialVersionUID = -3082091690910649178L;
	private String message = null;

	public SystemStatusMessage(String message, String senderHostname) {
		super(senderHostname);

		if (senderHostname == null)
			throw new NullPointerException();

		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
