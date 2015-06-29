package behaviors;

import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class ShutdownCIBehavior extends CIBehavior {
	
	private RealAquaticDroneCI drone;
	
	public ShutdownCIBehavior(CIArguments args, RobotCI drone) {
		super(args,drone);
		this.drone = (RealAquaticDroneCI)drone;
	}

	@Override
	public void step(double time) {
		drone.shutdown();
		System.exit(0);
	}
	
}