/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class AvoidCollisionsFunction extends EvaluationFunction {

    private final double safetyDistance;
    private double minDistanceOthers = Double.POSITIVE_INFINITY;
    private boolean kill = false;
    private boolean dontUse = false;

    public AvoidCollisionsFunction(Arguments args) {
        super(args);
        safetyDistance = args.getArgumentAsDouble("safetydistance");
        kill = args.getFlagIsTrue("kill");
        dontUse = args.getFlagIsTrue("dontuse");
    }

    @Override
    public void update(Simulator simulator) {
        if (dontUse) {
            return;
        }

        for (int i = 0; i < simulator.getRobots().size(); i++) {
            Robot ri = simulator.getRobots().get(i);
            if (checkCollisions(ri)) {
                for (int j = i + 1; j < simulator.getRobots().size(); j++) {
                    Robot rj = simulator.getRobots().get(j);
                    if (checkCollisions(rj)) {
                        minDistanceOthers = Math.min(minDistanceOthers, ri.getPosition().distanceTo(rj.getPosition()) - ri.getRadius() - rj.getRadius());
                    }
                }
                if (kill && ri.isInvolvedInCollison()) {
                    simulator.stopSimulation();
                }
            }
        }
        double safetyFactor = 0.1 + (Math.max(0, Math.min(safetyDistance, minDistanceOthers)) / safetyDistance) * 0.9;
        fitness *= safetyFactor;
    }

    protected boolean checkCollisions(Robot r) {
        return true;
    }
}
