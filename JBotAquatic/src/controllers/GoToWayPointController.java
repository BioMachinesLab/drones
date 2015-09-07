package controllers;

import simpletestbehaviors.GoToWaypointCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI;
import commoninterface.utils.CIArguments;

public class GoToWayPointController extends Controller {

	private GoToWaypointCIBehavior goToWayPointBehavior;
	
	public GoToWayPointController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		String argsString = "angletolerance=10,distancetolerance=1.5,"+args.getCompleteArgumentString();
		goToWayPointBehavior = new GoToWaypointCIBehavior(new CIArguments(argsString), (AquaticDroneCI) robot);
	}

	@Override
	public void controlStep(double time) {
		goToWayPointBehavior.step(time);
	}

}
