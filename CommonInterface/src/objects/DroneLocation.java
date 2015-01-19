package objects;

import java.util.LinkedHashSet;

import commoninterface.AquaticDroneCI;

public class DroneLocation extends Entity{

	public DroneLocation(double lat, double lon, double orientation, String name) {
		super(lat, lon, orientation, name);
	}
	
	public static LinkedHashSet<DroneLocation> getDroneLocations(AquaticDroneCI drone) {
		LinkedHashSet<DroneLocation> droneLocations = new LinkedHashSet<DroneLocation>();
		
		for(Entity e : drone.getEntities()) {
			if(e instanceof DroneLocation)
				droneLocations.add((DroneLocation)e);
		}
		
		return droneLocations;
	}
	
}
