package network.messages;

public class CompassMessage extends Message {
	private static final long serialVersionUID = 7925900141790370630L;
	private int[] axisReadings;

	public CompassMessage(int[] axisReadings) {
		super();
		this.axisReadings = axisReadings;
	}

	public int getXAxis() {
		return axisReadings[0];
	}

	public int getYAxis() {
		return axisReadings[1];
	}

	public int getZAxis() {
		return axisReadings[2];
	}

	public int[] getAxisReadings() {
		return axisReadings;
	}
}
