package commoninterface.neuralnetwork.outputs;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class RudderCINNOutput extends CINNOutput {
	private static final long serialVersionUID = -6786816191708652763L;
	private double heading;
	private double speed;
	private boolean bottomLimit;

	public RudderCINNOutput(RobotCI robot, CIArguments args) {
		super(robot, args);
		bottomLimit=args.getArgumentAsIntOrSetDefault("bottomLimit", 0)==1;
		
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
			if (bottomLimit) {
				if (value < 0.1) {
					speed = 0;
				} else {
					speed = value;
				}
			} else {
				speed = value;
			}
		}
	}

	@Override
	public void apply() {
		((AquaticDroneCI) robot).setRudder(heading * 2 - 1, speed);
	}

}
