package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.objects.Entity;
import commoninterface.objects.PreyEntity;
import commoninterface.utils.CIArguments;

public class PreyCISensor extends ThymioConeTypeCISensor {

	public PreyCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Object e) {
		return e instanceof PreyEntity;
	}

}
