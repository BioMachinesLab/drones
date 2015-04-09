package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.objects.Entity;
import commoninterface.objects.ThymioEntity;
import commoninterface.utils.CIArguments;

public class ThymioCISensor extends ThymioConeTypeCISensor {

	public ThymioCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Entity e) {
		return e instanceof ThymioEntity;
	}

}
