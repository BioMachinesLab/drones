/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import commoninterface.sensors.DroneCISensor;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

import simulation.Simulator;
import simulation.robot.CISensorWrapper;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class DispersionFitness extends EvaluationFunction {

    private boolean kill = false;
    private double meanDistance = 0;
    private double margin = 0;
    private boolean config = false;
    private double targetDistance = 0;
    private final double safetyDistance;
    private double minDistanceOthers = Double.POSITIVE_INFINITY;
    
    public DispersionFitness(Arguments args) {
        super(args);
        kill = args.getFlagIsTrue("kill");
        margin = args.getArgumentAsDouble("margin");
        safetyDistance = args.getArgumentAsDouble("safetydistance");
    }

    @Override
    public void update(Simulator simulator) {
        if (!config) {
            CISensorWrapper wr = (CISensorWrapper)simulator.getRobots().get(0).getSensorByType(CISensorWrapper.class);
            DroneCISensor dcs = (DroneCISensor) wr.getCisensor();
            double r = dcs.getRange();
            targetDistance = r - margin;
            config = true;
        }
        
        // MEAN DISTANCE TO CLOSEST ROBOT
        double distanceDelta = 0;
        for (Robot r : simulator.getRobots()) {
            double minDist = Double.POSITIVE_INFINITY;
            for (Robot r2 : simulator.getRobots()) {
                if (r != r2) {
                    minDist = Math.min(minDist, r.getPosition().distanceTo(r2.getPosition()));
                }
            }
            distanceDelta += Math.abs(minDist - targetDistance);
        }
        distanceDelta /= simulator.getRobots().size();
        meanDistance += (simulator.getEnvironment().getWidth() - distanceDelta);
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
