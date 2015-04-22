package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.sensors.InsideBoundaryCISensor;
import commoninterface.utils.CIArguments;

public class GenericCINNInput extends CINNInput{
	
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
