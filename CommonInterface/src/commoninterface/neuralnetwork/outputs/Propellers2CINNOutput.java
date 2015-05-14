package commoninterface.neuralnetwork.outputs;

import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class Propellers2CINNOutput extends CINNOutput {

	
	private double yaw = 0;
	private double speed = 0;
	
	private double deadZone = 0.10;
	private boolean forwardOnly = false;
	
	public Propellers2CINNOutput(RobotCI robot, CIArguments args) {
		super(robot,args);
		forwardOnly = args.getArgumentAsIntOrSetDefault("forwardonly", 0) == 1;
		deadZone = args.getArgumentAsDoubleOrSetDefault("deadzone", deadZone);
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {
		if (output == 0) {
			speed = forwardOnly ? value : value*2 - 1;
			if(deadZone > 0 && Math.abs(speed) <= deadZone)
				speed = 0;
				
		} else {
			yaw = (value-0.5)*2.0;
		}
	}

	@Override
	public void apply() {
		
		double absYaw = Math.abs(yaw);
		
		double l = 0,r = 0;
		
		if(absYaw < 0) {
			l = speed*(1-absYaw);
			r = speed;
		} else if(absYaw >= 0) {
			l = speed;
			r = speed*(1-absYaw);
		}
		
		robot.setMotorSpeeds(l, r);
	}

}
