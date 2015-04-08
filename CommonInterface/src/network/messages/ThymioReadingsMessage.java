package network.messages;

import java.util.List;

import network.messages.Message;

public class ThymioReadingsMessage extends Message {

	private static final long serialVersionUID = -6251588481500969630L;
	private List<Short> readings;

	public ThymioReadingsMessage(List<Short> readings) {
		super();
		this.readings = readings;
	}

	public List<Short> getReadings() {
		return readings;
	}
	
}
