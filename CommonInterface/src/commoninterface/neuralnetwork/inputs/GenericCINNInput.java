package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.utils.CIArguments;

public class GenericCINNInput extends CINNInput{
	private static final long serialVersionUID = -6426303360394201826L;

	public GenericCINNInput(CISensor sensor, CIArguments args) {
		super(sensor, args);
	}

	@Override
	public int getNumberOfInputValues() {
		return sensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		
		return sensor.getSensorReading(index);
	}

}
