/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import commoninterface.entities.Entity;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class WaypointFitness extends EvaluationFunction {


    private boolean configured = false;
    private double startingDistance = 0;
    private double targetDistance = 1.5;
    private Waypoint wp = null;
    private int steps = 0;
    private boolean kill = false;
    private int timeWithin = 0;
    private int timeStopped = 0;
    
    public WaypointFitness(Arguments args) {
    	super(args);
    	targetDistance = args.getArgumentAsDouble("targetdistance");
        kill = args.getFlagIsTrue("kill");
	}
    
    @Override
    public void update(Simulator simulator) {
        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        boolean insideWP = false;

        if (!configured) {
            steps = simulator.getEnvironment().getSteps();
            ArrayList<Waypoint> waypoints = Waypoint.getWaypoints(drone);

            if (!waypoints.isEmpty()) {
                wp = getWaypoint(drone);
                startingDistance = calculateDistance(wp, drone);
            }

            configured = true;
        }

        if (wp != null) {
            double currentDistance = calculateDistance(wp, drone);
            if (currentDistance <= targetDistance) {
                insideWP = true;
                timeWithin++;
                
                if(drone.getLeftWheelSpeed() == 0 && drone.getRightWheelSpeed() == 0) {
                	timeStopped++;
                }
                
            } else {
                insideWP = false;
            }
            fitness = (float) ((startingDistance - currentDistance) / startingDistance) + ((double)timeWithin / steps) +  ( (double)timeStopped / steps);
            
        }

        if (kill && !insideWP && drone.isInvolvedInCollison()) {
            simulator.stopSimulation();
            fitness /= 10;
        }
    }

    public static Waypoint getWaypoint(AquaticDrone drone) {
        ArrayList<Entity> entities = drone.getEntities();
        for (Entity e : entities) {
            if (e instanceof Waypoint) {
                return (Waypoint) e;
            }
        }
        return null;
    }
    
    @Override
    public double getFitness() {
    	return Math.max(fitness,0);
    }

    public static double calculateDistance(Waypoint wp, AquaticDrone drone) {
        Vector2d pos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        Vector2d robotPos = new Vector2d(drone.getPosition().getX(), drone.getPosition().getY());
        return pos.distanceTo(robotPos);
    }
}