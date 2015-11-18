/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fieldtests.evaluation;

import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import evaluation.AvoidCollisionsFunction;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class AggregateWaypointFitnessTest extends AvoidCollisionsFunction {

    private boolean configured = false;
    private double startingDistance = 0;
    private double meanDistance = 0;
    private Waypoint wp = null;
    private double savedDistance = 0;
    private int numberChanges = 0;
    private int targetwp = 0;

    public AggregateWaypointFitnessTest(Arguments args) {
        super(args);
        targetwp = args.getArgumentAsIntOrSetDefault("targetwp", targetwp);
    }

    @Override
    public void update(Simulator simulator) {
    	
        if (!configured) {
            wp = ((AquaticDrone) simulator.getRobots().get(0)).getActiveWaypoint();
            for (Robot r : simulator.getRobots()) {
                startingDistance += calculateDistance(wp, r);
            }
            startingDistance /= simulator.getRobots().size();
            configured = true;
        }

        // MEAN DISTANCE TO WAYPOINT
        
        double minDistance = Double.MAX_VALUE;
        double maxDistance = 0;
        
        double currentDistance = 0;
        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            double dist = calculateDistance(wp, drone);;
            currentDistance += dist;
            
            minDistance = Math.min(dist,minDistance);
            maxDistance = Math.max(dist,maxDistance);
        }
        currentDistance /= simulator.getRobots().size();
        
        System.out.println(currentDistance);
        
        if(targetwp == 3)
        	savedDistance = currentDistance;
        
        if(!((AquaticDrone) simulator.getRobots().get(0)).getActiveWaypoint().equals(wp)) {
    		wp = ((AquaticDrone) simulator.getRobots().get(0)).getActiveWaypoint();
    		if(numberChanges == targetwp)
    			savedDistance=currentDistance;
    		numberChanges++;
    	}
        
        meanDistance += (startingDistance - currentDistance) / startingDistance;
        fitness = meanDistance / simulator.getTime();
        
        super.update(simulator);
    }

    @Override
    public double getFitness() {
//        return 10 + fitness;
    	return savedDistance;
    }

    static double calculateDistance(Waypoint wp, Robot drone) {
        Vector2d pos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        Vector2d robotPos = new Vector2d(drone.getPosition().getX(), drone.getPosition().getY());
        return pos.distanceTo(robotPos);
    }
}
