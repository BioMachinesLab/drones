package fieldtests.evaluation;

import commoninterface.AquaticDroneCI.DroneType;
import evaluation.AvoidCollisionsFunction;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class Coverage2FitnessTest extends AvoidCollisionsFunction {
	private static final long serialVersionUID = -488771630943339840L;
	private double steps = 0;

	public Coverage2FitnessTest(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {

		double distance = 0;
		int measurements = 0;
		steps++;

		for (int i = 0; i < simulator.getRobots().size(); i++) {
			Robot r = simulator.getRobots().get(i);
			AquaticDrone drone = (AquaticDrone) r;

			if (drone.getDroneType() == DroneType.DRONE) {

				double closest = Double.MAX_VALUE;

				for (int j = 0; j < simulator.getRobots().size(); j++) {

					if (i == j)
						continue;

					Robot r2 = simulator.getRobots().get(j);
					AquaticDrone drone2 = (AquaticDrone) r2;

					if (drone2.getDroneType() == DroneType.DRONE) {
						double d = drone.getGPSLatLon().distanceInMeters(drone2.getGPSLatLon());
						if (closest > d)
							closest = d;
					}
				}

				measurements++;
				distance += closest;
			}
		}

		this.fitness += distance / measurements;
	}

	@Override
	public double getFitness() {
		return fitness / steps;
	}

}
