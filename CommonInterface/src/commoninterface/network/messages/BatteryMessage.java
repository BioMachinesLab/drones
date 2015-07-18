package commoninterface.network.messages;

import commoninterface.dataobjects.BatteryStatus;
import commoninterface.network.messages.Message;

public class BatteryMessage extends Message {
	private static final long serialVersionUID = 8202553628283198093L;
	private BatteryStatus batteryStatus;

	public BatteryMessage(BatteryStatus batteryStatus,String senderHostname) {
		super(senderHostname);
		this.batteryStatus = batteryStatus;
	}

	public BatteryStatus getBatteryStatus() {
		return batteryStatus;
	}
	
	@Override
	public Message getCopy() {
		return new BatteryMessage(batteryStatus, getSenderHostname());
	}
}
