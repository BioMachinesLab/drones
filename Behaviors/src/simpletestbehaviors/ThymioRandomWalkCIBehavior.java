package simpletestbehaviors;

import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import commoninterface.utils.CIArguments;

public class ThymioRandomWalkCIBehavior extends CIBehavior{

	public enum State { FORWARD, TURNING }
	
	private ThymioCI thymio;
	private State currentState;
	
	private int forwardTime;
	private int turningTime;
	
	private int forwardSteps;
	private int turningSteps;
	
	public ThymioRandomWalkCIBehavior(CIArguments args, RobotCI thymio) {
		super(args, thymio);
		this.thymio = (ThymioCI)thymio;
		currentState = State.FORWARD;
		
		CIArguments arguments = null;
		
		if(args.getArgumentIsDefined("behavior"))
			arguments = new CIArguments(args.getArgumentAsString("behavior"));
		else
			arguments = args;
		
		forwardTime = arguments.getArgumentAsIntOrSetDefault("forward", 22);
		turningTime = arguments.getArgumentAsIntOrSetDefault("turning", 19);
		
		forwardSteps = 0;
		turningSteps = 0;
	}

	@Override
	public void step(double timestep) {
		
//		if(timestep < 40)
//			thymio.setMotorSpeeds(1, 1);
//		else
//			thymio.setMotorSpeeds(0, 0);
		
		if(currentState == State.FORWARD){
			turningSteps = 0;
			thymio.setMotorSpeeds(0.5, 0.5);
			forwardSteps ++;
			
			if(forwardSteps > forwardTime)
				currentState = State.TURNING;
			
		}else{
			forwardSteps = 0;
			thymio.setMotorSpeeds(0, 0.5);
			turningSteps ++;
			
			if(turningSteps > turningTime)
				currentState = State.FORWARD;
			
		}
		
	}

	@Override
	public void cleanUp() {
		forwardSteps = 0;
		turningSteps = 0;
		currentState = State.FORWARD;
		
		thymio.setMotorSpeeds(0, 0);
	}
	
}
