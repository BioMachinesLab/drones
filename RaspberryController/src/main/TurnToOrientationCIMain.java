package main;

import simpletestbehaviors.TurnToOrientationCIBehavior;
import commoninterface.CIStdOutLogger;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class TurnToOrientationCIMain {

	public static void main(String[] args) {
		RealAquaticDroneCI drone  = new RealAquaticDroneCI();
		TurnToOrientationCIBehavior behavior = new TurnToOrientationCIBehavior(new CIArguments(""), drone);
		behavior.start();
		
		long millisBetweenControlCycles = (long) (1000.0 * behavior.getControlStepPeriod());
		long before = System.currentTimeMillis();
		
		double timestep = 0;
		
		while (!behavior.getTerminateBehavior()) {
			behavior.step(timestep++);
			long after = System.currentTimeMillis();
			long millisTaken = Math.max(after - before, 0);
			
			if (millisTaken < millisBetweenControlCycles) {
				try {
					Thread.sleep(millisBetweenControlCycles - millisTaken);
				} catch (InterruptedException e) {}	
			}
	
			before = after;
		}
	}

}
