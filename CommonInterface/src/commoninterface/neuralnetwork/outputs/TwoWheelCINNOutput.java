package commoninterface.neuralnetwork.outputs;

import commoninterface.AquaticDroneCI;

public class TwoWheelCINNOutput extends CINNOutput {

	private double leftSpeed;
	private double rightSpeed;
	private AquaticDroneCI drone;
	
	public TwoWheelCINNOutput(AquaticDroneCI drone) {
		this.drone = drone;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {
		if (output == 0)
			leftSpeed = value;
		else
			rightSpeed = value;
	}

	@Override
	public void apply() {
		drone.setMotorSpeeds(leftSpeed, rightSpeed);
	}

}
