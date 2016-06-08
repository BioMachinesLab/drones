package environment.target;

import java.util.Arrays;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.target.Target;
import commoninterface.entities.target.TargetLinearMotionData;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MultiTargetEnvironment extends TargetEnvironment {
	private static final long serialVersionUID = 2179291959991705038L;
	private int targetsQuantity = 2;
	private boolean variateTargetsQuantity = false;

	public MultiTargetEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		targetsQuantity = args.getArgumentAsIntOrSetDefault("targetsQuantity", targetsQuantity);
		variateTargetsQuantity = args.getArgumentAsIntOrSetDefault("variateTargetsQnt", 0) == 1;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);

		int definedTargetsQuantity = targetsQuantity;
		if (variateTargetsQuantity) {
			definedTargetsQuantity += definedTargetsQuantity * simulator.getRandom().nextDouble() * 3;
			if (definedTargetsQuantity == 0) {
				definedTargetsQuantity = 1;
			}
		}

		targets = new Target[definedTargetsQuantity];

		for (int i = 0; i < definedTargetsQuantity; i++) {
			double x_pos = radiusOfObjPositioning * 2 * simulator.getRandom().nextDouble() - radiusOfObjPositioning;
			double y_pos = radiusOfObjPositioning * 2 * simulator.getRandom().nextDouble() - radiusOfObjPositioning;
			LatLon latLon = CoordinateUtilities.cartesianToGPS(x_pos, y_pos);
			Target target = new Target("target" + i, latLon);

			if (i != 0) {
				while (!safeForTarget(target, simulator)) {
					positionTargetInRandomPos(target, simulator);
					updateCollisions(0);
				}
			}

			targets[i] = target;

			if (moveTargets) {
				double targetVelocity = movementVelocity;
				if (variateTargetVelocity) {
					targetVelocity += targetVelocity * simulator.getRandom().nextDouble() * 0.1;
				}

				double orientation = simulator.getRandom().nextDouble() * Math.PI * 2;
				motionData.put(target, new TargetLinearMotionData(target, targetVelocity, orientation));
			}
		}

		for (Robot r : robots) {
			do {
				positionDroneInRandomPos((AquaticDrone) r, simulator);
				updateCollisions(0);
			} while (!safeForRobot(r, simulator));

			((AquaticDroneCI) r).getEntities().addAll(Arrays.asList(targets));
		}

		setup = true;
	}
}
