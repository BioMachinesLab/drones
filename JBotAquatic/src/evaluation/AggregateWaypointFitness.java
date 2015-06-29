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
    private double meanDistance = 0;
    private Waypoint wp = null;
    private boolean kill = true;
    private int steps = 0;

    public AggregateWaypointFitness(Arguments args) {
        super(args);
        kill = args.getFlagIsTrue("kill");
    }

    @Override
    public void update(Simulator simulator) {
        steps++;
        if (!configured) {
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
        }
        currentDistance /= simulator.getRobots().size();

        meanDistance += (startingDistance - currentDistance) / startingDistance;
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
        return 10 + fitness;
    }

    static double calculateDistance(Waypoint wp, Robot drone) {
        Vector2d pos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        Vector2d robotPos = new Vector2d(drone.getPosition().getX(), drone.getPosition().getY());
        return pos.distanceTo(robotPos);
    }
}
