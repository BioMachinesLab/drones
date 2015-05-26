package dataObjects;

import java.util.ArrayList;
import java.util.HashMap;

public class DronesSet {
	private HashMap<String, DroneData> dronesSet = new HashMap<>();

	public void addDrone(DroneData drone) {
		dronesSet.put(drone.getIpAddr().getHostAddress(), drone);
	}

	public void removeDrone(String ipAddr) {
		dronesSet.remove(ipAddr);
	}

	public void removeDrone(DroneData drone) {
		dronesSet.remove(drone.getIpAddr().getHostAddress(), drone);
	}

	public DroneData getDrone(String ipAddr) {
		return dronesSet.get(ipAddr);
	}

	public boolean existsDrone(String ipAddr) {
		return dronesSet.containsKey(ipAddr);
	}

	public ArrayList<DroneData> getDronesSet() {
		return new ArrayList<DroneData>(dronesSet.values());
	}

	public ArrayList<DroneData> getDrones(
			ArrayList<String> dronesIdentifications) {
		if (dronesIdentifications != null && !dronesIdentifications.isEmpty()) {
			ArrayList<DroneData> dronesData = new ArrayList<DroneData>();

			for (String str : dronesIdentifications) {
				dronesData.add(dronesSet.get(str));
			}

			return dronesData;
		} else {
			return new ArrayList<DroneData>(dronesSet.values());
		}
	}
}
