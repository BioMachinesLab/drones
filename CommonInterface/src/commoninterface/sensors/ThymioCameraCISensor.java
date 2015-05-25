package commoninterface.sensors;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import commoninterface.utils.CIArguments;

public class ThymioCameraCISensor extends CISensor {

	private ThymioCI thymio;
	private double[] readings;
	
	public ThymioCameraCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		thymio = (ThymioCI)robot;
	}

	@Override
	public int getNumberOfSensors() {
		return 2;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}

	@Override
	public void update(double time, Object[] entities) {
		readings = thymio.getCameraReadings();
	}

}
