package commoninterface.neuralnetwork.outputs;

import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class PropellersCINNOutput extends CINNOutput {

	private double leftSpeed;
	private double rightSpeed;
	private double deadZone = 0.10;
	private boolean forwardOnly = false;
	
	public PropellersCINNOutput(RobotCI robot, CIArguments args) {
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
			leftSpeed = forwardOnly ? value : value*2 - 1;
			if(deadZone > 0 && Math.abs(leftSpeed) <= deadZone)
				leftSpeed = 0;
				
		} else {
			rightSpeed = forwardOnly ? value : value*2 - 1;
			if(deadZone > 0 && Math.abs(rightSpeed) <= deadZone)
				rightSpeed = 0;
		}
	}

	@Override
	public void apply() {
		robot.setMotorSpeeds(leftSpeed, rightSpeed);
	}

}
