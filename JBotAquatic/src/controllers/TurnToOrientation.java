package controllers;

import simpletestbehaviors.TurnToOrientationCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI;
import commoninterface.CILogger;
import commoninterface.CIStdOutLogger;
import commoninterface.utils.CIArguments;

public class TurnToOrientation extends Controller {

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
