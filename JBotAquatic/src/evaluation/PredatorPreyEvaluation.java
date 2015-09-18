/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import environment.PredatorPreyEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class PredatorPreyEvaluation extends EvaluationFunction {

    double initialDist = Double.NaN;
    double finalDist = 0;
    public static double CAPTURE_DIST = 2;

    public PredatorPreyEvaluation(Arguments args) {
        super(args);
    }

    @Override
    public void update(Simulator simulator) {
        // get prey
        AquaticDrone prey = ((PredatorPreyEnvironment) simulator.getEnvironment()).getPreyDrone();

        boolean caught = false;
        finalDist = 0;
        for (Robot r : simulator.getRobots()) {
            if (r != prey) {
                double d = r.getPosition().distanceTo(prey.getPosition());
                if (d <= CAPTURE_DIST) {
                    caught = true;
                }
                finalDist += d;
            }
        }
        finalDist /= (simulator.getRobots().size() - 1);

        if (Double.isNaN(initialDist)) {
            initialDist = finalDist;
        }

        if (caught) {
            fitness = 2 - simulator.getTime() / simulator.getEnvironment().getSteps();
            simulator.stopSimulation();
        } else {
            fitness = Math.max(0, (initialDist - finalDist) / simulator.getEnvironment().getWidth());
        }

        // kill if prey escapes the arena
        double w = simulator.getEnvironment().getWidth();
        double h = simulator.getEnvironment().getHeight();
        if (prey.getPosition().x > w / 2 || prey.getPosition().x < -w / 2
                || prey.getPosition().y > h / 2 || prey.getPosition().y < -h / 2) {
            simulator.stopSimulation();
        }
    }

    /*@Override
    protected boolean checkCollisions(Robot r) {
        return r != prey;
    }*/

}
