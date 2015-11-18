/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fieldtests.evaluation;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.CISensorWrapper;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.sensors.DroneCISensor;
import commoninterface.utils.CoordinateUtilities;
import evaluation.AvoidCollisionsFunction;

/**
 *
 * @author jorge
 */
public class DispersionFitnessTest extends AvoidCollisionsFunction {

    private double meanDistance = 0;
    private double margin = 0;
    private boolean config = false;
    private double targetDistance = 0;
    private double range = 0;
    private double startingDistance = 0;
    private boolean lastSteps = false;
    private double ls = 0;
    private boolean adaptive = false;
    private boolean useGPS = false;
    
    public DispersionFitnessTest(Arguments args) {
        super(args);
        margin = args.getArgumentAsDouble("margin");
        range = args.getArgumentAsDoubleOrSetDefault("range", range);
        startingDistance = args.getArgumentAsDoubleOrSetDefault("startingdistance", startingDistance);
        lastSteps = args.getFlagIsTrue("laststeps");
        useGPS = args.getFlagIsTrue("usegps");
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
            
            if(adaptive && r.getId() > 3 && simulator.getTime() < 60*10+120*10)
            	continue;
            
            for (Robot r2 : simulator.getRobots()) {
            	
//              758 steps all dispersing, 1138 all stop ADAPTIVE
                //TODO REMOVE THIS
                if(adaptive && r2.getId() > 3 && simulator.getTime() < 60*10+120*10)
                	continue;
            	
                if (r != r2) {
                	if(!useGPS)
                		minDist = Math.min(minDist, r.getPosition().distanceTo(r2.getPosition()));
                	else
                		minDist = Math.min(minDist,((AquaticDrone)r).getGPSLatLon().distanceInKM(((AquaticDrone)r2).getGPSLatLon())*1000);
//                	System.out.println(minDist);
                }
            }
//            System.out.println(r.getId()+" "+minDist + " "+(minDist - targetDistance));
            distanceDelta += Math.abs(minDist - targetDistance);
        }
        
        if(!adaptive) {
        	distanceDelta /= simulator.getRobots().size();
//        	System.out.println(distanceDelta);
        } else {
        	distanceDelta /= simulator.getTime() < 60*10+120*10 ? 4 : simulator.getRobots().size();
        	System.out.println(distanceDelta+" "+(simulator.getTime() < 60*10+120*10 ? 4 : simulator.getRobots().size()));
        }
//        System.out.println(simulator.getTime()+"  "+distanceDelta);
//        System.out.println();
        
        meanDistance += (startingDistance - distanceDelta) / startingDistance;
        
        if(lastSteps) {
        	if(simulator.getTime() > simulator.getEnvironment().getSteps() - 100) {
        		ls++;
        		fitness+=distanceDelta;
        		
        	} else {
        		fitness = 0;
        	}
        }else {
        	fitness = meanDistance / Math.max(simulator.getTime(),1);
        }

        super.update(simulator);
    }

    @Override
    public double getFitness() {
//    	System.out.println();
    	if(lastSteps) {
    		return fitness/ls;
    	}else{
    		return 10 + fitness;
    	}
    }
}
