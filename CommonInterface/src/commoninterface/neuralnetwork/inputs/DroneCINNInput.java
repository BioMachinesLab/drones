package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.utils.CIArguments;

public class DroneCINNInput extends CINNInput {

	public DroneCINNInput(CISensor sensor, CIArguments args) {
		super(sensor, args);
	}
	
	@Override
	public int getNumberOfInputValues() {
		return sensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		
		if(index >= getNumberOfInputValues())
			throw new RuntimeException("[DroneCINNInput] Invalid number of input index!");
		
		return sensor.getSensorReading(index);
		
	}

}
