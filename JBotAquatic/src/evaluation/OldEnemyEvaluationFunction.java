package evaluation;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class OldEnemyEvaluationFunction extends EvaluationFunction{
	
	@ArgumentsAnnotation(name="targetdistance", defaultValue="10")
	private double targetDistance = 10;
	@ArgumentsAnnotation(name="minimumdistance", defaultValue="4")
	private double minimumDistance = 4;
	@ArgumentsAnnotation(name="kill", defaultValue="0")
	private boolean kill = false;
	private double value = 0;
	
	private Robot enemy = null;

	public OldEnemyEvaluationFunction(Arguments args) {
		super(args);
		targetDistance = args.getArgumentAsDoubleOrSetDefault("targetdistance", targetDistance);
		minimumDistance = args.getArgumentAsDoubleOrSetDefault("minimumdistance", minimumDistance);
		kill = args.getArgumentAsDoubleOrSetDefault("kill", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(enemy == null) {
			enemy = simulator.getRobots().get(simulator.getRobots().size()-1);
			value = 1.0/simulator.getEnvironment().getSteps()/(simulator.getRobots().size()-1);
			fitness = 0;
		}
		
		for(Robot r : simulator.getRobots()) {
			if(r.getId() != enemy.getId()) {
				
				double dist = r.getPosition().distanceTo(enemy.getPosition());
				
				if(r.isInvolvedInCollison()) {
					if(kill) {
						simulator.stopSimulation();
						fitness = 0;
					} else
						fitness-=value*2;
				} else if(dist < targetDistance && dist > minimumDistance) {
					fitness+=value;
				} else {
//					fitness-=value;
				}
			}
		}
	}
	
	@Override
	public double getFitness() {
		return Math.max(10+fitness,0);
	}
	
}