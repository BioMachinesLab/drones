/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation.deprecated;

import commoninterface.sensors.DroneCISensor;
import evaluation.AvoidCollisionsFunction;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;

import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.CISensorWrapper;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class DispersionFitnessBoundary extends AvoidCollisionsFunction {

    private double meanDistance = 0;
    private double margin = 0;
    private boolean config = false;
    private double targetDistance = 0;

    public DispersionFitnessBoundary(Arguments args) {
        super(args);
        margin = args.getArgumentAsDouble("margin");
    }

    @Override
    public void update(Simulator simulator) {
        if (!config) {
            CISensorWrapper wr = (CISensorWrapper) simulator.getRobots().get(0).getSensorByType(CISensorWrapper.class);
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
        meanDistance += (simulator.getEnvironment().getWidth() - distanceDelta) / simulator.getEnvironment().getWidth();
        fitness = meanDistance / simulator.getTime();
        
        // TODO: STAY INSIDE BOUNDARIES

        super.update(simulator);
    }

    @Override
    public double getFitness() {
        return Math.max(0, fitness);
    }
}
