package commoninterface.neuralnetwork.inputs;

import commoninterface.AquaticDroneCI;

public class CompassCINNInput extends CINNInput {

	private AquaticDroneCI drone;

	public CompassCINNInput(AquaticDroneCI drone) {
		this.drone = drone;
	}
	
	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return drone.getCompassOrientationInDegrees();
	}

}
