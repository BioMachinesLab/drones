/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class ClusterFitness extends AvoidCollisionsFunction {

    private double clusterDistance = 5;
    private int totalClusters = 0;

    public ClusterFitness(Arguments args) {
        super(args);
        clusterDistance = args.getArgumentAsDoubleOrSetDefault("clusterdistance", clusterDistance);
    }

    @Override
    public void update(Simulator simulator) {
        int numClusters = numberOfClusters(simulator.getRobots());
        int numRobots = simulator.getRobots().size();

        totalClusters += numClusters;
        fitness = (numRobots - totalClusters / simulator.getTime()) / (double) (numRobots - 1);
        super.update(simulator);
    }

    @Override
    public double getFitness() {
        return 10 + fitness;
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
                            if (r1.getPosition().distanceTo(r2.getPosition()) - r1.getRadius() - r2.getRadius() <= clusterDistance) {
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
