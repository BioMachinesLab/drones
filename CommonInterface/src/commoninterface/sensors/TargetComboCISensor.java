package commoninterface.sensors;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import net.jafama.FastMath;

public class TargetComboCISensor extends CISensor {
	private static final long serialVersionUID = -8416969087882860806L;
	private static double SPEED_NORMALIZATION_CEELING = 2.0;
	protected double[] readings = { 0, 0, 0 };
	private boolean excludeOccupied = false;
	private double range = 100;
	private Target consideringTarget = null;
	private double distanceToCommute = 2.5;

	public TargetComboCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);

		excludeOccupied = args.getArgumentAsIntOrSetDefault("excludeOccupied", 0) == 1;
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
		distanceToCommute = args.getArgumentAsDoubleOrSetDefault("distanceToCommute", distanceToCommute);
	}

	@Override
	public void update(double time, Object[] entities) {
		LatLon robotLatLon = ((AquaticDroneCI) robot).getGPSLatLon();
		ArrayList<Target> targets = getTargetsOccupancy(entities);
		Target target = getClosestTarget(excludeOccupied, targets);

		LatLon latLon = null;
		double distance = -1;
		if (target != null) {
			consideringTarget = target;
			latLon = target.getLatLon();
			distance = CoordinateUtilities.distanceInMeters(robotLatLon, latLon);

			if (distance > -1 && latLon != null && distance <= range) {
				double robotOrientation = ((AquaticDroneCI) robot).getCompassOrientationInDegrees();

				/*
				 * Target velocity
				 */
				Vector2d velocityVector = target.getMotionData().getVelocityVector(time);
				double targetVelocity = velocityVector.length();
				double robotVelocity = ((AquaticDroneCI) robot).getRobotSpeedMs();
				double velocityDifference = robotVelocity - targetVelocity;
				velocityDifference /= SPEED_NORMALIZATION_CEELING * 2;

				double orientationDifference = 0;
				if (distance > distanceToCommute) {
					/*
					 * Relative orientation based on target's geographic
					 * coordinates (Used when further than 2.5 from target)
					 */
					double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotLatLon, latLon);
					orientationDifference = robotOrientation - coordinatesAngle;
					orientationDifference %= 360;
					if (orientationDifference > 180) {
						orientationDifference = -((180 - orientationDifference) + 180);
					}
				} else {
					/*
					 * Relative orientation based on target's velocity vector
					 */

					double orientation = velocityVector.getAngle();
					// Original value comes in [0,180] with alpha on [0,180]
					// degrees and [-180,0] with alpha [180,360]. Convert it to
					// [0,360] on [0,360]
					if (orientation < 0) {
						orientation = (FastMath.PI * 2) + orientation;
					}

					// Rotate the referential 1 quadrant
					orientation += 3 * FastMath.PI / 2;
					orientation %= FastMath.PI * 2;

					// Invert the scale
					orientation = (FastMath.PI * 2) - orientation;
					orientation = Math.toDegrees(orientation);

					orientationDifference = robotOrientation - orientation;

					if (robotOrientation < 0)
						robotOrientation += 360;

					if (orientationDifference < 0)
						orientationDifference += 360;

					robotOrientation %= 360;
					orientationDifference %= 360;

					if (orientationDifference > 180) {
						orientationDifference = -((180 - orientationDifference) + 180);
					}
				}

				// [0] - Relative orientation (dependent from the distance to
				// target
				// [1] - Distance to target
				// [2] - Target velocity
				readings[0] = (orientationDifference / 360.0) + 0.5;
				readings[1] = distance / range;
				readings[2] = velocityDifference + 0.5;
			} else {
				readings[0] = 0.5;
				readings[1] = 1;
				readings[2] = 0.5;
			}
		} else {
			readings[0] = 0.5;
			readings[1] = 1;
			readings[2] = 0.5;
		}
	}

	public Target getConsideringTarget() {
		return consideringTarget;
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

		return closest;
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
