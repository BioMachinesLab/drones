/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fieldtests.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import evaluation.AvoidCollisionsFunction;

/**
 *
 * @author jorge
 */
public class ClusterFitnessWeightedTest extends AvoidCollisionsFunction {

    private double clusterDistance = 5;
    private double totalClusters = 0;
    private double totalNorm = 0;
    private boolean useGPS = false;
    private boolean lastSteps = false;
    private boolean min = false;
    private double ls = 0;
    private double currentMin = Double.MAX_VALUE;
    
    public ClusterFitnessWeightedTest(Arguments args) {
        super(args);
        clusterDistance = args.getArgumentAsDoubleOrSetDefault("clusterdistance", clusterDistance);
        useGPS = args.getFlagIsTrue("usegps");
        lastSteps = args.getFlagIsTrue("laststeps");
        min = args.getFlagIsTrue("min");
    }

    @Override
    public void update(Simulator simulator) {
        int numClusters = numberOfClusters(simulator.getRobots());
        int numRobots = simulator.getRobots().size();
        
        double weight = simulator.getTime() / simulator.getEnvironment().getSteps();
        totalNorm += weight;
        totalClusters += numClusters * weight;
        
        if(min && numClusters < currentMin) {
        	currentMin = numClusters;
        }
        
        if(lastSteps) {
        	if(simulator.getTime() > simulator.getEnvironment().getSteps() - 300) {
        		ls++;
        		fitness+=numClusters;
        	} else {
        		fitness = 0;
        	}
        } else {
        	fitness = (numRobots - totalClusters / totalNorm) / (double) (numRobots - 1);
        }
        
        super.update(simulator);
    }

    @Override
    public double getFitness() {
    	
    	if(min)
    		return currentMin;
    	
    	if(lastSteps) {
    		return fitness/ls;
    	}else{
    		return 10 + fitness;
    	}
    }

    // bottom-up single-linked clustering
    private int numberOfClusters(Collection<Robot> robots) {
        List<List<Robot>> clusters = new ArrayList<>();
        // One cluster for each robot
        for (Robot r : robots) {
            List<Robot> cluster = new ArrayList<>();
            cluster.add(r);
            clusters.add(cluster);
        }

        boolean merged = true;
        // stop when the clusters cannot be merged anymore
        
        while(merged) {
            merged = false;
            // find two existing clusters to merge
            for (int i = 0; i < clusters.size() && !merged; i++) {
                for (int j = i + 1; j < clusters.size() && !merged; j++) {
                    List<Robot> c1 = clusters.get(i);
                    List<Robot> c2 = clusters.get(j);
                    // check if the two clusters have (at least) one individual close to the other
                    for (int ri = 0; ri < c1.size() && !merged; ri++) {
                        for (int rj = 0; rj < c2.size() && !merged; rj++) {
                            Robot r1 = c1.get(ri);
                            Robot r2 = c2.get(rj);
                            
                            if(useGPS) {
                            	
                            	LatLon r1pos = ((AquaticDrone)r1).getGPSLatLon();
                            	LatLon r2pos = ((AquaticDrone)r2).getGPSLatLon();
                            	
                            	double distance = CoordinateUtilities.distanceInMeters(r1pos, r2pos);
                            	
                            	if(distance -r1.getRadius() - r2.getRadius() <= clusterDistance) {
                            		// do the merge
	                                merged = true;
	                                clusters.get(i).addAll(clusters.get(j));
	                                clusters.remove(j);
                            	}
                            	
                            } else if (r1.getPosition().distanceTo(r2.getPosition()) - r1.getRadius() - r2.getRadius() <= clusterDistance) {
	                                // do the merge
                                merged = true;
                                clusters.get(i).addAll(clusters.get(j));
                                clusters.remove(j);
                            }
                        }
                    }
                }
            }
        }
        return clusters.size();
    }
}

/*List<List<Robot>> clusters = new LinkedList<>();
 for (Robot r : robots) {
 boolean added = false;
 // Try to add to an existing cluster
 Iterator<List<Robot>> clustersIter = clusters.iterator();
 while (clustersIter.hasNext() && !added) {
 List<Robot> cluster = clustersIter.next();
 Iterator<Robot> clusterRobots = cluster.iterator();
 // Check if there is a close neighbour in this existing cluster
 while (clusterRobots.hasNext() && !added) {
 Robot r2 = clusterRobots.next();
 if (r.getPosition().distanceTo(r2.getPosition()) - r.getRadius() - r2.getRadius() < clusterDistance) {
 cluster.add(r);
 added = true;
 }
 }
 }
 // If it was far away from all existing clusters, createnew one
 if (!added) {
 List<Robot> cluster = new LinkedList<>();
 cluster.add(r);
 clusters.add(cluster);
 }
 }*/
