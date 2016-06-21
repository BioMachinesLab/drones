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

public class MonoTargetEnvironment extends TargetEnvironment {
	private static final long serialVersionUID = -8803488309063954990L;

	public MonoTargetEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);

		double radius = radiusOfObjPositioning * simulator.getRandom().nextDouble();
		double orientation = (simulator.getRandom().nextDouble() * FastMath.PI * 2) % 360;
		Vector2d position = new Vector2d(radius * FastMath.cos(orientation), radius * FastMath.sin(orientation));

		LatLon latLon = CoordinateUtilities.cartesianToGPS(position);
		targets.add(new Target("target", latLon, targetRadius));

		if (moveTargets) {
			LinearMotionData lmd = new LinearMotionData(targets.get(0), movementVelocity, movementAzimuth);
			targets.get(0).setMotionData(lmd);
		}

		for (Robot r : robots) {
			do {
				positionDroneInRandomPos((AquaticDrone) r, simulator);
				updateCollisions(0);
			} while (!safeForRobot(r, simulator));

			((AquaticDroneCI) r).getEntities().add(targets.get(0));

			if (moveTargets) {
				((AquaticDroneCI) r).setUpdateEntities(true);
			}
		}

		setup = true;
	}
}
