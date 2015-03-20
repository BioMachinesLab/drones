package commoninterface.neuralnetwork.outputs;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.CIArguments;

public class PropellersCINNOutput extends CINNOutput {

	private double leftSpeed;
	private double rightSpeed;
	private boolean forwardOnly = false;
	
	public PropellersCINNOutput(AquaticDroneCI drone, CIArguments args) {
		super(drone,args);
		forwardOnly = args.getArgumentAsIntOrSetDefault("forwardonly", 0) == 1;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {
		if (output == 0)
			leftSpeed = forwardOnly ? value : value*2 - 1;
		else
			rightSpeed = forwardOnly ? value : value*2 - 1;
	}

	@Override
	public void apply() {
		drone.setMotorSpeeds(leftSpeed, rightSpeed);
	}

}
