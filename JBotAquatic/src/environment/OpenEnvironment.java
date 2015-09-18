package environment;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class OpenEnvironment extends Environment {

    @ArgumentsAnnotation(name = "distance", defaultValue = "0")
    protected double distance = 0;
    protected double maxDistance = 0;
    protected double safetyDistance = 0;

    public OpenEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        safetyDistance = args.getArgumentAsDoubleOrSetDefault("safetydistance", safetyDistance);
        maxDistance = args.getArgumentAsDoubleOrSetDefault("maxdistance", maxDistance);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);
        for (Robot r : simulator.getRobots()) {

            do {
                positionDrone((AquaticDrone) r, simulator);
                simulator.updatePositions(0);
            } while (!safe(r, simulator));

        }
        this.setup = true;
    }

    protected boolean safe(Robot r, Simulator simulator) {

        if (r.isInvolvedInCollison()) {
            return false;
        }

        if (safetyDistance > 0) {

            double min = Double.MAX_VALUE;

            for (Robot robot : robots) {

                if (robot.getId() == r.getId()) {
    				//all robots with a lower ID are at safe distances, so we can exit

                    if (maxDistance > 0) {
                        return min < maxDistance || robot.getId() == 0;
                    }

                    return true;
                }

                double d = robot.getPosition().distanceTo(r.getPosition());

                if (d < safetyDistance) {
                    return false;
                }

                min = Math.min(d, min);
            }

        }

        return true;
    }

    protected void positionDrone(AquaticDrone drone, Simulator simulator) {

        double x = distance * 2 * simulator.getRandom().nextDouble() - distance;
        double y = distance * 2 * simulator.getRandom().nextDouble() - distance;
        drone.setPosition(x, y);
        drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
    }

    @Override
    public void update(double time) {

    }
}
