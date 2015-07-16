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
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

/**
 *
 * @author jorge
 */
public class StationKeepingAvoidFitness extends EvaluationFunction {

    @ArgumentsAnnotation(name = "alloweddistance", defaultValue = "1.5")
    private double allowedDistance = 1.5;
    private double safetyDistance = 3;

    private final double maxDistance = 10;
    private double usedEnergy = 0;
    private double totalDistance = 0;
    private int steps = 0;
    private double minDistance = Double.POSITIVE_INFINITY;

    public StationKeepingAvoidFitness(Arguments args) {
        super(args);
        allowedDistance = args.getArgumentAsDouble("alloweddistance");
        safetyDistance = args.getArgumentAsDouble("safetydistance");
    }

    @Override
    public void update(Simulator simulator) {
        steps++;
        double distance = 0;
        double energy = 0;

        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        Waypoint wp = drone.getActiveWaypoint();
        Vector2d wpPos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        double d = wpPos.distanceTo(new Vector2d(drone.getPosition().x, drone.getPosition().y));
        distance += d;
        energy += (Math.abs(drone.getLeftMotorSpeed()) + Math.abs(drone.getRightMotorSpeed())) / 2;

        usedEnergy += (distance <= allowedDistance ? energy : 1);
        totalDistance += distance / simulator.getRobots().size();

        double dist = simulator.getRobots().get(0).getPosition().distanceTo(simulator.getRobots().get(1).getPosition());
        minDistance = Math.min(minDistance, dist);
        
        fitness = (maxDistance - Math.min(maxDistance, totalDistance / steps)) / maxDistance + 
                1 - usedEnergy / steps +
                (Math.min(safetyDistance, minDistance) / safetyDistance) * 2;
    }

    @Override
    public double getFitness() {
        return Math.max(0,fitness);
    }

}
