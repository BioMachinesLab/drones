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
import simulation.util.ArgumentsAnnotation;

/**
 *
 * @author jorge
 */
public class StationKeepingFitness extends EvaluationFunction {

    @ArgumentsAnnotation(name="alloweddistance", defaultValue="1.5")
    private double allowedDistance = 1.5;

    private final double maxDistance = 10;
    private double usedEnergy = 0;
    private double totalDistance = 0;
    private int steps = 0;

    public StationKeepingFitness(Arguments args) {
        super(args);
    	allowedDistance = args.getArgumentAsDouble("alloweddistance");
    }
    
    @Override
    public void update(Simulator simulator) {
        steps++;
        double distance = 0;
        double energy = 0;
        for(Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            Waypoint wp = drone.getActiveWaypoint();
            Vector2d wpPos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
            double d = wpPos.distanceTo(new Vector2d(drone.getPosition().x, drone.getPosition().y));
            distance += d;
            energy += (Math.abs(drone.getLeftMotorSpeed()) + Math.abs(drone.getRightMotorSpeed())) / 2;
        }
        
        usedEnergy += energy / simulator.getRobots().size();
        totalDistance += distance / simulator.getRobots().size();
        
        fitness = (maxDistance - Math.min(maxDistance, totalDistance / steps)) / maxDistance;
        if(totalDistance / steps <= allowedDistance) {
            fitness += (1 - usedEnergy / steps);
        }        
    }
    
    @Override
    public double getFitness() {
    	return fitness;
    }    
    
}
