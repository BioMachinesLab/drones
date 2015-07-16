package commoninterface.neuralnetwork.outputs;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class RudderCINNOutput extends CINNOutput {

	private double heading;
	private double speed;
	
	public RudderCINNOutput(RobotCI robot, CIArguments args) {
		super(robot,args);
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {
		if (output == 0) {
			heading = value;	
		} else {
			speed = value;
		}
	}

	@Override
	public void apply() {
		((AquaticDroneCI)robot).setRudder(heading*2-1, speed);
	}

}
