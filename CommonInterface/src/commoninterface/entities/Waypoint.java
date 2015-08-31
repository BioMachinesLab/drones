package commoninterface.entities;

import java.util.ArrayList;

import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class Waypoint extends GeoEntity {
	
	private static final long serialVersionUID = -2781183218215515113L;

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
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Waypoint)
			return super.equals(obj) && latLon.equals(((Waypoint)obj).getLatLon());
		
		return false;
	}
	
}