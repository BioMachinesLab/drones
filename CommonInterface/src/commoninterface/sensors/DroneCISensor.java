package commoninterface.sensors;

import objects.DroneLocation;
import objects.Entity;

import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class DroneCISensor extends ConeTypeCISensor{
	

	public DroneCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Entity e) {
		return e instanceof DroneLocation;
	}

}
