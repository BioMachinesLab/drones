package drone;

import java.util.ArrayList;
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
	
	protected static double GO_BACK_BATTERY = 0.1;
	protected static double ALERT_TIMEOUT = 30*10;//30 sec
	
	protected CIArguments args;
	protected AquaticDroneCI drone;

	protected int waypointDistanceThreshold = 3;
	protected int baseDistanceThreshold = 5; //5 meters
	protected int batteryLife = 8*60*10; //8 min in timesteps
	protected State currentState = State.GO_TO_AREA;
	protected double currentBattery = 1.0;
	protected double lastIntruderTime;
	protected int currentSubController = 0;
	protected boolean share = false;
	protected boolean lampedusa = false;
	protected Waypoint baseWP;
	protected Vector2d baseWPcartesian;
	protected ArrayList<Line> lines;
	protected int commRange = 40;
	
	protected int stop = 0;//10*60*10;//10 min mission
	
	protected String description = "";
	
	protected ArrayList<CIBehavior> subControllers = new ArrayList<CIBehavior>();
	
	protected int onboardRange = 20;
	
	protected double startingBattery = 0;
	
	protected int numberRobotsPursuing = 3;
	
	public MissionController(CIArguments args, RobotCI robot) {
		super(args, robot);
		this.args = args;
		this.drone = (AquaticDroneCI)robot;
		if(args.getArgumentIsDefined("ip"+drone.getNetworkAddress())) {
			this.currentBattery = args.getArgumentAsDouble("ip"+drone.getNetworkAddress());
		} else {
			this.currentBattery = 0.5 + Math.random()*0.5;//50% to 100%
		}
		
		commRange = args.getArgumentAsIntOrSetDefault("commrange", commRange);
		onboardRange = args.getArgumentAsIntOrSetDefault("onboardrange", onboardRange);
		stop = args.getArgumentAsIntOrSetDefault("stop", stop);
		share = args.getFlagIsTrue("share");
		lampedusa = args.getFlagIsTrue("lampedusa");
		batteryLife = args.getArgumentAsIntOrSetDefault("batterylife", batteryLife);
		baseDistanceThreshold = args.getArgumentAsIntOrSetDefault("basedistancethreshold", baseDistanceThreshold);
		waypointDistanceThreshold = args.getArgumentAsIntOrSetDefault("waypointdistancethreshold", waypointDistanceThreshold);
		numberRobotsPursuing = args.getArgumentAsIntOrSetDefault("numberrobotspursuing",numberRobotsPursuing);
		
		startingBattery = currentBattery;
		
		description+="starting battery="+currentBattery+";";
		
		//TODO remove this debug line
		stop = 0;
	}
	
	@Override
	public void start() {
		for(Controller c : Controller.values()) {
			CIArguments a = new CIArguments(args.getArgumentAsString(c.toString()));
			subControllers.add(new ControllerCIBehavior(a, drone));
		}
		
		baseWP = Waypoint.getWaypoints(drone).get(0);
		baseWPcartesian = CoordinateUtilities.GPSToCartesian(baseWP.getLatLon());
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
					Waypoint wp = chooseWaypointInGeoFence(timestep);
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
					
					if(distance < waypointDistanceThreshold) {
						
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

					if(distance < baseDistanceThreshold) {
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
			currentBattery-= 1.0/batteryLife;
		} else {
			double distance = CoordinateUtilities.distanceInMeters(drone.getActiveWaypoint().getLatLon(),drone.getGPSLatLon());
			
			if(distance < baseDistanceThreshold)
//				currentBattery+= (1.0/BATTERY_LIFE)*5;
				currentBattery = 1.0;
			else
				currentBattery-= 1.0/batteryLife;	
		}
		
	}
	
	protected boolean eligibleToPursue(LatLon intruderPos) {
		
		ArrayList<RobotLocation> locations = RobotLocation.getDroneLocations(drone);
		
		int position = 0;
		double droneDist = drone.getGPSLatLon().distanceInMeters(intruderPos);

//		if(drone.getNetworkAddress().endsWith("0"))
//			System.out.println(droneDist);
		
		if(droneDist > commRange) //comm range
			return false;
		
		for(RobotLocation loc : locations) {
			
			if(loc.getDroneType() == DroneType.ENEMY)
				continue;
			
			double dist = loc.getLatLon().distanceInMeters(intruderPos);
			if(dist < droneDist)
				position++;
			if(position >= numberRobotsPursuing)
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
		}
		ArrayList<RobotLocation> robotLocations = RobotLocation.getDroneLocations(drone);
		
		for(RobotLocation loc : robotLocations) {
			if(loc.getDroneType() == DroneType.ENEMY && loc.getLatLon().distanceInMeters(drone.getGPSLatLon()) < onboardRange) {
				return loc.getLatLon();
			}
		}
		
		return null;
		
	}
	
	protected void alertNearbyDrones() {
		//This is done automatically by the sensor that shares
	}
	
	protected Waypoint chooseWaypointInGeoFence(double timestep) {

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
			
			Random r = new Random((long)(Math.pow(startingBattery*1000,2)));
			
			do {
				
				double x = minX+(maxX-minX)*r.nextDouble();
				double y = minY+(maxY-minY)*r.nextDouble();
				
				if(!lampedusa || Math.signum(baseWPcartesian.x) == Math.signum(x)) {
				
					LatLon rLatLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x,y));
					
					if(insideBoundary(rLatLon)) {
						success = true;
						result = new Waypoint("waypointX", rLatLon);
					}
				}
				
			} while(!success && ++tries < 1000);
			
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
		
		if(lines == null) {
		
			GeoFence fence = getGeoFence();
			
			if(fence != null) {
				lines = new ArrayList<Line>();
				
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
		} else {
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
		
		double droneDist = drone.getGPSLatLon().distanceInMeters(intruderPosition);
		return droneDist <= onboardRange;
	}
	
	public boolean seeingEnemyShared() {
		
		LatLon intruderPosition = intruderPosition();
		
		if(intruderPosition == null || !insideBoundary(intruderPosition))
			return false;
		
		double droneDist = drone.getGPSLatLon().distanceInKM(intruderPosition);
		return droneDist*1000 <= commRange;
	}
	
}