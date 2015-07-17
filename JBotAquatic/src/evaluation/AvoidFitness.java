/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class AvoidFitness extends EvaluationFunction {

    private double safetyDistance = 7.5;
    private double usedEnergy = 0;
    private double timeInDanger = 0;

    public AvoidFitness(Arguments args) {
        super(args);
        safetyDistance = args.getArgumentAsDouble("safetydistance");
    }

    @Override
    public void update(Simulator simulator) {
        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        usedEnergy += (Math.abs(drone.getLeftMotorSpeed()) + Math.abs(drone.getRightMotorSpeed())) / 2d;

        for(int i = 1 ; i < simulator.getRobots().size() ; i++) {
            double dist = drone.getPosition().distanceTo(simulator.getRobots().get(i).getPosition());
            if(dist < safetyDistance) {
                timeInDanger++;
            }
        }
        
        fitness = 1 - timeInDanger / simulator.getEnvironment().getSteps();
        if (fitness > 0.95) {
            fitness = fitness + 1 - usedEnergy / simulator.getEnvironment().getSteps();
        }
        if(!insideLines(drone.getPosition(), simulator)) {
            fitness = 0;
            simulator.stopSimulation();
        }        
    }

    public boolean insideLines(Vector2d v, Simulator sim) {
        //http://en.wikipedia.org/wiki/Point_in_polygon
        int count = 0;
        for (PhysicalObject p : sim.getEnvironment().getAllObjects()) {
            if (p.getType() == PhysicalObjectType.LINE) {
                Line l = (Line) p;
                if (l.intersectsWithLineSegment(v, new Vector2d(0, -1000)) != null) {
                    count++;
                }
            }
        }
        return count % 2 != 0;
    }

    @Override
    public double getFitness() {
        return Math.max(0,fitness);
    }

}
