package evaluation;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class EnemyEvaluationFunction extends EvaluationFunction{
	
	@ArgumentsAnnotation(name="targetdistance", defaultValue="10")
	private double targetDistance = 10;
	@ArgumentsAnnotation(name="minimumdistance", defaultValue="4")
	private double minimumDistance = 4;
	@ArgumentsAnnotation(name="avoiddistance", defaultValue="0")
	private double avoidDistance = 0;
	@ArgumentsAnnotation(name="kill", defaultValue="0")
	private boolean kill = false;
	private double value = 0;
	
	private Robot enemy = null;

	public EnemyEvaluationFunction(Arguments args) {
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
		
		boolean killed = false;
		
		for(Robot r : simulator.getRobots()) {
			if(r.getId() != enemy.getId()) {
				
				double dist = r.getPosition().distanceTo(enemy.getPosition());
				
				if(r.isInvolvedInCollison()) {
					if(kill) {
						simulator.stopSimulation();
						killed = true;
					}
					fitness-=value*2;
				} else if(dist < targetDistance && dist > minimumDistance) {
					fitness+=value;
				} else {
					fitness-=value;
				}
			}
		}
		
		if(killed) {
			fitness-=value*(simulator.getRobots().size()-1)*(simulator.getEnvironment().getSteps()-simulator.getTime());
		}
	}
	
	@Override
	public double getFitness() {
		return fitness;
	}
	
}
