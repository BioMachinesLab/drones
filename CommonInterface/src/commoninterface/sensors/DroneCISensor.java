package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.objects.Entity;
import commoninterface.objects.RobotLocation;
import commoninterface.utils.CIArguments;

public class DroneCISensor extends ConeTypeCISensor{
	

	public DroneCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Entity e) {
		return e instanceof RobotLocation;
	}

}
