package simpletestbehaviors;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.LedState;
import commoninterface.RobotCI;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public class GoToWaypointCIBehavior extends CIBehavior {

	private double distanceTolerance = 3;
	private double angleTolerance = 10;
	private double wait = 0;
	private AquaticDroneCI drone;
	private double currentWait = 0;
	private boolean waiting = false;
	private Waypoint wp;
	
	public GoToWaypointCIBehavior(CIArguments args, RobotCI drone) {
		super(args, drone);
		this.drone = (AquaticDroneCI)drone;
		
		distanceTolerance = args.getArgumentAsDoubleOrSetDefault("distancetolerance", distanceTolerance);
		angleTolerance = args.getArgumentAsDoubleOrSetDefault("angletolerance", angleTolerance);
		wait = args.getArgumentAsDoubleOrSetDefault("wait", wait);
	}
	
	@Override
	public void step(double timestep) {
		
		if(waiting && currentWait++ > wait) {
			waiting = false;
			currentWait = 0;
			wp = null;
		}
		
		if(wp == null) {
			drone.setLed(0, LedState.OFF);
			drone.setMotorSpeeds(0, 0);
			wp = drone.getActiveWaypoint();
			if(wp == null)
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
					drone.setMotorSpeeds(0, 0.1);
				} else {
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