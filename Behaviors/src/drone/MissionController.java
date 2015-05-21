package drone;

import java.util.ArrayList;
import java.util.LinkedList;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import commoninterface.utils.jcoord.LatLon;

public class MissionController extends CIBehavior {
	
	public enum State {
		GO_TO_AREA,PATROL,RECHARGE,PURSUE_INTRUDER
	}
	
	public enum Controller {
		PATROL,INTRUDER,WAYPOINT
	}
	
	protected static long BATTERY_LIFE = 10*60*10; //10 min in timesteps
	protected static double GO_BACK_BATTERY = 0.1;
	protected static double WAYPOINT_DISTANCE_THRESHOLD = 3; //3 meters
	protected static double BASE_DISTANCE_THRESHOLD = 10; //3 meters 
	protected static double ALERT_TIMEOUT = 1*60*10; //1 min
	
	protected CIArguments args;
	protected AquaticDroneCI drone;

	protected State currentState = State.GO_TO_AREA;
	protected double currentBattery = 1.0;
	protected double lastIntruderTime;
	protected int currentSubController = 0;
	
	protected ArrayList<CIBehavior> subControllers = new ArrayList<CIBehavior>();

	//TODO we have to remove the "change active waypoint" instinct for this to work

	public MissionController(CIArguments args, RobotCI robot) {
		super(args, robot);
		this.args = args;
		this.drone = (AquaticDroneCI)robot;
	}
	
	@Override
	public void start() {
		for(Controller c : Controller.values()) {
			CIArguments a = new CIArguments(args.getArgumentAsString(c.toString()));
			subControllers.add(new ControllerCIBehavior(a, drone));
		}
		drone.setActiveWaypoint(null);
	}

	@Override
	public void step(double timestep) {
		
		int subController = 0;
		
		updateEnergy(timestep);
		
		if(currentBattery < GO_BACK_BATTERY) {
			currentState = State.RECHARGE;
		}
		
		switch(currentState) {
			case GO_TO_AREA:
				if(drone.getActiveWaypoint() == null) {
					Waypoint wp = chooseWaypointInGeoFence();
					if(wp != null) {
						
						robot.replaceEntity(wp);
						drone.setActiveWaypoint(wp);
						
						if(robot.getLogger() != null)
							robot.getLogger().logMessage(wp.getLogMessage());
					}
				}
				
				subController = Controller.WAYPOINT.ordinal();
				
				if(drone.getActiveWaypoint() != null) {
				
					double distance = CoordinateUtilities.distanceInMeters(drone.getActiveWaypoint().getLatLon(),drone.getGPSLatLon());
					
					if(distance < WAYPOINT_DISTANCE_THRESHOLD) {
						
						currentState = State.PATROL;
						
						synchronized(robot.getEntities()) {
							robot.getEntities().remove(drone.getActiveWaypoint());
						}
					}
				}
				
				break;
			case PATROL:
				subController = Controller.PATROL.ordinal();
				
				if(foundIntruder()) {
					currentState = State.PURSUE_INTRUDER;
				}
				
				break;
			case PURSUE_INTRUDER:
				subController = Controller.INTRUDER.ordinal();
				
				if(foundIntruder()) {
					lastIntruderTime = timestep;
					alertNearbyDrones();
				} else if(timestep - lastIntruderTime > ALERT_TIMEOUT) {
					currentState = State.PATROL;
				}
				
				break;
			case RECHARGE:
				drone.setActiveWaypoint(Waypoint.getWaypoints(robot).get(0));
				subController = Controller.WAYPOINT.ordinal();
				
				if(currentBattery >= 1.0) {
					currentState = State.GO_TO_AREA;
					drone.setActiveWaypoint(null);
				} else {
					double distance = CoordinateUtilities.distanceInMeters(drone.getActiveWaypoint().getLatLon(),drone.getGPSLatLon());

					if(distance < BASE_DISTANCE_THRESHOLD) {
						robot.setMotorSpeeds(0, 0);
					}
				}
				break;
		}
		
		chooseSubController(subController,timestep);
	}
	
	protected void chooseSubController(int output, double time) {
		if(!subControllers.isEmpty()) {
			
			if(output != currentSubController) {
				subControllers.get(currentSubController).cleanUp();
				currentSubController = output;
				subControllers.get(currentSubController).start();
			}
			
			subControllers.get(currentSubController).step(time);
		}
	}
	
	protected void updateEnergy(double timestep) {
		if(currentState != State.RECHARGE) {
			currentBattery-= 1/BATTERY_LIFE;
		} else {
			currentBattery+= 1/BATTERY_LIFE;
		}
	}
	
	protected boolean foundIntruder() {
		//TODO
		return false;
	}
	
	protected void alertNearbyDrones() {
		//TODO
	}
	
	protected Waypoint chooseWaypointInGeoFence() {

		Waypoint result = null;
		GeoFence fence = getGeoFence();
		
		if(fence != null) {
			
			LinkedList<Waypoint> wps = fence.getWaypoints();
			
			LatLon coord = wps.getFirst().getLatLon();
			
			double minLat = coord.getLat();
			double maxLat = coord.getLat();
			double minLon = coord.getLon();
			double maxLon = coord.getLon();
			
			for(int i = 1 ; i < wps.size() ; i++) {
				LatLon wpLatLon = wps.get(i).getLatLon();
				
				minLat = Math.min(minLat, wpLatLon.getLat());
				maxLat = Math.max(maxLat, wpLatLon.getLat());
				
				minLon = Math.min(minLon, wpLatLon.getLon());
				maxLon = Math.max(minLon, wpLatLon.getLon());
			}
			
			int tries = 0;
			boolean success = false;
			
			do {
				
				double rLat = minLat+(maxLat-minLat)*Math.random();
				double rLon = minLon+(maxLon-minLon)*Math.random();
				
				LatLon rLatLon = new LatLon(rLat,rLon);
				
				if(insideBoundary(rLatLon)) {
					success = true;
					result = new Waypoint("waypointX", rLatLon);
				}
				
			} while(!success && ++tries < 100);
		
		}
		return result;
	}
	
	private GeoFence getGeoFence() {
		for(Entity e : robot.getEntities()) {
			if(e instanceof GeoFence) {
				return (GeoFence)e;
			}
		}
		return null;
	}
	
	private boolean insideBoundary(LatLon latLon) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		
		Vector2d vector = CoordinateUtilities.GPSToCartesian(latLon);
		
		for(Line l : getGeoFenceLines()) {
			if(l.intersectsWithLineSegment(vector, new Vector2d(0,-Integer.MAX_VALUE)) != null)
				count++;
		}
		return count % 2 != 0;
	}
	
	private ArrayList<Line> getGeoFenceLines() {
		
		GeoFence fence = getGeoFence();
		
		if(fence != null) {
			ArrayList<Line> lines = new ArrayList<Line>();
			
			LinkedList<Waypoint> waypoints = fence.getWaypoints();
			
			for(int i = 1 ; i < waypoints.size() ; i++) {
				
				Waypoint wa = waypoints.get(i-1);
				Waypoint wb = waypoints.get(i);
				
				lines.add(getLine(wa,wb));
			}
			
			//loop around
			Waypoint wa = waypoints.get(waypoints.size()-1);
			Waypoint wb = waypoints.get(0);
			
			lines.add(getLine(wa,wb));
			
			return lines;
		}
		return null;
	}
	
	private Line getLine(Waypoint wa, Waypoint wb) {
		Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());
		
		return new Line(va.getX(), va.getY(), vb.getX(), vb.getY());
	}
	
	@Override
	public void cleanUp() {
		subControllers.get(currentSubController).cleanUp();
		robot.setMotorSpeeds(0, 0);
		subControllers.clear();
	}
}