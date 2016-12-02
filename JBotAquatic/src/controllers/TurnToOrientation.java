package controllers;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.CIArguments;
import simpletestbehaviors.TurnToOrientationCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TurnToOrientation extends Controller {
	private static final long serialVersionUID = 6480465291730124993L;
	private TurnToOrientationCIBehavior turnBehavior;
	
	public TurnToOrientation(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		String argsString = "target=0,tolerance=20";
		turnBehavior = new TurnToOrientationCIBehavior(new CIArguments(argsString), (AquaticDroneCI) robot);
	}

	@Override
	public void controlStep(double time) {
		turnBehavior.step(time);
	}
	
}
