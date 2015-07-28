/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import java.util.Collection;
import java.util.Iterator;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class AggregateFitnessWeighted extends AvoidCollisionsFunction {

    private boolean configured = false;
    private double meanDistance = 0;
    private double startingDistance = 0;
    private double totalNorm = 0;

    public AggregateFitnessWeighted(Arguments args) {
        super(args);
        startingDistance = args.getArgumentAsDoubleOrSetDefault("startingdistance", startingDistance);
    }

    @Override
    public void update(Simulator simulator) {
        if (startingDistance == 0) {
            startingDistance = calculateDistCM(simulator.getRobots(),simulator);
        }

        double weight = simulator.getTime() / simulator.getEnvironment().getSteps();
        totalNorm += weight;
        
        // MEAN DISTANCE TO CENTRE OF MASS
        double currDistance = calculateDistCM(simulator.getRobots(),simulator);
        meanDistance += ((startingDistance - currDistance) / startingDistance)*weight;
        fitness = meanDistance / totalNorm;
//        System.out.println(simulator.getTime()+" MEAN "+meanDistance+" FITNESS "+fitness+" STARTING "+startingDistance+" CURRENT "+currDistance+" ROBOTS "+simulator.getRobots().size());
        super.update(simulator);
    }

    @Override
    public double getFitness() {
        return 10 + fitness;
    }

    private double calculateDistCM(Collection<Robot> robots, Simulator sim) {
        mathutils.Vector2d centreMass = new mathutils.Vector2d();
        for (Robot r : robots) {
            centreMass.add(r.getPosition());
        }
        centreMass.x = centreMass.x / robots.size();
        centreMass.y = centreMass.y / robots.size();
        
//        Iterator<PhysicalObject> i = sim.getEnvironment().getAllObjects().iterator();
//        
//        while(i.hasNext()) {
//        	if(i.next() instanceof LightPole)
//        		i.remove();
//        }
        
//        sim.getEnvironment().addStaticObject(new LightPole(sim, "lp", centreMass.x, centreMass.y, 1));
        
        double currentDistance = 0;
        for (Robot r : robots) {
            AquaticDrone drone = (AquaticDrone) r;
            currentDistance += drone.getPosition().distanceTo(centreMass);
        }
        
        return currentDistance / robots.size();
    }
}
