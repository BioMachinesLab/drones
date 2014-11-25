package commoninterface.neuralnetwork.inputs;

import commoninterface.AquaticDroneCI;

public class GPSCINNInput extends CINNInput {

	private AquaticDroneCI drone;

	public GPSCINNInput(AquaticDroneCI drone) {
		this.drone = drone;
	}
	
	@Override
	public int getNumberOfInputValues() {
		return 2;
	}

	@Override
	public double getValue(int index) {
		if(index == 0){
			return drone.getGPSLatitude();
		}else{
			return drone.getGPSLongitude();
		}
	}

}
