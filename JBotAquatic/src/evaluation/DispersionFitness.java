/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.CISensorWrapper;
import simulation.robot.Robot;
import simulation.util.Arguments;

import commoninterface.sensors.DroneCISensor;
import commoninterface.utils.CoordinateUtilities;

/**
 *
 * @author jorge
 */
public class DispersionFitness extends AvoidCollisionsFunction {

    private double meanDistance = 0;
    private double margin = 0;
    private boolean config = false;
    private double targetDistance = 0;
    private double range = 0;
    private double startingDistance = 0;
    
    public DispersionFitness(Arguments args) {
        super(args);
        margin = args.getArgumentAsDouble("margin");
        range = args.getArgumentAsDoubleOrSetDefault("range", range);
        startingDistance = args.getArgumentAsDoubleOrSetDefault("startingdistance", startingDistance);
    }

    @Override
    public void update(Simulator simulator) {
        if (!config) {
        	
        	if(startingDistance == 0)
        		startingDistance = simulator.getEnvironment().getWidth();
        	
        	//TODO
        	range=40;
        	
        	if(range == 0) {
	            CISensorWrapper wr = (CISensorWrapper)simulator.getRobots().get(0).getSensorByType(CISensorWrapper.class);
	            DroneCISensor dcs = (DroneCISensor) wr.getCisensor();
	            range = dcs.getRange();
        	}
            targetDistance = range - margin;
            config = true;
        }
        
        // MEAN DISTANCE TO CLOSEST ROBOT
        double distanceDelta = 0;
        for (Robot r : simulator.getRobots()) {
            double minDist = Double.POSITIVE_INFINITY;
            for (Robot r2 : simulator.getRobots()) {
                if (r != r2) {
                	minDist = Math.min(minDist, r.getPosition().distanceTo(r2.getPosition()));
                }
            }
//            System.out.println(r.getId()+" "+minDist);
            distanceDelta += Math.abs(minDist - targetDistance);
        }
        
        distanceDelta /= simulator.getRobots().size();
        
//        System.out.println(simulator.getTime()+"  "+distanceDelta);
//        System.out.println();
        
        meanDistance += (startingDistance - distanceDelta) / startingDistance;
        
        fitness = meanDistance / Math.max(simulator.getTime(),1);

        super.update(simulator);
    }

    @Override
    public double getFitness() {
        return 10 + fitness;
    }
}
