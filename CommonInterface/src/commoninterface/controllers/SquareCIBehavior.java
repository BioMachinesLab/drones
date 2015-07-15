package commoninterface.controllers;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class SquareCIBehavior extends CIBehavior {
	
	private AquaticDroneCI drone;
	private double stepsfw = 80;
	private double stepsturning = 13;
	private double currentSteps = 0;
	private boolean fw = true;
	
	public SquareCIBehavior(CIArguments args, RobotCI drone) {
		super(args,drone);
		this.drone = (AquaticDroneCI)drone;
	}

	@Override
	public void step(double time) {
		
		currentSteps++;
		
		if(fw) {
			if(currentSteps % stepsfw == 0) {
				fw = false;
				currentSteps=1;
			} else {
				drone.setRudder(0, 1);
			}
		}
		
		if(!fw) {
			if(currentSteps % stepsturning == 0) {
				fw = true;
				currentSteps=0;
			} else {
				drone.setRudder(0.8, 0.17);
			}
		}
		
	}
	
	public void cleanUp() {
		drone.setMotorSpeeds(0, 0);
	}
	
}