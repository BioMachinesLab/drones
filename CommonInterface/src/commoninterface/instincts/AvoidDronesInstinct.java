package commoninterface.instincts;

import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.utils.CIArguments;

public class AvoidDronesInstinct extends AvoidEntitiesInstinct{
	
	public AvoidDronesInstinct(CIArguments args, RobotCI robot) {
		super(args, robot);
	}

	protected boolean validEntity(Entity e) {
		return e instanceof RobotLocation;
	}

}
