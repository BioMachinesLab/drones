package simpletestbehaviors;

import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import commoninterface.utils.CIArguments;

public class ThymioRandomWalkCIBehavior extends CIBehavior{

	private ThymioCI thymio;

	public ThymioRandomWalkCIBehavior(CIArguments args, RobotCI thymio) {
		super(args, thymio);
		this.thymio = (ThymioCI)thymio;
	}

	@Override
	public void step(double timestep) {
		int changeDirection = 0; 
		
		if(changeDirection < 100){
			thymio.setMotorSpeeds(0.5, 0.5);
		}else if(changeDirection < 100){
			thymio.setMotorSpeeds(0.25, 0.5);
		}else if(changeDirection < 150){
			thymio.setMotorSpeeds(0.5, 0.25);
		}
		
		changeDirection++;
	}

	@Override
	public void cleanUp() {
		thymio.setMotorSpeeds(0, 0);
	}
	
}
