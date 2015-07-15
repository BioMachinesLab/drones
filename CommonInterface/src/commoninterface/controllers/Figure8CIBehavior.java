package commoninterface.controllers;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class Figure8CIBehavior extends CIBehavior {
	
	private AquaticDroneCI drone;
	private double steps = 80;
	private boolean left = false;
	
	public Figure8CIBehavior(CIArguments args, RobotCI drone) {
		super(args,drone);
		this.drone = (AquaticDroneCI)drone;
	}

	@Override
	public void step(double time) {
		
		if(time % steps == 0) {
			left = !left;
		}
		
		if(left)
			drone.setRudder(-0.5, 1);
		else
			drone.setRudder(0.5, 1);
		
	}
	
	public void cleanUp() {
		drone.setMotorSpeeds(0, 0);
	}
	
}