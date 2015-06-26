/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class AggregateWaypointFitness extends EvaluationFunction {

    private boolean configured = false;
    private double startingDistance = 0;
    private Waypoint wp = null;
    private int steps = 0;
    private boolean kill = false;
    private double timeStopped = 0;

    public AggregateWaypointFitness(Arguments args) {
        super(args);
        kill = args.getFlagIsTrue("kill");
    }

    @Override
    public void update(Simulator simulator) {
        if (!configured) {
            steps = simulator.getEnvironment().getSteps();
            wp = ((AquaticDrone) simulator.getRobots().get(0)).getActiveWaypoint();
            for (Robot r : simulator.getRobots()) {
                startingDistance += calculateDistance(wp, r);
            }
            startingDistance /= simulator.getRobots().size();
            configured = true;
        }

        double currentDistance = 0;
        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            currentDistance += calculateDistance(wp, drone);
            if (drone.getLeftWheelSpeed() == 0 && drone.getRightWheelSpeed() == 0) {
                timeStopped++;
            }
        }
        currentDistance /= simulator.getRobots().size();

        fitness = ((startingDistance - currentDistance) / startingDistance) + ((double) timeStopped / simulator.getRobots().size() / steps) / 2;

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
        return Math.max(fitness, 0);
    }

    static double calculateDistance(Waypoint wp, Robot drone) {
        Vector2d pos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        Vector2d robotPos = new Vector2d(drone.getPosition().getX(), drone.getPosition().getY());
        return pos.distanceTo(robotPos);
    }
}
