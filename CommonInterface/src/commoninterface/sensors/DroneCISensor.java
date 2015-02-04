package commoninterface.sensors;

import objects.DroneLocation;
import objects.Entity;
import commoninterface.AquaticDroneCI;
import commoninterface.utils.CIArguments;

public class DroneCISensor extends ConeTypeCISensor{
	

	public DroneCISensor(int id, AquaticDroneCI drone, CIArguments args) {
		super(id, drone, args);
	}

	@Override
	public boolean validEntity(Entity e) {
		return e instanceof DroneLocation;
	}

}
