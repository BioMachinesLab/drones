package simpletestbehaviors;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.LedState;
import commoninterface.RobotCI;
import commoninterface.objects.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class GoToWaypointCIBehavior extends CIBehavior {

	private double distanceTolerance = 3;
	private double angleTolerance = 10;
	private AquaticDroneCI drone;
	
	public GoToWaypointCIBehavior(CIArguments args, RobotCI drone) {
		super(args, drone);
		this.drone = (AquaticDroneCI)drone;
		
		distanceTolerance = args.getArgumentAsDoubleOrSetDefault("distancetolerance", distanceTolerance);
		angleTolerance = args.getArgumentAsDoubleOrSetDefault("angletolerance", angleTolerance);
	}
	
	@Override
	public void step(double timestep) {
		
		ArrayList<Waypoint> waypoints = Waypoint.getWaypoints(drone);
		
		if(waypoints.size() == 0) {
			drone.setLed(0, LedState.OFF);
			drone.setMotorSpeeds(0, 0);
			return;
		}
		
		double currentOrientation = drone.getCompassOrientationInDegrees();
		double coordinatesAngle = CoordinateUtilities.angleInDegrees(drone.getGPSLatLon(),
				waypoints.get(0).getLatLon());
		
		double currentDistance = CoordinateUtilities.distanceInMeters(drone.getGPSLatLon(),
				waypoints.get(0).getLatLon());

		double difference = currentOrientation - coordinatesAngle;
		
		difference%=360;
		
		if(difference > 180){
			difference = -((180 -difference) + 180);
		}
		
		if(Math.abs(currentDistance) < distanceTolerance){
			System.out.println("stop");
			drone.setLed(0, LedState.OFF);
			drone.setMotorSpeeds(0, 0);
		}else{
			if (Math.abs(difference) <= angleTolerance) {
				System.out.println("front");
				drone.setLed(0, LedState.ON);
				drone.setMotorSpeeds(1.0, 1.0);
			}else {
				drone.setLed(0, LedState.BLINKING);
				if (difference > 0) {
					System.out.println("left");
					drone.setMotorSpeeds(0, 0.2);
				} else {
					System.out.println("right");
					drone.setMotorSpeeds(0.2, 0);
				}
			}
		}
	}
	
	@Override
	public void cleanUp() {
		drone.setLed(0, LedState.OFF);
		drone.setMotorSpeeds(0, 0);
	}
}