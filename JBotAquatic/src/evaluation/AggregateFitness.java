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
    private boolean kill = false;
    private final double safetyDistance;
    private double minDistanceOthers = Double.POSITIVE_INFINITY;

    public AggregateFitness(Arguments args) {
        super(args);
        kill = args.getFlagIsTrue("kill");
        safetyDistance = args.getArgumentAsDouble("safetydistance");
    }

    @Override
    public void update(Simulator simulator) {
        // MEAN DISTANCE TO CENTRE OF MASS
        mathutils.Vector2d centreMass = new mathutils.Vector2d();
        for (Robot r : simulator.getRobots()) {
            centreMass.add(r.getPosition());
        }
        centreMass.x = centreMass.x / simulator.getRobots().size();
        centreMass.y = centreMass.y / simulator.getRobots().size();

        double currentDistance = 0;
        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            currentDistance += drone.getPosition().distanceTo(centreMass);
        }
        currentDistance /= simulator.getRobots().size();
        meanDistance += (simulator.getEnvironment().getWidth() - currentDistance);
        fitness = meanDistance / simulator.getTime();

        // COLLISIONS
        for(int i = 0 ; i < simulator.getRobots().size() ; i++) {
            for(int j = i + 1 ; j < simulator.getRobots().size() ; j++) {
                Robot ri = simulator.getRobots().get(i);
                Robot rj = simulator.getRobots().get(j);
                minDistanceOthers = Math.min(minDistanceOthers, ri.getPosition().distanceTo(rj.getPosition()) - ri.getRadius() - rj.getRadius());
            }
            if(kill && simulator.getRobots().get(i).isInvolvedInCollison()){
                simulator.stopSimulation();
            }
        }
        double safetyFactor = Math.min(safetyDistance, minDistanceOthers) / safetyDistance;        
        fitness *= safetyFactor;
    }

    @Override
    public double getFitness() {
        return Math.max(0, fitness);
    }
}
