package objects;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;

public class Waypoint extends Entity {
	
	public Waypoint(double lat, double lon, String name) {
		super(lat, lon, name);
	}
	
	public static ArrayList<Waypoint> getWaypoints(AquaticDroneCI drone) {
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		
		for(Entity e : drone.getEntities()) {
			if(e instanceof Waypoint)
				waypoints.add((Waypoint)e);
		}
		
		return waypoints;
	}
	
}