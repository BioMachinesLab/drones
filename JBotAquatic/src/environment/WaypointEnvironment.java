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

public class WaypointEnvironment extends Environment {

    @ArgumentsAnnotation(name = "numberwaypoints", defaultValue = "1")
    private int nWaypoints = 1;
    @ArgumentsAnnotation(name = "distance", defaultValue = "0")
    private double distance = 0;
    @ArgumentsAnnotation(name = "random", defaultValue = "0")
    private double random = 0;
    @ArgumentsAnnotation(name = "otherrobots", defaultValue = "0")
    private int otherRobots = 0;

    private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();

    public WaypointEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        nWaypoints = args.getArgumentAsIntOrSetDefault("numberwaypoints", nWaypoints);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        random = args.getArgumentAsDoubleOrSetDefault("random", random);
        otherRobots = args.getArgumentAsIntOrSetDefault("otherrobots", 0);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);

        for (Robot r : simulator.getRobots()) {
            r.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
        }

        for (int i = 0; i < otherRobots; i++) {
            AquaticDrone drone = new AquaticDrone(simulator, new Arguments("gpserror=1.0,radius=0.30,diameter=0.60,commrange=0.01,rudder=1"));

            double dist = (distance) * simulator.getRandom().nextDouble() + 5;
            double angle = simulator.getRandom().nextDouble() * Math.PI * 2;
            double x = Math.cos(angle) * dist;
            double y = Math.sin(angle) * dist;

            drone.setPosition(x, y);
            addRobot(drone);
        }

        for (int i = 0; i < nWaypoints; i++) {
            double dist = distance + distance * random * simulator.getRandom().nextDouble() * 2 - random;

            double angle = simulator.getRandom().nextDouble() * Math.PI * 2;

            double x = Math.cos(angle) * (dist > 0 ? dist : width / 2 / 3);
            double y = Math.sin(angle) * (dist > 0 ? dist : width / 2 / 3);

            LatLon latLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x, y));
            Waypoint wp = new Waypoint("wp" + i, latLon);

            waypoints.add(wp);

            for (Robot r : simulator.getRobots()) {
                ((AquaticDroneCI) r).getEntities().add(wp);
                if (i == 0) {
                    ((AquaticDroneCI) r).setActiveWaypoint(wp);
                }
            }
            LightPole lp = new LightPole(simulator, "wp" + i, x, y, 1.5);
            addObject(lp);
        }
    }

    @Override
    public void update(double time) {

    }
}
