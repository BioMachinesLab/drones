package controllers;

import java.util.LinkedList;

import simpletestbehaviors.GoToWaypointCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public class LawnMowerController extends Controller{
	
	protected double laneWidth = 10;
	protected GeoFence fence;
	protected GoToWaypointCIBehavior gotoWaypoint;

	public LawnMowerController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		laneWidth = args.getArgumentAsDoubleOrSetDefault("lanewidth", laneWidth);
		gotoWaypoint = new GoToWaypointCIBehavior(new CIArguments(args.getCompleteArgumentString()), (RobotCI)robot);
	}
	
	@Override
	public void controlStep(double time) {
		super.controlStep(time);
		
		//Setup controller
		if(fence == null) {
			fence = GeoFence.getGeoFences((AquaticDroneCI)robot).get(0);
			
			LinkedList<Waypoint> wps = fence.getWaypoints();
			
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			
			for(Waypoint wp : wps) {
				Vector2d v = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
				minX = Math.min(v.x, minX);
				minY = Math.min(v.y, minY);
				maxX = Math.max(v.x, maxX);
				maxY = Math.max(v.y, maxY);
			}
			
			double width = maxX - minX;
			
			int nLanes = (int)Math.ceil((width)/laneWidth);
			
			double extraSide = Math.max(laneWidth/2,(width-nLanes*laneWidth));
			
			int currentIndex = 0;
			
			boolean down = true;
			
			AquaticDroneCI drone = (AquaticDroneCI)robot;
			
			Waypoint active = null;
			
			for(int i = 0 ; i < nLanes ; i++) {
				
				double currentX = minX+extraSide+i*laneWidth;
				double currentY = maxY-extraSide;
				
				Waypoint currentWp1 = new Waypoint("wp"+(currentIndex++), CoordinateUtilities.cartesianToGPS(new Vector2d(currentX,currentY)));
				
				if(active == null)
					active = currentWp1;
				
				currentY = minY+extraSide;
				
				Waypoint currentWp2 = new Waypoint("wp"+(currentIndex++), CoordinateUtilities.cartesianToGPS(new Vector2d(currentX,currentY)));
				
				if(down) {
					drone.getEntities().add(currentWp1);
					drone.getEntities().add(currentWp2);
				} else {
					drone.getEntities().add(currentWp2);
					drone.getEntities().add(currentWp1);
				}
				
				down = !down;
			}
			drone.setActiveWaypoint(active);
		}
		gotoWaypoint.step(time);
	}

}
