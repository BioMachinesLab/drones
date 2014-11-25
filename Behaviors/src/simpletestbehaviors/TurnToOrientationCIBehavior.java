package simpletestbehaviors;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;

public class TurnToOrientationCIBehavior extends CIBehavior {

	private double targetOrientation = 0;
	private double tolerance         = 40;
	
	public TurnToOrientationCIBehavior(String[] args, AquaticDroneCI drone, CILogger logger) {
		super(args, drone, logger);
		
		//TODO: Find a more elegant way to parse arguments
		for (String arg : args) {
			if (arg.startsWith("target=")) {
				targetOrientation = Double.parseDouble(arg.substring(arg.indexOf("=") + 1));
				logger.logMessage("Setting target orientation to: " + targetOrientation);
			}
			if (arg.startsWith("tolerance=")) {
				tolerance = Double.parseDouble(arg.substring(arg.indexOf("=") + 1));
				logger.logMessage("Setting tolerance to: " + tolerance);
			}
		}
	}

	@Override
	public void step() {
		double currentOrientation = drone.getCompassOrientationInDegrees();		
//		getLogger().logMessage("Current orientation: " + currentOrientation);
		double difference = currentOrientation - targetOrientation;
//		getLogger().logMessage("Current difference: " + difference);
		
		difference%=360;
		
		System.out.println("[diff] "+difference);
		
		if (Math.abs(difference) <= tolerance) {
			drone.setLed(0, AquaticDroneCI.LedState.ON);
			drone.setMotorSpeeds(0, 0);
//			getLogger().logMessage("Within tolerance, stopping");
		}
		else {
			drone.setLed(0, AquaticDroneCI.LedState.BLINKING);
			if (difference > 0) {
//				getLogger().logMessage("Turning left");				
				drone.setMotorSpeeds(-0.1, 0.1);
			} else {
//				getLogger().logMessage("Turning right");
				drone.setMotorSpeeds(0.1, -0.1);
			}
		}
	}
	
	@Override
	public void cleanUp() {
		drone.setLed(0, AquaticDroneCI.LedState.OFF);
		drone.setMotorSpeeds(0, 0);
	}
}
