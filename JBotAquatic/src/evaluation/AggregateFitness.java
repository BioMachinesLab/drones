/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import java.util.Collection;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class AggregateFitness extends AvoidCollisionsFunction {

    private boolean configured = false;
    private double meanDistance = 0;
    private double startingDistance = 0;

    public AggregateFitness(Arguments args) {
        super(args);
    }

    @Override
    public void update(Simulator simulator) {
        if (!configured) {
            startingDistance = calculateDistCM(simulator.getRobots());
            configured = true;
        }

        // MEAN DISTANCE TO CENTRE OF MASS
        double currDistance = calculateDistCM(simulator.getRobots());
        meanDistance += (startingDistance - currDistance) / startingDistance;
        fitness = meanDistance / simulator.getTime();

        super.update(simulator);
    }

    @Override
    public double getFitness() {
        return 10 + fitness;
    }

    private double calculateDistCM(Collection<Robot> robots) {
        mathutils.Vector2d centreMass = new mathutils.Vector2d();
        for (Robot r : robots) {
            centreMass.add(r.getPosition());
        }
        centreMass.x = centreMass.x / robots.size();
        centreMass.y = centreMass.y / robots.size();

        double currentDistance = 0;
        for (Robot r : robots) {
            AquaticDrone drone = (AquaticDrone) r;
            currentDistance += drone.getPosition().distanceTo(centreMass);
        }
        return currentDistance / robots.size();
    }
}
