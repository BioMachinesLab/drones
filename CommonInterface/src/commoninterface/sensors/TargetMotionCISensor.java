package commoninterface.sensors;

import java.util.ArrayList;
import java.util.HashMap;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.formation.Formation;
import commoninterface.entities.formation.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import net.jafama.FastMath;

public class TargetMotionCISensor extends CISensor {
	private static final long serialVersionUID = 6221903809782922861L;
	private static double SPEED_NORMALIZATION_CEELING = 2.0;
	protected double[] readings = { 0, 0 };
	private boolean excludeOccupied = false;
	private boolean stabilize = false;

	private double range = 100;
	private int historySize = 10;
	private Target[] lastSeenTargets;
	private int pointer = 0;
	private Target consideringTarget = null;

	public TargetMotionCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);

		excludeOccupied = args.getArgumentAsIntOrSetDefault("excludeOccupied", 0) == 1;
		stabilize = args.getArgumentAsIntOrSetDefault("stabilize", 0) == 1;
		historySize = args.getArgumentAsIntOrSetDefault("historySize", historySize);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);

		lastSeenTargets = new Target[historySize];
		for (int i = 0; i < lastSeenTargets.length; i++) {
			lastSeenTargets[i] = null;
		}
	}

	@Override
	public void update(double time, Object[] entities) {
		ArrayList<Target> targets = getTargetsOccupancy(entities);
		Target target = getClosestTarget(excludeOccupied, targets);

		if (target != null) {
			consideringTarget = target;

			Vector2d velocityVector = target.getMotionData().getVelocityVector(time);

			/*
			 * Deal with orientation
			 */
			double orientation = velocityVector.getAngle();

			// Original value comes in [0,180] with alpha on [0,180] degrees and
			// [-180,0] with alpha [180,360]. Convert it to [0,360] on [0,360]
			if (orientation < 0) {
				orientation = (FastMath.PI * 2) + orientation;
			}

			// Rotate the referential 1 quadrant
			orientation += 3 * FastMath.PI / 2;
			orientation %= FastMath.PI * 2;

			// Invert the scale
			orientation = (FastMath.PI * 2) - orientation;
			orientation = Math.toDegrees(orientation);

			double robotOrientation = ((AquaticDroneCI) robot).getCompassOrientationInDegrees();
			double orientationDifference = robotOrientation - orientation;

			if (robotOrientation < 0)
				robotOrientation += 360;

			if (orientationDifference < 0)
				orientationDifference += 360;

			robotOrientation %= 360;
			orientationDifference %= 360;

			if (orientationDifference > 180) {
				orientationDifference = -((180 - orientationDifference) + 180);
			}

			/*
			 * Deal with velocity
			 */
			double targetVelocity = velocityVector.length();
			double robotVelocity = ((AquaticDroneCI) robot).getRobotSpeedMs();
			double diff = robotVelocity - targetVelocity;
			diff /= SPEED_NORMALIZATION_CEELING * 2;

			readings[0] = orientationDifference / 360.0 + 0.5;
			readings[1] = diff + 0.5;

			// Is the sensed target inside the sensor range?
			double distance = ((AquaticDroneCI) robot).getGPSLatLon().distanceInMeters(target.getLatLon());
			if (distance > range) {
				readings[0] = 0.5;
				readings[1] = 0.5;
			}
		}
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}

	@Override
	public int getNumberOfSensors() {
		return readings.length;
	}

	private Target getClosestTarget(boolean excludeOccupied, ArrayList<Target> targets) {
		// Get robot location
		Vector2d robotPosition = null;
		if (robot instanceof AquaticDroneCI) {
			robotPosition = CoordinateUtilities.GPSToCartesian(((AquaticDroneCI) robot).getGPSLatLon());
		} else {
			throw new NullPointerException("Incompatible robot instance!");
		}

		// Get the closest target
		Target closest = null;
		double minDistance = Double.MAX_VALUE;
		for (Target ent : targets) {
			Vector2d pos = CoordinateUtilities.GPSToCartesian(ent.getLatLon());
			if (ent.isOccupied() && ent.getOccupantID().equals(robot.getNetworkAddress())) {
				minDistance = robotPosition.distanceTo(pos);
				closest = ent;
				break;
			} else {
				if (!ent.isOccupied() && robotPosition.distanceTo(pos) < minDistance) {
					minDistance = robotPosition.distanceTo(pos);
					closest = ent;
				}
			}
		}

		// Return either the most seen or the closest
		if (stabilize) {
			int pos = (pointer++) % historySize;
			lastSeenTargets[pos] = closest;

			if (pos < historySize) {
				return getMostCommonTarget(pos);
			} else {
				return getMostCommonTarget();
			}
		} else {
			return closest;
		}
	}

	public Target getConsideringTarget() {
		return consideringTarget;
	}

	private Target getMostCommonTarget(int endIndex) {
		// Create a map of target positions in a count table
		HashMap<Target, Integer> positions = new HashMap<Target, Integer>();
		ArrayList<Target> targets = new ArrayList<Target>();

		int counter = 0;
		targets.add(null);
		positions.put(null, counter++);
		for (Object ent : ((AquaticDroneCI) robot).getEntities()) {
			if (ent instanceof Target) {
				positions.put((Target) ent, counter++);
				targets.add((Target) ent);
			}
		}

		// Table to count the occurrences
		int[] count = new int[positions.size()];

		// Count each target occurrence
		for (int i = 0; i < endIndex; i++) {
			int positionInArray = positions.get(lastSeenTargets[i]);
			count[positionInArray]++;
		}

		// Get the most common one
		int maxValue = Integer.MIN_VALUE;
		Target target = null;
		for (int i = 0; i < count.length; i++) {
			if (count[i] > maxValue) {
				target = targets.get(i);
			}
		}

		return target;
	}

	private Target getMostCommonTarget() {
		return getMostCommonTarget(historySize);
	}

	private ArrayList<Target> getTargetsOccupancy(Object[] entities) {
		ArrayList<RobotLocation> rls = new ArrayList<RobotLocation>();
		ArrayList<Target> targets = new ArrayList<Target>();

		for (Object ent : entities) {
			if (ent instanceof RobotLocation) {
				rls.add((RobotLocation) ent);
			}
		}

		for (Entity ent : ((AquaticDroneCI) robot).getEntities()) {
			if (ent instanceof Formation) {
				targets.addAll(((Formation) ent).getTargets());
				break;
			}
		}

		RobotLocation myLocation = new RobotLocation(((AquaticDroneCI) robot).getNetworkAddress(),
				((AquaticDroneCI) robot).getGPSLatLon(), ((AquaticDroneCI) robot).getCompassOrientationInDegrees(),
				((AquaticDroneCI) robot).getDroneType());
		rls.add(myLocation);

		for (Target t : targets) {
			RobotLocation robot = getClosestRobotToTarget(t, rls);
			if (!t.isOccupied() && isInsideTarget(robot, t)) {
				t.setOccupantID(robot.getName());
				t.setOccupied(true);
			}
		}

		return targets;
	}

	private RobotLocation getClosestRobotToTarget(Target target, ArrayList<RobotLocation> rls) {
		double minDistance = Double.MAX_VALUE;
		RobotLocation closestRobot = null;

		Vector2d targetPosition = CoordinateUtilities.GPSToCartesian(target.getLatLon());
		String occupiedID = null;
		for (RobotLocation robot : rls) {
			Vector2d robotPosition = CoordinateUtilities.GPSToCartesian(robot.getLatLon());
			double distance = FastMath.abs(targetPosition.distanceTo(robotPosition));

			if (distance < target.getRadius() && (occupiedID == null || robot.getName().compareTo(occupiedID) > 0)) {
				closestRobot = robot;
				minDistance = distance;
				occupiedID = robot.getName();
			} else if (distance < minDistance) {
				closestRobot = robot;
				minDistance = distance;
			}
		}

		return closestRobot;
	}

	private boolean isInsideTarget(RobotLocation robot, Target target) {
		if (robot == null) {
			return false;
		}

		Vector2d pos = CoordinateUtilities.GPSToCartesian(target.getLatLon());
		return pos.distanceTo(CoordinateUtilities.GPSToCartesian(robot.getLatLon())) <= target.getRadius();
	}
}
