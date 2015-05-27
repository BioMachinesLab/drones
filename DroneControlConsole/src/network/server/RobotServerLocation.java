package network.server;

import dataObjects.DroneData;

public class RobotServerLocation {
	public static enum DroneType {
		DRONE, ENEMY, OTHER
	};

	private double orientation;
	private DroneType type;

	private double lat;
	private double lng;

	public RobotServerLocation(String name, double lat, double lng,
			double orientation, DroneType type) {
		this.orientation = orientation;
		this.type = type;
		this.lat = lat;
		this.lng = lng;
	}

	public double getOrientation() {
		return orientation;
	}

	public DroneType getDroneType() {
		return type;
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lng;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RobotServerLocation) {
			RobotServerLocation robotServerLocation = ((RobotServerLocation) obj);
			return (robotServerLocation.getDroneType() == type
					&& robotServerLocation.getLatitude() == lat
					&& robotServerLocation.getLongitude() == lng && robotServerLocation
						.getOrientation() == orientation);

		} else {
			return false;
		}
	}
}
