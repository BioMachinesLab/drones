package environment;


import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class OpenEnvironment extends Environment {

    @ArgumentsAnnotation(name = "distance", defaultValue = "0")
    private double distance = 0;

    public OpenEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);
        for (Robot r : simulator.getRobots()) {
            AquaticDrone drone = (AquaticDrone) r;
            double dist = (distance) * simulator.getRandom().nextDouble();
            double angle = simulator.getRandom().nextDouble() * Math.PI * 2;
            double x = Math.cos(angle) * (dist > 0 ? dist : width / 2 / 3);
            double y = Math.sin(angle) * (dist > 0 ? dist : width / 2 / 3);
            drone.setPosition(x, y);
            drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
        }
    }

    @Override
    public void update(double time) {

    }
}
