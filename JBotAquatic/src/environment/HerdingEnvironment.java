/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package environment;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class HerdingEnvironment extends PredatorPreyEnvironment {

    private double waypointMinDistance;
    private double objectiveDistance;
    private Vector2d objective;

    public HerdingEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        this.waypointMinDistance = args.getArgumentAsDouble("waypointmindistance");
        this.objectiveDistance = args.getArgumentAsDouble("objectivedistance");
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);
        Vector2d point = null;
        boolean found = false;
        while (!found) {
            double r = minPreyDistance + simulator.getRandom().nextDouble() * (maxPreyDistance - minPreyDistance);
            double a = simulator.getRandom().nextDouble() * Math.PI * 2;
            point = new Vector2d(Math.cos(a) * r, Math.sin(a) * r);
            if (point.distanceTo(preyDrone.getPosition()) > waypointMinDistance) {
                found = true;
            }
        }

        objective = point;
        LatLon latLon = CoordinateUtilities.cartesianToGPS(point.x, point.y);
        Waypoint objectiveWP = new Waypoint("wp", latLon);
        for (Robot r : simulator.getRobots()) {
            ((AquaticDroneCI) r).getEntities().add(objectiveWP);
            ((AquaticDroneCI) r).setActiveWaypoint(objectiveWP);
        }
        LightPole lp = new LightPole(simulator, "wp", point.x, point.y, objectiveDistance);
        addObject(lp);
    }
    
    public double getObjectiveDistance() {
        return objectiveDistance;
    }
    
    public Vector2d getObjective() {
        return objective;
    }
}
