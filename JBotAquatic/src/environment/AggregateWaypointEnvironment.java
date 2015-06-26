package environment;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import commoninterface.AquaticDroneCI;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class AggregateWaypointEnvironment extends Environment {

    @ArgumentsAnnotation(name = "distance", defaultValue = "0")
    private double distance = 0;
    @ArgumentsAnnotation(name = "waypointdistance", defaultValue = "0")
    private double wpDistance = 0;

    public AggregateWaypointEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        wpDistance = args.getArgumentAsDoubleOrSetDefault("waypointdistance", wpDistance);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);

        double dist = (wpDistance) * simulator.getRandom().nextDouble();
        double angle = simulator.getRandom().nextDouble() * Math.PI * 2;
        double x = Math.cos(angle) * (dist > 0 ? dist : width / 2 / 3);
        double y = Math.sin(angle) * (dist > 0 ? dist : width / 2 / 3);
        LatLon latLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x, y));
        Waypoint wp = new Waypoint("wp", latLon);
        LightPole lp = new LightPole(simulator, "wp", x, y, 1.5);
        addObject(lp);
        
        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            dist = (distance) * simulator.getRandom().nextDouble();
            angle = simulator.getRandom().nextDouble() * Math.PI * 2;
            x = Math.cos(angle) * (dist > 0 ? dist : width / 2 / 3);
            y = Math.sin(angle) * (dist > 0 ? dist : width / 2 / 3);
            drone.setPosition(x, y);
            drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
            drone.getEntities().add(wp);
            drone.setActiveWaypoint(wp);
        }
    }

    @Override
    public void update(double time) {

    }
}
