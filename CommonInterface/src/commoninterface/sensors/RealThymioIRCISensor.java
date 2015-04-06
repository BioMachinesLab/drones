package commoninterface.sensors;

import java.util.ArrayList;
import java.util.List;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import commoninterface.objects.Entity;
import commoninterface.utils.CIArguments;

public class RealThymioIRCISensor extends CISensor {

	private ThymioCI thymio;
	private List<Short> readings;
	
	public RealThymioIRCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		thymio = (ThymioCI)robot;
	}

	@Override
	public int getNumberOfSensors() {
		return 7;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return readings.get(sensorNumber);
	}

	@Override
	public void update(double time, ArrayList<Entity> entities) {
		readings = thymio.getInfraredSensorsReadings();
	}

}
