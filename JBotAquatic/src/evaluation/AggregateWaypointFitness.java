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
    private final double safetyDistance;
    private int steps = 0;
    private double minDistanceOthers = Double.POSITIVE_INFINITY;

    public AggregateWaypointFitness(Arguments args) {
        super(args);
        safetyDistance = args.getArgumentAsDouble("safetydistance");
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
            for(Robot r2 : simulator.getRobots()) {
                if(r != r2) {
                    double d = r.getPosition().distanceTo(r2.getPosition()) - r.getRadius() - r2.getRadius();
                    minDistanceOthers = Math.min(d, minDistanceOthers);
                }
            }
        }
        currentDistance /= simulator.getRobots().size();
        
        meanDistance += (startingDistance - currentDistance) / startingDistance;
        double safetyFactor = Math.min(safetyDistance, minDistanceOthers) / safetyDistance;        
        fitness = (meanDistance / steps) * safetyFactor;
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
