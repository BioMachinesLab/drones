package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.utils.CIArguments;

public class DroneCISensor extends ConeTypeCISensor{
	

	public DroneCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Entity e) {
		if(e instanceof RobotLocation) {
			RobotLocation rl = (RobotLocation)e;
			return rl.getDroneType().equals(AquaticDroneCI.DroneType.DRONE);
		}
		return false;
	}
	
}
