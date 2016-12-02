package controllers.outputs;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuator.RudderActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class RudderNNOutput extends NNOutput {

	private static final long serialVersionUID = 4086134606434922680L;
	private double heading;
	private double speed;
	private RudderActuator rudder;

	public RudderNNOutput(Actuator act, Arguments args) {
		super(act, args);
		rudder = (RudderActuator) act;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {

		// if(Double.isNaN(value)) {
		// System.out.println("wtf!");
		// }

		if (output == 0) {
			heading = value;
		} else {
			speed = value;
		}
	}

	@Override
	public void apply() {

		double h = heading;
		h = Math.max(-1, h);
		h = Math.min(1, h);

		double s = speed;

		if (speed > 0) {
			s = speed * (1.0);
			s = Math.max(0, s);
			s = Math.min(1, s);
		}

		// if(Double.isNaN(h)) {
		// System.out.println("wtf!");
		// }

		rudder.setHeading(h);
		rudder.setSpeed(s);
	}
}