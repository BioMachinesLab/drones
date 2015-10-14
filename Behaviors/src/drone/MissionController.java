package drone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import commoninterface.AquaticDroneCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.SharedDroneLocation;
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
	
	protected static long BATTERY_LIFE = 8*60*10; //8 min in timesteps
	protected static double GO_BACK_BATTERY = 0.1;
	protected static double WAYPOINT_DISTANCE_THRESHOLD = 3; //3 meters
	protected static double BASE_DISTANCE_THRESHOLD = 5; //5 meters 
	protected static double ALERT_TIMEOUT = 30*10;//30 sec
	
	protected CIArguments args;
	protected AquaticDroneCI drone;

	protected State currentState = State.GO_TO_AREA;
	protected double currentBattery = 1.0;
	protected double lastIntruderTime;
	protected int currentSubController = 0;
	protected boolean share = false;
	
	protected int stop = 10*60*10;//10 min mission
	
	protected String description = "";
	
	protected ArrayList<CIBehavior> subControllers = new ArrayList<CIBehavior>();
	
	protected int onboardRange = 20;
	
	public MissionController(CIArguments args, RobotCI robot) {
		super(args, robot);
		this.args = args;
		this.drone = (AquaticDroneCI)robot;
		if(args.getArgumentIsDefined("ip"+drone.getNetworkAddress())) {
			this.currentBattery = args.getArgumentAsDouble("ip"+drone.getNetworkAddress());
		} else {
			this.currentBattery = 0.5 + Math.random()*0.5;//50% to 100%
		}
		
		onboardRange = args.getArgumentAsIntOrSetDefault("onboardrange", onboardRange);
		stop = args.getArgumentAsIntOrSetDefault("stop", stop);
		share = args.getFlagIsTrue("share");
		
		description+="starting battery="+currentBattery+";";
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
		
//		System.out.println(currentState);
		
		if(stop != 0 && timestep > stop) {
			robot.setMotorSpeeds(0, 0);
			return;
		}
		
		int subController = 0;
		
		updateEnergy(timestep);
		
		boolean skipSubController = false;
		
		if(currentBattery < GO_BACK_BATTERY) {
			currentState = State.RECHARGE;
		}
		
//		if(timestep % 10 == 0 && drone.getNetworkAddress().endsWith("0"))
//			System.out.println(currentState+" "+currentBattery);
		
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
				
				LatLon intruderPos2 = intruderPosition();
				
				if(intruderPos2 != null && insideBoundary(drone.getGPSLatLon()) && insideBoundary(intruderPos2) && eligibleToPursue(intruderPos2)) {
					currentState = State.PURSUE_INTRUDER;
				}
				
				break;
			case PATROL:
				subController = Controller.PATROL.ordinal();
				
				LatLon intruderPos = intruderPosition();
				
				if(intruderPos != null && insideBoundary(intruderPos) && eligibleToPursue(intruderPos)) {
					currentState = State.PURSUE_INTRUDER;
				}
				
				break;
			case PURSUE_INTRUDER:
				subController = Controller.INTRUDER.ordinal();
				
				intruderPos = intruderPosition();
				
//				if(robot.getNetworkAddress().endsWith("103"))
//					System.out.println(intruderPos);
				
				if(intruderPos != null) {
					
					if(insideBoundary(intruderPos) && eligibleToPursue(intruderPos)) {
						lastIntruderTime = timestep;
						alertNearbyDrones();
					} else {
						currentState = State.PATROL;
					}
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
						skipSubController = true;
					}
				}
				break;
		}
		
		if(!skipSubController)
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
			currentBattery-= 1.0/BATTERY_LIFE;
		} else {
			double distance = CoordinateUtilities.distanceInMeters(drone.getActiveWaypoint().getLatLon(),drone.getGPSLatLon());
			
			if(distance < BASE_DISTANCE_THRESHOLD)
//				currentBattery+= (1.0/BATTERY_LIFE)*5;
				currentBattery = 1.0;
			else
				currentBattery-= 1.0/BATTERY_LIFE;	
		}
		
	}
	
	protected boolean eligibleToPursue(LatLon intruderPos) {
		
		ArrayList<RobotLocation> locations = RobotLocation.getDroneLocations(drone);
		
		int position = 0;
		double droneDist = drone.getGPSLatLon().distance(intruderPos);

//		if(drone.getNetworkAddress().endsWith("0"))
//			System.out.println(droneDist);
		
		if(droneDist*1000 > 40) //comm range
			return false;
		
		for(RobotLocation loc : locations) {
			
			if(loc.getDroneType() == DroneType.ENEMY)
				continue;
			
			double dist = loc.getLatLon().distance(intruderPos);
			if(dist < droneDist)
				position++;
			if(position >= 3)
				return false;
		}
		
		return true;
	}
	
	protected LatLon intruderPosition() {
		
		if(share) {
			LinkedList<SharedDroneLocation> locations = SharedDroneLocation.getSharedDroneLocations(drone);
			
			for(SharedDroneLocation loc : locations) {
				if(loc.getDroneType() == DroneType.ENEMY) {
					return loc.getLatLon();
				}
			}
		} else {
			ArrayList<RobotLocation> locations = RobotLocation.getDroneLocations(drone);
			
			for(RobotLocation loc : locations) {
				if(loc.getDroneType() == DroneType.ENEMY && loc.getLatLon().distance(drone.getGPSLatLon())*1000 < onboardRange) {
//					if(robot.getNetworkAddress().endsWith("103"))
//						System.out.println(loc.getLatLon().distance(drone.getGPSLatLon())*1000+" < "+onboardRange);
					return loc.getLatLon();
				}
			}
		}
		
		return null;
		
	}
	
	protected void alertNearbyDrones() {
		//This is done automatically by the sensor that shares
	}
	
	protected Waypoint chooseWaypointInGeoFence() {

		Waypoint result = null;
		GeoFence fence = getGeoFence();
		
		if(fence != null) {
			
			LinkedList<Waypoint> wps = fence.getWaypoints();
			
			Vector2d coord = CoordinateUtilities.GPSToCartesian(wps.getFirst().getLatLon());
			
			double minX = coord.x;
			double maxX = coord.x;
			double minY = coord.y;
			double maxY = coord.y;
			
			for(int i = 1 ; i < wps.size() ; i++) {
				Vector2d wpCoord = CoordinateUtilities.GPSToCartesian(wps.get(i).getLatLon());
				
				minX = Math.min(minX, wpCoord.x);
				maxX = Math.max(maxX, wpCoord.x);
				
				minY = Math.min(minY, wpCoord.y);
				maxY = Math.max(maxY, wpCoord.y);
			}
			
			int tries = 0;
			boolean success = false;
			
			//diversify the seed, they are all almost the same with [0,1]
			Random r = new Random((long)(Math.pow(currentBattery*1000,2)));
			
			do {
				
				double x = minX+(maxX-minX)*r.nextDouble();
				double y = minY+(maxY-minY)*r.nextDouble();
				
				LatLon rLatLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x,y));
				
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
	
	public boolean insideBoundary(LatLon latLon) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		
		Vector2d vector = CoordinateUtilities.GPSToCartesian(latLon);

		for (Line l : getGeoFenceLines()) {
			if (l.intersectsWithLineSegment(vector, new Vector2d(0,
					-Integer.MAX_VALUE)) != null)
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
		if(!subControllers.isEmpty())
			subControllers.get(currentSubController).cleanUp();
		robot.setMotorSpeeds(0, 0);
		subControllers.clear();
		
		ArrayList<Waypoint> wps = Waypoint.getWaypoints(drone);
		if(wps != null && !wps.isEmpty()) {
			drone.setActiveWaypoint(wps.get(0));
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + description;
	}
	
	public State getCurrentState() {
		return currentState;
	}
	
	public boolean seeingEnemyDirectly() {
		
		LatLon intruderPosition = intruderPosition();
		
		if(intruderPosition == null || !insideBoundary(intruderPosition))
			return false;
		
		double droneDist = drone.getGPSLatLon().distance(intruderPosition);
		return droneDist*1000 <= 20;
	}
	
	public boolean seeingEnemyShared() {
		
		LatLon intruderPosition = intruderPosition();
		
		if(intruderPosition == null || !insideBoundary(intruderPosition))
			return false;
		
		double droneDist = drone.getGPSLatLon().distance(intruderPosition);
		return droneDist*1000 <= 40;
	}
	
}