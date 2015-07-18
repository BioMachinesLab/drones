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
public class StationKeepingFitness extends EvaluationFunction {

    @ArgumentsAnnotation(name = "alloweddistance", defaultValue = "2.5")
    private double allowedDistance = 2.5;
    private final double maxDistance;
    private double energyBonus = 0;
    private double totalDistance = 0;

    public StationKeepingFitness(Arguments args) {
        super(args);
        allowedDistance = args.getArgumentAsDouble("alloweddistance");
        maxDistance = allowedDistance * 5;
    }

    @Override
    public void update(Simulator simulator) {
        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        Waypoint wp = drone.getActiveWaypoint();
        Vector2d wpPos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        double distance = wpPos.distanceTo(new Vector2d(drone.getPosition().x, drone.getPosition().y));
        
        if(distance <= allowedDistance) {
            energyBonus += 1 - drone.getMotorSpeedsInPercentage();
        }
        totalDistance += distance;

        fitness = (maxDistance - totalDistance / simulator.getTime()) / maxDistance * (energyBonus / simulator.getTime());
    }

    @Override
    public double getFitness() {
        return Math.max(0, 10 + fitness);
    }

}
