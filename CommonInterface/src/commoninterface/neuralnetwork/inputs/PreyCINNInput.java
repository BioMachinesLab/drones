package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.utils.CIArguments;

public class PreyCINNInput extends CINNInput {

	private CISensor sensor;

	public PreyCINNInput(CISensor s, CIArguments args) {
		super(s, args);
		this.sensor = s;
	}

	@Override
	public int getNumberOfInputValues() {
		return sensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		if(index >= getNumberOfInputValues())
			throw new RuntimeException("[PreyCINNInput] Invalid number of input index!");
		
		return sensor.getSensorReading(index);
	}

}
