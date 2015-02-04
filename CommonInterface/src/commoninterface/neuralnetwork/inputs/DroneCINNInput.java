package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.sensors.WaypointCISensor;

public class DroneCINNInput extends CINNInput {

	public DroneCINNInput(CISensor sensor) {
		super(sensor);
	}
	
	@Override
	public int getNumberOfInputValues() {
		return cisensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		
		if(index >= getNumberOfInputValues())
			throw new RuntimeException("[DroneCINNInput] Invalid number of input index!");
		
		return cisensor.getSensorReading(index);
		
	}

}
