package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.sensors.WaypointCISensor;
import commoninterface.utils.CIArguments;

public class WaypointCINNInput extends CINNInput {

	public WaypointCINNInput(CISensor sensor, CIArguments args) {
		super(sensor, args);
	}
	
	@Override
	public int getNumberOfInputValues() {
		return 2;
	}

	@Override
	public double getValue(int index) {
		
		if(index >= getNumberOfInputValues())
			throw new RuntimeException("[WaypointCINNInput] Invalid number of input index!");
		
		if(index == 0) { //angle
			
			return sensor.getSensorReading(index)/360.0+0.5;
			
		} else {//range
			
			double range = ((WaypointCISensor)sensor).getRange();
			double distance = sensor.getSensorReading(index); 
			if(distance > range) {
				return 0;
			} else {
				return (range-distance)/range;
			}
		}
		
	}

}
