package environment;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import controllers.GoToWayPointController;
import java.util.ArrayList;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.util.ArgumentsAnnotation;

public class StationKeepingAvoidEnvironment extends Environment {

    @ArgumentsAnnotation(name = "distance", defaultValue = "5")
    protected double distance = 5;

    public StationKeepingAvoidEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
    }

    @Override
    public void setup(Simulator simulator) {
        simulator.getRandom().nextDouble();
        this.setup = true;

        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            double dist = (distance) * simulator.getRandom().nextDouble();
            double angle = simulator.getRandom().nextDouble() * Math.PI * 2;
            double x = Math.cos(angle) * (dist > 0 ? dist : width / 2 / 3);
            double y = Math.sin(angle) * (dist > 0 ? dist : width / 2 / 3);
            drone.setPosition(x, y);
            drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);

            Waypoint wp = new Waypoint("wp." + drone.getName(), CoordinateUtilities.cartesianToGPS(x, y));
            drone.getEntities().add(wp);
            drone.setActiveWaypoint(wp);
            LightPole lp = new LightPole(simulator, "wp." + drone.getName(), x, y, 1.5);
            addObject(lp);
        }

        ArrayList<Waypoint> wps = new ArrayList<Waypoint>();
        double d = distance * 0.8;
        wps.add(new Waypoint("wpb", CoordinateUtilities.cartesianToGPS(d * -0.5,d * 0.8666)));
        wps.add(new Waypoint("wpc", CoordinateUtilities.cartesianToGPS(d * -0.5,d * -0.8666)));
        wps.add(new Waypoint("wpa", CoordinateUtilities.cartesianToGPS(d, 0)));
        wps.add(new Waypoint("wpa", CoordinateUtilities.cartesianToGPS(-0.5 * d, 0)));

        AquaticDrone drone = new AquaticDrone(simulator, new Arguments("diameter=4,gpserror=1.0,commrange=40,avoiddrones=0"));
        drone.setDroneType(DroneType.ENEMY);
        drone.setPosition(d, 0);
        drone.setOrientation(Math.PI / 1.2);
        GoToWayPointController controller = new GoToWayPointController(simulator, drone, new Arguments(""));
        drone.setController(controller);
        drone.getEntities().addAll(wps);
        addRobot(drone);
        drone.setActiveWaypoint(wps.get(0));
    }

    @Override
    public void update(double time) {

    }
}
