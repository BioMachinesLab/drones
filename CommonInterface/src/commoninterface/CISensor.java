package commoninterface;

import java.io.Serializable;

import commoninterface.utils.CIArguments;

public abstract class CISensor implements Serializable {

	protected int id = 0;
	protected RobotCI robot;

	public CISensor(int id, RobotCI robot, CIArguments args) {
		super();
		this.id = id;
		this.robot = robot;
	}

	public abstract int getNumberOfSensors();

	public abstract double getSensorReading(int sensorNumber);

	public abstract void update(double time, Object[] entities);

	public static CISensor getSensor(RobotCI robot, String name,
			CIArguments arguments) {
		int id = arguments.getArgumentAsIntOrSetDefault("id", 0);
		return (CISensor) CIFactory.getInstance(name, id, robot, arguments);
	}

	public int getId() {
		return id;
	}

}
