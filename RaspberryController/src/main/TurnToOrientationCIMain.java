package main;

import simpletestbehaviors.TurnToOrientationCIBehavior;
import commoninterface.CIStdOutLogger;
import commoninterfaceimpl.RealAquaticDroneCI;

public class TurnToOrientationCIMain {

	public static void main(String[] args) {
		RealAquaticDroneCI drone  = new RealAquaticDroneCI();
		CIStdOutLogger     logger = new CIStdOutLogger(drone);
		TurnToOrientationCIBehavior behavior = new TurnToOrientationCIBehavior(args, drone, logger);
		behavior.start();
		
		long millisBetweenControlCycles = (long) (1000.0 * behavior.getControlStepPeriod());
		long before = System.currentTimeMillis();
		
		while (!behavior.getTerminateBehavior()) {
			behavior.step();
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
