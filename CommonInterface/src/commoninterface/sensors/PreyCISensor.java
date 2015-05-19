package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.entities.PreyEntity;
import commoninterface.entities.ThymioSharedEntity;
import commoninterface.entities.VirtualEntity;
import commoninterface.utils.CIArguments;

public class PreyCISensor extends ThymioConeTypeCISensor {

	private boolean share = false;
	
	public PreyCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		share = args.getFlagIsTrue("share");
	}

	@Override
	public boolean validEntity(Object e) {
		return e instanceof PreyEntity;
	}
	
	@Override
	protected void sensedEntity(VirtualEntity ve) {
		if(share && ve instanceof PreyEntity) {
			PreyEntity pe = (PreyEntity)ve;
			ThymioSharedEntity tse = new ThymioSharedEntity(pe.getName(), robot.getNetworkAddress(), pe.getPosition());
			tse.setTimestepReceived((long)(robot.getTimeSinceStart()*10));
			robot.replaceEntity(tse);
		}
	}
	
}
