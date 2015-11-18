package fieldtests.evaluation;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;
import environment.TestHierarchicalMissionEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class IntruderStatisticsFitness extends EvaluationFunction {
	
	public long stepsSeeingOne = 0;
	public long stepsSeeingTwoOrMore = 0;
	public long stepsInside = 0;
	public long totalIntruders = 0;
	public long foundIntruders = 0;
	public AquaticDrone intruder = null;
	public boolean currentlyInside = false;
	public boolean detected = false;
	public TestHierarchicalMissionEnvironment env;
	
	private long currentStepsUntilSeeing = 0;
	
	private int onboardRange = 20;
	
	int[] countNumberSeeing = new int[100];
	
	//percentageseeing,averagerobotsseeing coverage
	String type = "percentageseeing";
	
	CoverageFitnessTest coverage = null;

    public IntruderStatisticsFitness(Arguments args) {
        super(args);
        type = args.getArgumentAsStringOrSetDefault("type", type);
    }

    @Override
    public void update(Simulator simulator) {
    	if(type.equals("coverage")) {
    		if(coverage == null)
    			coverage = new CoverageFitnessTest(new Arguments("distance=20,kill=1,resolution=5,safetydistance=3,dontuse=1"));
    		coverage.update(simulator);
    		
    	} else {
    		myUpdate(simulator);
    	}
    }
    
    public void myUpdate(Simulator simulator) {

    	if(env == null) {
			env = (TestHierarchicalMissionEnvironment)simulator.getEnvironment();
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
			
			countNumberSeeing[count]++;
			
			if(currentlyInside && detected && currentStepsUntilSeeing > 0) {
				currentStepsUntilSeeing = 0;
			}
			
			if(currentlyInside && !detected) {
				currentStepsUntilSeeing++;
			}
			
			stepsInside++;
		} else {
			currentlyInside = false;
		}    	
    }

    @Override
    public double getFitness() {
    	int total = 0;
		for(int i = 0 ; i < 10 ; i++) {
//			System.out.print(countNumberSeeing[i]+" ");
			total+=countNumberSeeing[i];
		}
//		System.out.println();
		
		double notSeeing = countNumberSeeing[0];
		
		double result = 0;
		
		switch(type) {
			case "percentageseeing":
				result = (total-notSeeing) / total;
				break;
			case "averagerobotsseeing":
				double val = 0;
				for(int i = 1 ; i < countNumberSeeing.length ; i++) {
					val+=i*countNumberSeeing[i];
				}
				val/=total;
				result = val;
				break;
			case "coverage":
				if(coverage != null)
					result = coverage.getFitness();
				break;
			default:
		}
		
//		System.out.println();
		
		
		return result;
    }

}
