package network.server.messages;

import java.util.ArrayList;

import dataObjects.DroneData;

public class DronesInformationResponse extends ServerMessage {
	public ArrayList<DroneData> dronesData = new ArrayList<DroneData>();

	public DronesInformationResponse() {
		super(MessageType.DRONES_INFORMATION_RESPONSE);
	}

	public void addDroneData(DroneData droneData) {
		dronesData.add(droneData);
	}

	public void setDronesData(ArrayList<DroneData> dronesData) {
		this.dronesData = dronesData;
	}
}
