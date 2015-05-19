package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.ThymioEntity;
import commoninterface.utils.CIArguments;

public class ThymioCISensor extends ThymioConeTypeCISensor {

	public ThymioCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Object e) {
		return e instanceof ThymioEntity;
	}

}
