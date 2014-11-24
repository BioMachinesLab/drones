package network.messages;

import objects.Waypoint;

public class WaypointMessage extends Message {
	
	private static final long serialVersionUID = 7355614915881468297L;
	private Waypoint waypoint;
	
	public WaypointMessage(Waypoint p) {
		this.waypoint = p;
	}
	
	public Waypoint getWaypoint() {
		return waypoint;
	}

}
