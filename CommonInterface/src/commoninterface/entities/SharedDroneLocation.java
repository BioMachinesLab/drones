package commoninterface.entities;

import java.util.LinkedList;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class SharedDroneLocation extends GeoEntity {

	private static final long serialVersionUID = 2645528509058118310L;
	private AquaticDroneCI.DroneType type;
	private String observerAddress;

	public SharedDroneLocation(String name, String observerAddress, LatLon latLon, AquaticDroneCI.DroneType type) {
		super(name, latLon);
		this.type = type;
		this.observerAddress = observerAddress;
	}

	public AquaticDroneCI.DroneType getDroneType() {
		return type;
	}

	public String getObserverAddress() {
		return observerAddress;
	}

	public static LinkedList<SharedDroneLocation> getSharedDroneLocations(RobotCI robot) {
		LinkedList<SharedDroneLocation> droneLocations = new LinkedList<SharedDroneLocation>();

		Object[] entities = robot.getEntities().toArray();

		for (Object e : entities) {
			if (e instanceof SharedDroneLocation)
				droneLocations.add((SharedDroneLocation) e);
		}

		return droneLocations;
	}

	@Override
	public SharedDroneLocation clone() {
		return new SharedDroneLocation(observerAddress, observerAddress, new LatLon(latLon), type);
	}
}
