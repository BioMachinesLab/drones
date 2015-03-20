package commoninterface.neuralnetwork.outputs;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.CIArguments;

public class TwoWheelCINNOutput extends CINNOutput {

	private double leftSpeed;
	private double rightSpeed;
	
	public TwoWheelCINNOutput(AquaticDroneCI drone, CIArguments args) {
		super(drone,args);
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {
		
		if (output == 0)
			leftSpeed = value*2 - 1;
		else
			rightSpeed = value*2 - 1;
	}

	@Override
	public void apply() {
		drone.setMotorSpeeds(leftSpeed, rightSpeed);
	}

}
