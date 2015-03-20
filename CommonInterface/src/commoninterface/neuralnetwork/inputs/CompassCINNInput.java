package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.sensors.CompassCISensor;
import commoninterface.utils.CIArguments;

public class CompassCINNInput extends CINNInput {

	public CompassCINNInput(CISensor sensor, CIArguments args) {
		super(sensor, args);
	}
	
	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return ((CompassCISensor)sensor).getSensorReading(index) / 360.0;
	}

}
