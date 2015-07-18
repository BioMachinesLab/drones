package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class CompassMessage extends Message {
	private static final long serialVersionUID = 7925900141790370630L;
	private int heading;

	public CompassMessage(int heading, String senderHostname) {
		super(senderHostname);
		this.heading = heading;
	}

	public int getHeading() {
		return heading;
	}
	
	@Override
	public Message getCopy() {
		return new CompassMessage(heading, senderHostname);
	}
}
