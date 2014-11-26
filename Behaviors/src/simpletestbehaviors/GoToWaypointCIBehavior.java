package simpletestbehaviors;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;
import commoninterface.LedState;
import commoninterface.utils.CoordinateUtilities;

public class GoToWaypointCIBehavior extends CIBehavior {

	private double distanceTolerance = 3;
	private double angleTolerance = 10;
	
	public GoToWaypointCIBehavior(String[] args, AquaticDroneCI drone, CILogger logger) {
		super(args, drone, logger);
		
		//TODO: Find a more elegant way to parse arguments
		for (String arg : args) {
			if (arg.startsWith("distancetolerance=")) {
				distanceTolerance = Double.parseDouble(arg.substring(arg.indexOf("=") + 1));
				logger.logMessage("Setting distancetolerance to: " + distanceTolerance);
			}
			if (arg.startsWith("angletolerance=")) {
				angleTolerance = Double.parseDouble(arg.substring(arg.indexOf("=") + 1));
				logger.logMessage("Setting angletolerance to: " + angleTolerance);
			}
		}
	}

	@Override
	public void step() {
		
		if(drone.getWaypoints().size() == 0)
			return;
		
		double currentOrientation = drone.getCompassOrientationInDegrees();
		double coordinatesAngle = CoordinateUtilities.angleInDegrees(
				drone.getGPSLatitude(), drone.getGPSLongitude(),
				drone.getWaypoints().get(0).getLatitude(),
				drone.getWaypoints().get(0).getLongitude());
		
		double currentDistance = CoordinateUtilities.distanceInMeters(
				drone.getGPSLatitude(), drone.getGPSLongitude(),
				drone.getWaypoints().get(0).getLatitude(),
				drone.getWaypoints().get(0).getLongitude());

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
				drone.setMotorSpeeds(0.1, 0.1);
			}else {
				drone.setLed(0, LedState.BLINKING);
				if (difference > 0) {
					System.out.println("left");
					drone.setMotorSpeeds(0, 0.1);
				} else {
					System.out.println("right");
					drone.setMotorSpeeds(0.1, 0);
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