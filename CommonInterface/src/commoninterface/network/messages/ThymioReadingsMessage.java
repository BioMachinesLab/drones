package commoninterface.network.messages;

import java.util.List;

import commoninterface.network.messages.Message;

public class ThymioReadingsMessage extends Message {

	private static final long serialVersionUID = -6251588481500969630L;
	private List<Short> readings;

	public ThymioReadingsMessage(List<Short> readings, String senderHostname) {
		super(senderHostname);
		this.readings = readings;
	}

	public List<Short> getReadings() {
		return readings;
	}
	
	@Override
	public Message getCopy() {
		return new ThymioReadingsMessage(readings, senderHostname);
	}

}
