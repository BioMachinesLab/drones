package environment.target;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.target.Target;
import commoninterface.entities.target.motion.LinearMotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import net.jafama.FastMath;
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

		for (int i = 0; i < definedTargetsQuantity; i++) {
			double radius = radiusOfObjPositioning * simulator.getRandom().nextDouble();
			double orientation = (simulator.getRandom().nextDouble() * FastMath.PI * 2) % 360;
			Vector2d position = new Vector2d(radius * FastMath.cos(orientation), radius * FastMath.sin(orientation));
			LatLon latLon = CoordinateUtilities.cartesianToGPS(position);
			Target target = new Target("target" + i, latLon, targetRadius);

			if (i != 0) {
				while (!safeForTarget(target, simulator)) {
					positionTargetInRandomPos(target, simulator);
					updateCollisions(0);
				}
			}

			targets.add(target);

			if (moveTargets) {
				for (Target t : targets) {
					LinearMotionData lmd = new LinearMotionData(t, t.getLatLon(), movementVelocity, movementAzimuth);
					t.setMotionData(lmd);
				}
			}
		}

		for (Robot r : robots) {
			do {
				positionDroneInRandomPos((AquaticDrone) r, simulator);
				updateCollisions(0);
			} while (!safeForRobot(r, simulator));

			((AquaticDroneCI) r).getEntities().addAll(targets);
			
			if(moveTargets){
				((AquaticDroneCI) r).setUpdateEntities(true);
			}
		}

		setup = true;
	}
}
