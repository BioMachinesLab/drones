package network.server.shared.messages;

import java.util.ArrayList;

public class DronesInformationRequest extends ServerMessage {
	private ArrayList<String> dronesIdentifications = new ArrayList<String>();

	public DronesInformationRequest() {
		super(MessageType.DRONES_INFORMATION_REQUEST);
	}

	public ArrayList<String> getDroneIdentification() {
		return dronesIdentifications;
	}

	public void addDroneIdentification(String droneIdentification) {
		dronesIdentifications.add(droneIdentification);
	}

	public void removeDroneIdentification(String droneIdentification) {
		dronesIdentifications.remove(droneIdentification);
	}

	public void clearDroneIdentification() {
		dronesIdentifications.clear();
	}
}
