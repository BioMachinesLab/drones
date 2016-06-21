package environment.target;

import java.util.ArrayList;
import java.util.HashMap;

import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

public abstract class TargetEnvironment extends Environment {
	private static final long serialVersionUID = -1681466103802101133L;
	protected Simulator simulator;
	protected Arguments args;
	protected boolean moveTargets = false;
	protected double radiusOfObjPositioning = 20;
	protected double safetyDistance = 1;
	protected double targetRadius = 1;
	protected double holdDistanceToTarget = 0;

	protected double movementVelocity = 0.2;
	protected boolean variateTargetVelocity = false;
	protected double movementAzimuth = 0;
	protected boolean variateTargetAzimuth = true;

	protected ArrayList<Target> targets = new ArrayList<Target>();
	protected HashMap<String, LightPole> markers = new HashMap<String, LightPole>();

	public TargetEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.args = args;
		this.simulator = simulator;
		radiusOfObjPositioning = args.getArgumentAsDoubleOrSetDefault("radiusOfObjectPositioning",
				radiusOfObjPositioning);
		safetyDistance = args.getArgumentAsDoubleOrSetDefault("safetyRandomPositionDistance", safetyDistance);
		targetRadius = args.getArgumentAsDoubleOrSetDefault("targetRadius", targetRadius);
		holdDistanceToTarget = args.getArgumentAsDoubleOrSetDefault("holdDistanceToTarget", holdDistanceToTarget);
		moveTargets = args.getArgumentAsIntOrSetDefault("moveTarget", 0) == 1;

		movementVelocity = args.getArgumentAsDoubleOrSetDefault("targetMovementVelocity", movementVelocity);
		variateTargetVelocity = args.getArgumentAsIntOrSetDefault("variateTargetsSpeed", 0) == 1;

		if (args.getArgumentIsDefined("targetMovementAzimuth")) {
			movementAzimuth = args.getArgumentAsDouble("targetMovementAzimuth") * FastMath.PI / 180;
			movementAzimuth %= 360;
		}

		variateTargetAzimuth = args.getArgumentAsIntOrSetDefault("variateTargetsAzimuth", 1) == 1;
	}

	protected boolean safeForRobot(Robot r, Simulator simulator) {
		if (r.isInvolvedInCollison()) {
			return false;
		}

		if (safetyDistance > 0) {
			double min = Double.MAX_VALUE;

			for (Robot robot : robots) {
				if (robot.getId() == r.getId()) {
					// all robots with a lower ID are at safe distances, so we
					// can exit
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

	protected boolean safeForTarget(Target target, Simulator simulator) {
		Vector2d position = CoordinateUtilities.GPSToCartesian(target.getLatLon());

		for (Target t : targets) {
			if (t == null || target.equals(t)) {
				continue;
			}

			Vector2d pos = CoordinateUtilities.GPSToCartesian(t.getLatLon());
			if ((position.distanceTo(pos) - targetRadius * 2) - safetyDistance <= 0) {
				return false;
			}
		}

		return true;
	}

	protected void positionDroneInRandomPos(AquaticDrone drone, Simulator simulator) {
		do {
			double radius = radiusOfObjPositioning * simulator.getRandom().nextDouble();
			double orientation = (simulator.getRandom().nextDouble() * FastMath.PI * 2) % 360;

			double x = radius * FastMath.cos(orientation);
			double y = radius * FastMath.sin(orientation);
			drone.setPosition(x, y);
			drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
			updateCollisions(simulator.getTime());
		} while (!safeForRobot(drone, simulator));
	}

	protected void positionTargetInRandomPos(Target target, Simulator simulator) {
		do {
			double radius = radiusOfObjPositioning * simulator.getRandom().nextDouble();
			double orientation = (simulator.getRandom().nextDouble() * FastMath.PI * 2) % 360;

			double x = radius * FastMath.cos(orientation);
			double y = radius * FastMath.sin(orientation);
			target.setLatLon(CoordinateUtilities.cartesianToGPS(x, y));
			updateCollisions(simulator.getTime());

		} while (!safeForTarget(target, simulator));
	}

	public void addMarker(LightPole lp) {
		markers.put(lp.getName(), lp);
		addObject(lp);
	}

	public void removeMarker(String name) {
		removeObject(markers.remove(name));
	}

	@Override
	public void update(double time) {
		// for (Target target : targets) {
		// if (moveTargets) {
		// target.step(time);
		// }
		// }
	}

	public double getRadiusOfObjPositioning() {
		return radiusOfObjPositioning;
	}
}
