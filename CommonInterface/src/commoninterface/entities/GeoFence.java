package commoninterface.entities;

import java.util.ArrayList;
import java.util.LinkedList;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.jcoord.LatLon;

public class GeoFence extends Entity {
	
	private LinkedList<Waypoint> waypoints;
	
	public GeoFence(String name, LinkedList<Waypoint> waypoints) {
		super(name);
		this.waypoints = waypoints;
	}
	
	public GeoFence(String name) {
		super(name);
		waypoints = new LinkedList<Waypoint>();
	}
	
	public void addWaypoint(Waypoint wp) {
		waypoints.add(wp);
	}
	
	public void addWaypoint(LatLon latLon) {
		Waypoint wp = new Waypoint("geofence_wp_"+waypoints.size(), latLon);
		waypoints.add(wp);
	}
	
	public void clear() {
		waypoints.clear();
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

	public String getLogMessage() {
		String str = "";
		
		for(Waypoint w : getWaypoints())
			str+= w.getLatLon().getLat()+" "+w.getLatLon().getLon()+" ";
		
		return "entity added "+getClass().getSimpleName()+" "+getName()+" "+getWaypoints().size()+" "+str;
	}
	
}