package controllers;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class GenericCIController extends Controller {

	private CIBehavior behavior;
	
	public GenericCIController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);

		String ciString = args.getArgumentAsString("ci");
		CIArguments ciargs = new CIArguments(ciString,true);
		
		behavior = CIBehavior.getController(ciargs, (RobotCI)robot);
	}

	@Override
	public void controlStep(double time) {
		behavior.step(time);
	}
	
}
