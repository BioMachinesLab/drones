package network.server.shared.dataObjects;

import java.util.ArrayList;
import java.util.HashMap;

public class DronesSet {
	private HashMap<String, DroneData> dronesSet;
	private String connectedTo = "";

	public DronesSet() {
		dronesSet = new HashMap<>();
	}

	public void addDrone(DroneData drone) {
		dronesSet.put(drone.getName(), drone);
		System.out.println("[DRONES SET] Added new drone " + drone.getName()
				+ "@" + drone.getIpAddr() + " to set");
	}

	public void removeDrone(String name) {
		dronesSet.remove(name);
	}

	public void removeDrone(DroneData drone) {
		dronesSet.remove(drone.getName());
	}

	public DroneData getDrone(String name) {
		return dronesSet.get(name);
	}

	public boolean existsDrone(String name) {
		return dronesSet.containsKey(name);
	}

	public ArrayList<DroneData> getDronesSet() {
		return new ArrayList<DroneData>(dronesSet.values());
	}

	public ArrayList<DroneData> getDrones(ArrayList<String> dronesNames) {
		if (dronesNames != null && !dronesNames.isEmpty()) {
			ArrayList<DroneData> dronesData = new ArrayList<DroneData>();

			for (String str : dronesNames) {
				dronesData.add(dronesSet.get(str));
			}

			return dronesData;
		} else {
			return new ArrayList<DroneData>(dronesSet.values());
		}
	}

	public void setConnectedTo(String address) {
		this.connectedTo = address;
	}

	public String getConnectedToAddress() {
		return connectedTo;
	}
}
