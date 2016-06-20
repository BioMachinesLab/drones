package commoninterface.entities;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class RobotLocation extends GeoEntity {
	private static final long serialVersionUID = 3994455159701222432L;
	private double orientation;
	private AquaticDroneCI.DroneType type;

	public RobotLocation(String name, LatLon latLon, double orientation, AquaticDroneCI.DroneType type) {
		super(name, latLon);
		this.orientation = orientation;
		this.type = type;
	}

	public double getOrientation() {
		return orientation;
	}

	public AquaticDroneCI.DroneType getDroneType() {
		return type;
	}

	public static ArrayList<RobotLocation> getDroneLocations(RobotCI robot) {
		ArrayList<RobotLocation> droneLocations = new ArrayList<RobotLocation>();

		for (Entity e : robot.getEntities()) {
			if (e instanceof RobotLocation)
				droneLocations.add((RobotLocation) e);
		}

		return droneLocations;
	}

	@Override
	public RobotLocation clone() {
		return new RobotLocation(name, new LatLon(latLon), orientation, type);
	}
}
