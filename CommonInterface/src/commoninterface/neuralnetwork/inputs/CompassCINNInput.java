package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.sensors.CompassCISensor;
import commoninterface.utils.CIArguments;

public class CompassCINNInput extends CINNInput {
	private static final long serialVersionUID = -8060392904058025470L;

	public CompassCINNInput(CISensor sensor, CIArguments args) {
		super(sensor, args);
	}

	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return ((CompassCISensor) sensor).getSensorReading(index) / 360.0;
	}

}
