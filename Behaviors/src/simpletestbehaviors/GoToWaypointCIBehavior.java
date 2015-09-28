package simpletestbehaviors;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.LedState;
import commoninterface.RobotCI;
import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public class GoToWaypointCIBehavior extends CIBehavior {

	private double distanceTolerance = 3;
	private double angleTolerance = 10;
	private double wait = 0;
	private AquaticDroneCI drone;
	private boolean waiting = false;
	private Waypoint wp;
	private boolean defineWPs = false;
	
	private double offsetX = 0;
	private double offsetY = 0;
	private double distance = 100;
	
	private int currentWP = 0;
	private Waypoint prevWP = null;
	private double timeOffset = 0;
	private CIBehavior ann;
	
	public GoToWaypointCIBehavior(CIArguments args, RobotCI drone) {
		super(args, drone);
		this.drone = (AquaticDroneCI)drone;
		
		distanceTolerance = args.getArgumentAsDoubleOrSetDefault("distancetolerance", distanceTolerance);
		angleTolerance = args.getArgumentAsDoubleOrSetDefault("angletolerance", angleTolerance);
		wait = args.getArgumentAsDoubleOrSetDefault("wait", wait);
		defineWPs = args.getFlagIsTrue("definewps");
		
		if(args.getArgumentIsDefined("ann")) {
			CIArguments annArgs = new CIArguments(args.getArgumentAsString("ann"));
			ann = new ControllerCIBehavior(annArgs, drone);
			ann.start();
		}
	}

	public void start() {		
		if(defineWPs) {
			ArrayList<GeoFence> fences = GeoFence.getGeoFences(drone);
			
			if(fences.isEmpty())
				return;
			
			GeoFence fence = GeoFence.getGeoFences(drone).get(0);
			drone.getEntities().clear();
			drone.getEntities().add(fence);
			
			distance = fence.getWaypoints().get(0).getLatLon().distance(fence.getWaypoints().get(1).getLatLon())*1000;
			
			offsetX = CoordinateUtilities.GPSToCartesian(fence.getWaypoints().get(0).getLatLon()).x+distance/2;
			offsetY = CoordinateUtilities.GPSToCartesian(fence.getWaypoints().get(0).getLatLon()).y+distance/2;
			
			ArrayList<Waypoint> wps = getWPs();
			
			drone.getEntities().addAll(wps);
			drone.setActiveWaypoint(wps.get(0));
		}
	}
	
	@Override
	public void step(double timestep) {
		
		if(waiting) {
			
			int c = currentWP/2;
			
			if(currentWP % 2 == 0  || (timestep-timeOffset) >= c*wait) {
				
				waiting = false;
				prevWP = wp;
				wp = null;
			}
		}
		
		if(wp == null) {
			drone.setLed(0, LedState.OFF);
			drone.setMotorSpeeds(0, 0);
			wp = drone.getActiveWaypoint();
			if(wp != prevWP) {
				if(currentWP == 1) {
					timeOffset = timestep;
				}
				currentWP++;
			}
			if(wp == null)
				return;
		}
		
		if(ann != null) {
			ann.step(timestep);
			return;
		}
		
		
		double currentOrientation = drone.getCompassOrientationInDegrees();
		double coordinatesAngle = CoordinateUtilities.angleInDegrees(drone.getGPSLatLon(),
				wp.getLatLon());
		
		double currentDistance = CoordinateUtilities.distanceInMeters(drone.getGPSLatLon(),
				wp.getLatLon());

		double difference = currentOrientation - coordinatesAngle;
		
		difference%=360;
		
		if(difference > 180){
			difference = -((180 -difference) + 180);
		}
		
		if(Math.abs(currentDistance) < distanceTolerance){
			drone.setLed(0, LedState.OFF);
			drone.setMotorSpeeds(0, 0);
			waiting = true;
		}else{
			if (Math.abs(difference) <= angleTolerance) {
				drone.setLed(0, LedState.ON);
				
				double reduction = 1-(1.0*(Math.abs(difference)/angleTolerance));
				
				
				if(difference < 0)
					drone.setMotorSpeeds(1.0, reduction);
				else if(difference > 0)
					drone.setMotorSpeeds(reduction, 1.0);
				else
					drone.setMotorSpeeds(1.0, 1.0);
			}else {
				drone.setLed(0, LedState.BLINKING);
				if (difference > 0) {
					drone.setMotorSpeeds(0, 0.5);
				} else {
					drone.setMotorSpeeds(0.5, 0);
				}
			}
		}
	}
	
	@Override
	public void cleanUp() {
		drone.setLed(0, LedState.OFF);
		drone.setMotorSpeeds(0, 0);
	}
	
	private void addNode(ArrayList<Waypoint> wps, double x, double y) {

        x *= distance/2;
        y *= distance/2;
        
        System.out.println(x+" "+y);
        
        x+=offsetX;
        y+=offsetY;
        y-=distance;
        
        wps.add(new Waypoint("wp", CoordinateUtilities.cartesianToGPS(new commoninterface.mathutils.Vector2d(x, y))));
    }
	
	public ArrayList<Waypoint> getWPs() {
		ArrayList<Waypoint> wps = new ArrayList<Waypoint>();
		
		addNode(wps,-1.2,0.75);
		
		addNode(wps,1.2,0.75);
		addNode(wps,1.2,0.45);
		
		addNode(wps,-1.2,0.45);
		addNode(wps,-1.2,0.15);
		
		addNode(wps,1.2,0.15);
		addNode(wps,1.2,-0.15);
		
		addNode(wps,-1.2,-0.15);
		addNode(wps,-1.2,-0.45);
		
		addNode(wps,1.2,-0.45);
		addNode(wps,1.2,-0.75);
		
		addNode(wps,-1.2,-0.75);	
		
		return wps;
	
	}
	
}