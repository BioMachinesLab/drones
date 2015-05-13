package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.objects.Entity;
import commoninterface.objects.RobotLocation;
import commoninterface.utils.CIArguments;

public class EnemyCISensor extends ConeTypeCISensor{
	

	public EnemyCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Entity e) {
		if(e instanceof RobotLocation) {
			RobotLocation rl = (RobotLocation)e;
			return rl.getDroneType().equals(AquaticDroneCI.DroneType.ENEMY);
		}
		return false;
	}

}
