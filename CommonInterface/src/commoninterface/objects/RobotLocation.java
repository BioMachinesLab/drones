package commoninterface.objects;

import java.util.LinkedHashSet;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class RobotLocation extends GeoEntity{
	
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
	
	public static LinkedHashSet<RobotLocation> getDroneLocations(RobotCI robot) {
		LinkedHashSet<RobotLocation> droneLocations = new LinkedHashSet<RobotLocation>();
		
		for(Entity e : robot.getEntities()) {
			if(e instanceof RobotLocation)
				droneLocations.add((RobotLocation)e);
		}
		
		return droneLocations;
	}
	
}
