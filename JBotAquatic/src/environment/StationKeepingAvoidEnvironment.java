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

        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        double x = 0, y = 0;
        drone.setPosition(x, y);
        drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
        Waypoint wp = new Waypoint("wp." + drone.getName(), CoordinateUtilities.cartesianToGPS(x, y));
        drone.getEntities().add(wp);
        drone.setActiveWaypoint(wp);
        LightPole lp = new LightPole(simulator, "wp." + drone.getName(), x, y, 1.5);
        addObject(lp);
        
        double angle = simulator.getRandom().nextDouble() * Math.PI * 2;
        ArrayList<Waypoint> wps = new ArrayList<Waypoint>();
        wps.add(new Waypoint("wpb", CoordinateUtilities.cartesianToGPS(distance * Math.cos(angle),distance * Math.sin(angle))));
        double angle2 = angle + (simulator.getRandom().nextDouble() - 0.5) * (Math.PI / 4) - Math.PI;
        wps.add(new Waypoint("wpc", CoordinateUtilities.cartesianToGPS(distance * Math.cos(angle2),distance * Math.sin(angle2))));
        double angle3 = angle + (simulator.getRandom().nextDouble() - 0.5) * (Math.PI / 4);
        wps.add(new Waypoint("wpd", CoordinateUtilities.cartesianToGPS(distance * Math.cos(angle3),distance * Math.sin(angle3))));

        AquaticDrone other = new AquaticDrone(simulator, new Arguments("diameter=1,gpserror=1.0,commrange=40,avoiddrones=0"));
        other.setDroneType(DroneType.DRONE);
        other.setPosition(distance * Math.cos(angle2), distance * Math.sin(angle2));
        GoToWayPointController controller = new GoToWayPointController(simulator, other, new Arguments(""));
        other.setController(controller);
        other.getEntities().addAll(wps);
        addRobot(other);
        other.setActiveWaypoint(wps.get(0));
    }

    @Override
    public void update(double time) {

    }
}
