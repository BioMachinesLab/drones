package commoninterface.neuralnetwork.outputs;

import commoninterface.AquaticDroneCI;

public class StopCINNOutput extends CINNOutput {

	private boolean stop = false;
	
	public StopCINNOutput(AquaticDroneCI drone) {
		super(drone);
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int output, double value) {
		if (value > 0.5)
			stop = true;
		else
			stop = false;
	}

	@Override
	public void apply() {
		if(stop)
			drone.setMotorSpeeds(0.5, 0.5);
	}

}
