package network.server.shared.messages;

import java.util.ArrayList;

import network.server.shared.dataObjects.DroneData;

public class DronesInformationResponse extends ServerMessage {
	public ArrayList<DroneData> dronesData = new ArrayList<DroneData>();

	public DronesInformationResponse() {
		super(MessageType.DRONES_INFORMATION_RESPONSE);
	}

	public void setDronesData(ArrayList<DroneData> dronesData) {
		this.dronesData = dronesData;
	}
}
