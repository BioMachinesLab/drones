package commoninterface.entities;

import java.util.ArrayList;
import java.util.LinkedList;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.jcoord.LatLon;

public class GeoFence extends Entity {
	private static final long serialVersionUID = 4262661106635625238L;
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
		Waypoint wp = new Waypoint("geofence_wp_" + waypoints.size(), latLon);
		waypoints.add(wp);
	}

	public void clear() {
		waypoints.clear();
	}

	public LinkedList<Waypoint> getWaypoints() {
		return waypoints;
	}

	public static ArrayList<GeoFence> getGeoFences(AquaticDroneCI drone) {
		ArrayList<GeoFence> geoFences = new ArrayList<GeoFence>();

		for (Entity e : drone.getEntities()) {
			if (e instanceof GeoFence)
				geoFences.add((GeoFence) e);
		}

		return geoFences;
	}

	@Override
	public String toString() {
		String s = this.getClass().getSimpleName() + ";";
		for (Waypoint w : getWaypoints())
			s += "FENCEWP;" + w.getLatLon().getLat() + ";" + w.getLatLon().getLon() + ";";
		return s;
	}

	@Override
	public GeoFence clone() {
		GeoFence geo = new GeoFence(name, cloneEntities(waypoints));
		return geo;
	}

	private static LinkedList<Waypoint> cloneEntities(LinkedList<Waypoint> list) {
		LinkedList<Waypoint> clone = new LinkedList<Waypoint>();
		for (Waypoint item : list) {
			clone.add(item.clone());
		}
		return clone;
	}
}