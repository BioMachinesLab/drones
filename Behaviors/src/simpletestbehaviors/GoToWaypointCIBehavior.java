package simpletestbehaviors;

import objects.Waypoint;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;

public class GoToWaypointCIBehavior extends CIBehavior {

	private double distanceTolerance = 3;
	private double angleTolerance = 20;
	
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
		double currentOrientation = drone.getCompassOrientationInDegrees();
		double coordinatesAngle = calculateCoordinatesAngle();
		
		double difference = coordinatesAngle - currentOrientation;
		
		double currentDistance = calculateDistance();
	
		if(currentDistance > distanceTolerance) {
			if (Math.abs(difference) <= angleTolerance) {
				drone.setLed(0, AquaticDroneCI.LedState.ON);
				drone.setMotorSpeeds(0.1, 0.1);
//				getLogger().logMessage("Straight ahead, skipper!");
			} else {
				drone.setLed(0, AquaticDroneCI.LedState.BLINKING);
				if (difference < 180) {
//					getLogger().logMessage("Turning left "+difference);				
					drone.setMotorSpeeds(-0.1, 0.1);
				} else {
//					getLogger().logMessage("Turning right "+difference);
					drone.setMotorSpeeds(0.1, -0.1);
				}
			}
		} else {
			getLogger().logMessage("Reached waypoint");				
			drone.setLed(0, AquaticDroneCI.LedState.OFF);
			drone.setMotorSpeeds(0, 0);
		}
	}
	
	private double calculateCoordinatesAngle() {
		
		double angle = 0;
		
		if(drone.getWaypoints().size() > 0) {
			
			Waypoint destination = drone.getWaypoints().get(0);
			
			double lat1 = drone.getGPSLatitude();
			double lon1 = drone.getGPSLongitude();
			
			double lat2 = destination.getLatitude();
			double lon2 = destination.getLongitude();
		
			double dy = lat2 - lat1;
			double dx = Math.cos(Math.PI/180*lat1)*(lon2 - lon1);
			angle = Math.atan2(dy, dx);
		
			//0 is to the right. By subtracting 90, we are making 0 toward north
			return -(Math.toDegrees(angle) - 90);
		}
		
		return 0;
	}
	
	private double calculateDistance() {
		
		double distance = 0;
		if(drone.getWaypoints().size() > 0) {
		
			Waypoint destination = drone.getWaypoints().get(0);
			
			double lat1 = drone.getGPSLatitude();
			double lon1 = drone.getGPSLongitude();
			
			double lat2 = destination.getLatitude();
			double lon2 = destination.getLongitude();
			
			double R = 6371000; // earth's radius in meters
			double r1 = Math.toRadians(lat1);
			double r2 = Math.toRadians(lat2);
			double latDelta = Math.toRadians(lat2-lat1);
			double lonDelta = Math.toRadians(lon2-lon1);
	
			double a = Math.sin(latDelta/2) * Math.sin(latDelta/2) + Math.cos(r1) * Math.cos(r2) * Math.sin(lonDelta/2) * Math.sin(lonDelta/2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	
			distance = R * c;
		}
		
		return distance;
	}
	
	@Override
	public void cleanUp() {
		drone.setLed(0, AquaticDroneCI.LedState.OFF);
		drone.setMotorSpeeds(0, 0);
	}
}