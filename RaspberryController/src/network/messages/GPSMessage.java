package network.messages;

import dataObjects.GPSData;

public class GPSMessage extends Message {
	private static final long serialVersionUID = -7540879836376808265L;
	private GPSData gpsData;

	public GPSMessage(GPSData gpsData) {
		super();
		this.gpsData = gpsData;
	}

	public GPSData getGPSData() {
		return gpsData;
	}
}
