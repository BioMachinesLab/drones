package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class CompassCISensor extends CISensor {

	private AquaticDroneCI drone;
	private double reading = 0;
	private boolean normalize = false;

	public CompassCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		drone = (AquaticDroneCI) robot;
		normalize = args.getArgumentAsIntOrSetDefault("normalize", 0) == 1;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		if (normalize) {
			double val = reading % 360;
			return val /= 360;
		} else {
			return reading;
		}
	}

	@Override
	public void update(double time, Object[] entities) {
		reading = drone.getCompassOrientationInDegrees();
	}

	@Override
	public int getNumberOfSensors() {
		return 1;
	}

}
