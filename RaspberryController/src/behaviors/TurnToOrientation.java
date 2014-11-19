package behaviors;

import io.input.ControllerInput;
import io.input.I2CCompassModuleInput;
import io.output.ControllerOutput;
import io.output.ReversableESCManagerOutput;
import main.Controller;

public class TurnToOrientation extends Behavior {
	
	private I2CCompassModuleInput compass;
	private ReversableESCManagerOutput motors;
	private int targetOrientation = 0;
	
	public TurnToOrientation(Controller c) {
		super(c);
		
		for(ControllerInput i : c.getInputs()) {
			if(i instanceof I2CCompassModuleInput) {
				compass = (I2CCompassModuleInput)i;
				break;
			}
		}
		
		for(ControllerOutput o : c.getOutputs()) {
			if(o instanceof ReversableESCManagerOutput) {
				motors = (ReversableESCManagerOutput)o;
				break;
			}
		}
	}

	@Override
	protected void update() {
		
		while(targetOrientation < 0)
			targetOrientation+=360;
		
		while(targetOrientation > 360)
			targetOrientation-=360;
		
		int diff = Math.abs(targetOrientation - compass.getHeading());
		
		if(diff < 20) {//this is good enough
			motors.setValue(0, 0.5);
			motors.setValue(1, 0.5);
			return;
		}
		
		if(diff < 180) {//we should turn left
			motors.setValue(0, 0.4);
			motors.setValue(1, 0.6);
		} else {//we should turn right
			motors.setValue(0, 0.6);
			motors.setValue(1, 0.4);
		}
		
	}
	
	@Override
	public void setArgument(int index, double value) {
		if(index == 0) {
			targetOrientation = (int)value;
		}
	}
}