package network.messages;

public class CompassMessage extends Message {
	private static final long serialVersionUID = 7925900141790370630L;
	private int heading;

	public CompassMessage(int heading) {
		super();
		this.heading = heading;
	}

	public int getHeading() {
		return heading;
	}
}
