package network.messages;

import network.messages.Message;
import dataObjects.BatteryStatus;

public class BatteryMessage extends Message {
	private static final long serialVersionUID = 8202553628283198093L;
	private BatteryStatus batteryStatus;

	public BatteryMessage(BatteryStatus batteryStatus) {
		super();
		this.batteryStatus = batteryStatus;
	}

	public BatteryStatus getBatteryStatus() {
		return batteryStatus;
	}
}
