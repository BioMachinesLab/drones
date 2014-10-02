package network.messages;

public class SystemStatusMessage extends Message {
	private static final long serialVersionUID = -3082091690910649178L;
	private String message = null;

	public SystemStatusMessage(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}