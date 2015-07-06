package environment;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class AggregateWaypointEnvironment extends OpenEnvironment {

    @ArgumentsAnnotation(name = "waypointdistance", defaultValue = "0")
    private double wpDistance = 0;

    public AggregateWaypointEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        wpDistance = args.getArgumentAsDoubleOrSetDefault("waypointdistance", wpDistance);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);

        double d = (wpDistance) * simulator.getRandom().nextDouble();
        double angle = simulator.getRandom().nextDouble() * Math.PI * 2;
        double x = Math.cos(angle) * d;
        double y = Math.sin(angle) * d;
        LatLon latLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x, y));
        Waypoint wp = new Waypoint("wp", latLon);
        LightPole lp = new LightPole(simulator, "wp", x, y, 1.5);
        addObject(lp);

        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            drone.getEntities().add(wp);
            drone.setActiveWaypoint(wp);
        }
    }
}
