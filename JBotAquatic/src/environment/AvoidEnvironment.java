package environment;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import controllers.GoToWayPointController;
import java.util.ArrayList;

public class AvoidEnvironment extends BoundaryEnvironment {

    protected int otherRobots = 2;

    public AvoidEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        otherRobots = args.getArgumentAsIntOrSetDefault("otherrobots", otherRobots);
    }

    @Override
    protected void placeRobots(Simulator simulator) {
        // Evolved drone
        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        double a = simulator.getRandom().nextDouble() * Math.PI * 2;
        double x = (dronesDistance / 2) * simulator.getRandom().nextDouble() * Math.cos(a);
        double y = (dronesDistance / 2) * simulator.getRandom().nextDouble() * Math.sin(a);
        drone.setPosition(x, y);
        drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);

        // Other attackers
        for (int i = 0; i < otherRobots; i++) {
            ArrayList<Vector2d> wpPos = new ArrayList<Vector2d>();
            for (int j = 0; j < 4; j++) {
                double angle = simulator.getRandom().nextDouble() * Math.PI * 2;
                Vector2d pos = new Vector2d(wallsDistance * 1.25 * Math.cos(angle), wallsDistance * 1.25 * Math.sin(angle));
                wpPos.add(pos);
            }
            ArrayList<Waypoint> wps = new ArrayList<Waypoint>();
            for (int j = 1; j < wpPos.size(); j++) {
                wps.add(new Waypoint("wp" + j, CoordinateUtilities.cartesianToGPS(wpPos.get(j))));
            }
            AquaticDrone other = new AquaticDrone(simulator, new Arguments("diameter=1,gpserror=1.0,commrange=40,avoiddrones=0"));
            other.setPosition(wpPos.get(0).x, wpPos.get(0).y);
            GoToWayPointController controller = new GoToWayPointController(simulator, other, new Arguments(""));
            other.setController(controller);
            other.getEntities().addAll(wps);
            addRobot(other);
            other.setActiveWaypoint(wps.get(1));
        }
    }
}
