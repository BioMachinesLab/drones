package commoninterface.objects;

import java.util.LinkedHashSet;

import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class RobotLocation extends GeoEntity{
	
	private double orientation;

	public RobotLocation(String name, LatLon latLon, double orientation) {
		super(name, latLon);
		this.orientation = orientation;
		
	}
	
	public double getOrientation() {
		return orientation;
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
