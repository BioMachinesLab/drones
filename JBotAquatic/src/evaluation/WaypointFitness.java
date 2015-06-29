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

/**
 *
 * @author jorge
 */
public class WaypointFitness extends EvaluationFunction {

    private boolean configured = false;
    private double startingDistance = 0;
    private double targetDistance = 1.5;
    private int steps = 0;
    private boolean kill = true;
    private double usedEnergy = 0;

    public WaypointFitness(Arguments args) {
        super(args);
        targetDistance = args.getArgumentAsDouble("targetdistance");
        kill = args.getFlagIsTrue("kill");
    }

    @Override
    public void update(Simulator simulator) {
        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        Waypoint wp = drone.getActiveWaypoint();
        if (!configured) {
            steps = simulator.getEnvironment().getSteps();
            startingDistance = calculateDistance(wp, drone);
        }
        configured = true;

        Vector2d wpPos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        double distance = wpPos.distanceTo(new Vector2d(drone.getPosition().x, drone.getPosition().y));
        double energy = (Math.abs(drone.getLeftMotorSpeed()) + Math.abs(drone.getRightMotorSpeed())) / 2;

        usedEnergy += (distance <= targetDistance ? energy : 1);
        fitness = (startingDistance - distance) / startingDistance + 1 - (usedEnergy / steps);

        if (kill && drone.isInvolvedInCollison()) {
            simulator.stopSimulation();
            fitness /= 10;
        }
    }

    @Override
    public double getFitness() {
        return fitness + 10;
    }

    public static double calculateDistance(Waypoint wp, AquaticDrone drone) {
        Vector2d pos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
        Vector2d robotPos = new Vector2d(drone.getPosition().getX(), drone.getPosition().getY());
        return pos.distanceTo(robotPos);
    }
}
