/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import environment.ForagingEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class ForagingEvaluation extends EvaluationFunction {

    public ForagingEvaluation(Arguments args) {
        super(args);
    }

    @Override
    public void update(Simulator simulator) {
        ForagingEnvironment environment = (ForagingEnvironment) simulator.getEnvironment();
        fitness = environment.getCaptureCount();
    }
}
