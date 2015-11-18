package fieldtests.evaluation;

import commoninterface.CIBehavior;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.utils.jcoord.LatLon;
import drone.MissionController;
import drone.MissionController.State;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class HierarchicalFitnessTest extends EvaluationFunction {
	
	private int countCrossings = 0;
	private int prevCrossing = 0;
	private int seenThisCrossing = 0;
	
	private int stepsEnemyInside = 0;
	private int stepsSeeing = 0;
	private int stepsSeeingShared = 0;
	
	private boolean print = true;
	
	private int stepsGood = 0;

    public HierarchicalFitnessTest(Arguments args) {
        super(args);
    }

    @Override
    public void update(Simulator simulator) {
//    	System.out.println(countCrossings);
    	if(countCrossings == 4)
    		return;
    	
    	int countSeeing = 0;
    	int countSeeingShared = 0;
    	int enemyInside = 0;
    	
    	MissionController.State[] states = MissionController.State.values();
    	int[] countStates = new int[states.length];
    	
    	for(Robot r : simulator.getRobots()) {
    		AquaticDrone d = (AquaticDrone)r;
    		if(d.getDroneType() == DroneType.DRONE) {
    			CIBehavior activeBehavior = d.getActiveBehavior();
    			MissionController mc = (MissionController)activeBehavior;
    			if(mc != null) {
    				if(mc.seeingEnemyDirectly())
    					countSeeing++;
    				if(mc.seeingEnemyShared())
    					countSeeingShared++;
    			}
    		} else {
    			LatLon enemyLatLon = d.getGPSLatLon();
    			AquaticDrone firstDrone = (AquaticDrone)simulator.getRobots().get(0);
    			CIBehavior activeBehavior = firstDrone.getActiveBehavior();
    			MissionController mc = (MissionController)activeBehavior;
    			if(mc != null) {
    				if(mc.insideBoundary(enemyLatLon))
    					enemyInside = 1;
    			}
    		}
    	}
    	System.out.println(enemyInside);
    	stepsEnemyInside+=enemyInside;
    	stepsSeeing+= countSeeing > 0 ? 1 : 0;
    	stepsSeeingShared+= countSeeingShared > 0 ? 1 : 0;
    	
    	if(enemyInside == 1 && (countSeeing > 0 || countSeeingShared > 0))
    		seenThisCrossing = 1;
    	
    	if(prevCrossing == 1 && enemyInside == 0) {
    		countCrossings++;
    		seenThisCrossing = 0;
    	}
    	
    	prevCrossing = enemyInside;
    	if(print) {
//    		if(simulator.getTime()==0) {
//    			System.out.print("Time\tseeing\tseeingShared\tenemyInside\tseenThisCrossing\tseenCrossings\tstepsEnemyInside\tstepsSeeing\tstepsSeeingShared");
//    			for(State s : states)
//    				System.out.print("\t"+s);
//    		}
//    		System.out.print(simulator.getTime()+"\t"+countSeeing+"\t"+countSeeingShared+"\t"+enemyInside+"\t"+seenThisCrossing+"\t"+countCrossings+"\t"+stepsEnemyInside+"\t"+stepsSeeing+"\t"+stepsSeeingShared);
//    		for(int i : countStates)
//    			System.out.print("\t"+i);
//    		System.out.println();
    	}
    	
    }

    @Override
    public double getFitness() {
    	System.out.println(stepsEnemyInside+" "+stepsSeeing);
    	return stepsSeeing/(double)stepsEnemyInside;
    }

}
