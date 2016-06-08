package environment.target;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.target.Target;
import commoninterface.entities.target.TargetLinearMotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MonoTargetEnvironment extends TargetEnvironment {
	private static final long serialVersionUID = -8803488309063954990L;

	public MonoTargetEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);

		double x_pos = radiusOfObjPositioning * 2 * simulator.getRandom().nextDouble() - radiusOfObjPositioning;
		double y_pos = radiusOfObjPositioning * 2 * simulator.getRandom().nextDouble() - radiusOfObjPositioning;

		LatLon latLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x_pos, y_pos));
		targets = new Target[1];
		targets[0] = new Target("target", latLon);

		if (moveTargets) {
			double targetVelocity = movementVelocity;
			if (variateTargetVelocity) {
				targetVelocity += targetVelocity * simulator.getRandom().nextDouble() * 0.1;
			}

			double orientation = simulator.getRandom().nextDouble() * Math.PI * 2;
			motionData.put(targets[0], new TargetLinearMotionData(targets[0], targetVelocity, orientation));
		}

		for (Robot r : robots) {
			do {
				positionDroneInRandomPos((AquaticDrone) r, simulator);
				updateCollisions(0);
			} while (!safeForRobot(r, simulator));
		
			((AquaticDroneCI) r).getEntities().add(targets[0]);
		}

		setup = true;
	}
}
