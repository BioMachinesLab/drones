package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.entities.ThymioSharedEntity;
import commoninterface.utils.CIArguments;

public class SharedThymioCISensor extends ThymioConeTypeCISensor {
	private static final long serialVersionUID = -7683273067201412279L;

	public SharedThymioCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Object e) {
		return e instanceof ThymioSharedEntity;
	}

}
