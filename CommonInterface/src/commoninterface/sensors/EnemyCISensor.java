package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.SharedDroneLocation;
import commoninterface.utils.CIArguments;

public class EnemyCISensor extends ConeTypeCISensor{
	
	private boolean share = false;
	
	public EnemyCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		share = args.getFlagIsTrue("share");
	}

	@Override
	public boolean validEntity(Entity e) {
		if(e instanceof RobotLocation) {
			RobotLocation rl = (RobotLocation)e;
			return rl.getDroneType() == AquaticDroneCI.DroneType.ENEMY;
		}
		return false;
	}
	
	@Override
	protected void sensedEntity(Entity e){
		
		if(share && e instanceof RobotLocation) {
			RobotLocation rl = (RobotLocation)e;
			if(rl.getDroneType() == AquaticDroneCI.DroneType.ENEMY) {
				SharedDroneLocation dl = new SharedDroneLocation(rl.getName(), robot.getNetworkAddress(), rl.getLatLon(), rl.getDroneType());
				dl.setTimestepReceived((long)(robot.getTimeSinceStart()*10));
				robot.replaceEntity(dl);
			}
		}
	}
	
}
