package controllers.inputs;

import simulation.robot.CISensorWrapper;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class CISensorWrapperNNInput extends NNInput {
	
	protected CISensorWrapper sensor;
	
	public CISensorWrapperNNInput(Sensor s) {
		super(s);
		this.sensor = (CISensorWrapper)s;
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

