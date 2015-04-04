package objects;

import java.util.ArrayList;
import java.util.LinkedList;
import commoninterface.AquaticDroneCI;

public class GeoFence extends Entity {
	
	private LinkedList<Waypoint> waypoints;
	
	public GeoFence(String name, LinkedList<Waypoint> waypoints) {
		super(name);
		this.waypoints = waypoints;
	}
	
	public LinkedList<Waypoint> getWaypoints() {
		return waypoints;
	}
	
	public static ArrayList<GeoFence> getWaypoints(AquaticDroneCI drone) {
		ArrayList<GeoFence> geoFences = new ArrayList<GeoFence>();
		
		for(Entity e : drone.getEntities()) {
			if(e instanceof GeoFence)
				geoFences.add((GeoFence)e);
		}
		
		return geoFences;
	}
	
}