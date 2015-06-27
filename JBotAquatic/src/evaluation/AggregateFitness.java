/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class AggregateFitness extends EvaluationFunction {

    private double meanDistance = 0;
    private boolean kill = true;
    private int steps = 0;

    public AggregateFitness(Arguments args) {
        super(args);
        kill = args.getFlagIsTrue("kill");
    }

    @Override
    public void update(Simulator simulator) {
        steps++;

        mathutils.Vector2d centreMass = new mathutils.Vector2d();
        for(Robot r : simulator.getRobots()) {
            centreMass.add(r.getPosition());
        }
        centreMass.x /= simulator.getRobots().size();
        centreMass.y /= simulator.getRobots().size();
        
        double currentDistance = 0;
        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            currentDistance += drone.getPosition().distanceTo(centreMass);
        }
        currentDistance /= simulator.getRobots().size();

        meanDistance += (1000 - currentDistance);
        fitness = meanDistance / steps;

        for (Robot r : simulator.getRobots()) {
            if (kill && r.isInvolvedInCollison()) {
                simulator.stopSimulation();
                fitness /= 10;
                break;
            }
        }
    }

    @Override
    public double getFitness() {
        return fitness;
    }
}
