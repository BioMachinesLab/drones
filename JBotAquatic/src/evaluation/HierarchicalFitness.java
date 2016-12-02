package evaluation;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

public class HierarchicalFitness extends EvaluationFunction {
	private static final long serialVersionUID = -5856255072533095190L;

	public HierarchicalFitness(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
	}

	@Override
	public double getFitness() {
		return fitness;
	}

}
