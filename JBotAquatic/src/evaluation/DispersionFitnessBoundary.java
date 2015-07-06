/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import commoninterface.sensors.DroneCISensor;
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
public class DispersionFitnessBoundary extends EvaluationFunction {

    private boolean kill = true;
    private double meanDistance = 0;
    private int steps = 0;
    private double margin = 0;
    private boolean config = false;
    private double targetDistance = 0;

    public DispersionFitnessBoundary(Arguments args) {
        super(args);
        kill = args.getFlagIsTrue("kill");
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
        steps++;
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
        fitness = meanDistance / steps;

        for (Robot r : simulator.getRobots()) {
            if (kill && (r.isInvolvedInCollison() || r.isInvolvedInCollisonWall() || !insideLines(r.getPosition(), simulator))) {
                simulator.stopSimulation();
                fitness /= 10;
                break;
            }
        }
    }

    public boolean insideLines(Vector2d v, Simulator sim) {
        //http://en.wikipedia.org/wiki/Point_in_polygon
        int count = 0;
        for (PhysicalObject p : sim.getEnvironment().getAllObjects()) {
            if (p.getType() == PhysicalObjectType.LINE) {
                Line l = (Line) p;
                if (l.intersectsWithLineSegment(v, new Vector2d(0, -1000)) != null) {
                    count++;
                }
            }
        }
        return count % 2 != 0;
    }

    @Override
    public double getFitness() {
        return Math.max(0, fitness);
    }
}
