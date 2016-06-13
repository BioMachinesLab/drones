package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.entities.ThymioEntity;
import commoninterface.utils.CIArguments;

public class ThymioCISensor extends ThymioConeTypeCISensor {
	private static final long serialVersionUID = -4196570717856977376L;

	public ThymioCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Object e) {
		return e instanceof ThymioEntity;
	}

}
