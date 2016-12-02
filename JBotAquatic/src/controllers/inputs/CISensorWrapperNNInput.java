package controllers.inputs;

import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import simulation.robot.CISensorWrapper;
import simulation.robot.sensors.Sensor;

public class CISensorWrapperNNInput extends NNInput {
	private static final long serialVersionUID = 5240107236081184300L;
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

