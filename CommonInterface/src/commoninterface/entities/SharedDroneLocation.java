package commoninterface.entities;

import java.util.LinkedHashSet;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class SharedDroneLocation extends GeoEntity{
	
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
	
	public static LinkedHashSet<SharedDroneLocation> getSharedDroneLocations(RobotCI robot) {
		LinkedHashSet<SharedDroneLocation> droneLocations = new LinkedHashSet<SharedDroneLocation>();
		
		for(Entity e : robot.getEntities()) {
			if(e instanceof SharedDroneLocation)
				droneLocations.add((SharedDroneLocation)e);
		}
		
		return droneLocations;
	}
	
}
