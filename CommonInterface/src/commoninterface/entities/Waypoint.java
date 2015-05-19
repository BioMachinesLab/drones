package commoninterface.entities;

import java.util.ArrayList;

import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class Waypoint extends GeoEntity {
	
	public Waypoint(String name, LatLon latLon) {
		super(name, latLon);
	}
	
	public static ArrayList<Waypoint> getWaypoints(RobotCI robot) {
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		
		for(Object o : robot.getEntities().toArray()) { 
			Entity e = (Entity)o;
		
			if(e instanceof Waypoint)
				waypoints.add((Waypoint)e);
		}
		
		return waypoints;
	}
	
}