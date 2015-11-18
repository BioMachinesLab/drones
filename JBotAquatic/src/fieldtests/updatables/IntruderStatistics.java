package fieldtests.updatables;

import drone.MissionController;
import environment.LampedusaMissionEnvironment;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.AquaticDrone;

public class IntruderStatistics implements Updatable {
	
	public long stepsSeeingOne = 0;
	public long stepsSeeingTwoOrMore = 0;
	public long stepsInside = 0;
	public long totalIntruders = 0;
	public long foundIntruders = 0;
	private long stepsUntilSeeing = 0;
	public AquaticDrone intruder = null;
	public boolean currentlyInside = false;
	public boolean detected = false;
	public LampedusaMissionEnvironment env;
	
	private long currentStepsUntilSeeing = 0;
	
	private int onboardRange = 50;
	private int printFreq = 1000;
	
	public IntruderStatistics(int onboardRange) {
		this.onboardRange = onboardRange;
	}
	
	public IntruderStatistics(int onboardRange, int printFreq) {
		this.onboardRange = onboardRange;
		this.printFreq = printFreq;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(env == null) {
			env = (LampedusaMissionEnvironment)simulator.getEnvironment();
		}
		
		if(intruder == null) {
			intruder = (AquaticDrone) simulator.getRobots().get(simulator.getRobots().size()-1);
		}
		
		if(env.insideBoundary(intruder.getGPSLatLon())) {
			if(!currentlyInside) {
				totalIntruders++;
				currentlyInside = true;
				detected = false;
				currentStepsUntilSeeing = 0;
			}
			int count = 0;
			
			for(int i = 0 ; i < simulator.getRobots().size() - 1 ; i++) {
				AquaticDrone r = (AquaticDrone)simulator.getRobots().get(i);
				if(r.isEnabled() && r.getGPSLatLon() != null)
					if(r.getGPSLatLon().distanceInMeters(intruder.getGPSLatLon()) < onboardRange)
						count++;
			}
			if(count == 1)
				stepsSeeingOne++;
			else if(count > 1)
				stepsSeeingTwoOrMore++;
			
			if(count > 0 && !detected) {
				foundIntruders++;
				detected = true;
			}
			
			if(currentlyInside && detected && currentStepsUntilSeeing > 0) {
				stepsUntilSeeing+=currentStepsUntilSeeing;
				currentStepsUntilSeeing = 0;
			}
			
			if(currentlyInside && !detected) {
				currentStepsUntilSeeing++;
			}
			
			stepsInside++;
		} else {
			currentlyInside = false;
		}
		
		if(simulator.getTime() % printFreq == 0) {
			
			int[] states = new int[MissionController.State.values().length];
			
			for(int i = 0 ; i < simulator.getRobots().size() - 1 ; i++) {
				MissionController m = (MissionController) ((AquaticDrone)simulator.getRobots().get(i)).getActiveBehavior();
				if(m==null)
					continue;
				states[m.getCurrentState().ordinal()]++;
			}
			String st = "";
			for(int i = 0 ; i < states.length; i++) {
				st+=MissionController.State.values()[i]+":"+states[i]+" ";
			}
			System.out.println(simulator.getTime()+" "+stepsInside+" "+stepsSeeingOne+" "+stepsSeeingTwoOrMore+" "+totalIntruders+" "+foundIntruders+" "+stepsUntilSeeing+" "+st);
		}
	}
}