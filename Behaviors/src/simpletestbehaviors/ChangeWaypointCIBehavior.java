package simpletestbehaviors;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public class ChangeWaypointCIBehavior extends CIBehavior{
	
	private double distance = 3;
	private AquaticDroneCI drone;
	
	public ChangeWaypointCIBehavior(CIArguments args, RobotCI robot) {
		super(args,robot);
		distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
		drone = (AquaticDroneCI)robot;
	}

	@Override
	public void step(double timestep) {
		
		ArrayList<Waypoint> waypoints = Waypoint.getWaypoints(drone);
		
		if(drone.getActiveWaypoint() != null && waypoints.size() > 1) {
			
			double d = CoordinateUtilities.distanceInMeters(drone.getGPSLatLon(), drone.getActiveWaypoint().getLatLon());
			
			if(d < distance) {
				
				int index = waypoints.indexOf(drone.getActiveWaypoint());
				
				if(index != -1) {
					index = (index+1)%waypoints.size();
					drone.setActiveWaypoint(waypoints.get(index));
				}
			}
		}
	}
}
