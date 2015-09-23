/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import environment.HerdingEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class HerdingEvaluation extends EvaluationFunction {

    private double initialDistance = Double.NaN;
    private double objectiveDistance;

    public HerdingEvaluation(Arguments args) {
        super(args);
    }

    @Override
    public void update(Simulator simulator) {
        HerdingEnvironment env = (HerdingEnvironment) simulator.getEnvironment();
        // setup
        if (Double.isNaN(initialDistance)) {
            objectiveDistance = env.getObjectiveDistance();
            initialDistance = env.getPreyDrone().getPosition().distanceTo(env.getObjective());
        }

        double dist = env.getPreyDrone().getPosition().distanceTo(env.getObjective());
        if (dist < objectiveDistance) { // corraled
            fitness = 2 - simulator.getTime() / env.getSteps();
            simulator.terminate();
        } else {
            fitness = Math.max(0, 1 - dist / initialDistance);
        }
        
        mathutils.Vector2d p = env.getPreyDrone().getPosition();
        if(p.x > env.getWidth() / 2 || p.x < - env.getWidth() / 2 || p.y > env.getHeight() / 2 || p.y < - env.getHeight() / 2) {
            simulator.terminate();
        }
    }
}
