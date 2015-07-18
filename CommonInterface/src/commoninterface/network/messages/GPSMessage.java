package commoninterface.network.messages;

import commoninterface.dataobjects.GPSData;
import commoninterface.network.messages.Message;

public class GPSMessage extends Message {
	private static final long serialVersionUID = -7540879836376808265L;
	private GPSData gpsData;

	public GPSMessage(GPSData gpsData, String senderHostname) {
		super(senderHostname);
		this.gpsData = gpsData;
	}

	public GPSData getGPSData() {
		return gpsData;
	}
	
	@Override
	public Message getCopy() {
		return new GPSMessage(gpsData, senderHostname);
	}
}
