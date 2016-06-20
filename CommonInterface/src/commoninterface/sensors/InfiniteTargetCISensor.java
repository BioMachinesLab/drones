package commoninterface.sensors;

import java.util.ArrayList;
import java.util.HashMap;

import commoninterface.AquaticDroneCI;
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

public class InfiniteTargetCISensor extends WaypointCISensor {
	private static final long serialVersionUID = 490158787475877489L;
	private double expFactor = 30;
	// private double linearFactor = -0.009;
	private boolean excludeOccupied = false;
	private boolean linear = false;
	private boolean stabilize = false;
	private boolean normalize = false;

	private double range = 100;
	private int historySize = 10;
	private Target[] lastSeenTargets;
	private int pointer = 0;

	public InfiniteTargetCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);

		expFactor = args.getArgumentAsDoubleOrSetDefault("expFactor", expFactor);
		excludeOccupied = args.getArgumentAsIntOrSetDefault("excludeOccupied", 0) == 1;
		linear = args.getArgumentAsIntOrSetDefault("linear", 0) == 1;
		stabilize = args.getArgumentAsIntOrSetDefault("stabilize", 0) == 1;
		historySize = args.getArgumentAsIntOrSetDefault("historySize", historySize);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
		normalize = args.getArgumentAsIntOrSetDefault("normalize", 0) == 1;

		lastSeenTargets = new Target[historySize];
		for (int i = 0; i < lastSeenTargets.length; i++) {
			lastSeenTargets[i] = null;
		}
	}

	/**
	 * Sets the sensor reading in the readings array. The first element of the
	 * array corresponds to the difference between the robot orientation and the
	 * azimut to target. The second element corresponds to the distance from
	 * robot to the target
	 * 
	 * Return: readings[0] = difference between robot orientation an the azimuth
	 * to target, readings[1] = distance to target (in meters)
	 */
	@Override
	public void update(double time, Object[] entities) {

		LatLon robotLatLon = ((AquaticDroneCI) robot).getGPSLatLon();
		ArrayList<Target> targets = getTargetsOccupancy(entities);
		Target target = getClosestTarget(excludeOccupied, targets);

		LatLon latLon = null;
		double distance = -1;
		if (target != null) {
			latLon = target.getLatLon();
			distance = CoordinateUtilities.distanceInMeters(robotLatLon, latLon);
		}

		if (target != null && distance > -1 && latLon != null && distance <= range) {
			double currentOrientation = ((AquaticDroneCI) robot).getCompassOrientationInDegrees();
			double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotLatLon, latLon);
			double orientationDifference = currentOrientation - coordinatesAngle + 180;

			if (normalize) {
				orientationDifference %= 360;
				orientationDifference /= 360;
			} else {
				orientationDifference %= 360;
				if (orientationDifference > 180) {
					orientationDifference = -((180 - orientationDifference) + 180);
				}
			}

			readings[0] = orientationDifference;

			if (linear) {
				if (normalize) {
					readings[1] = distance / range;
				} else {
					readings[1] = distance;
				}
			} else {
				if (normalize) {
					readings[1] = (FastMath.exp(-distance / expFactor)) / range;
				} else {
					readings[1] = (FastMath.exp(-distance / expFactor));
				}
			}
		} else {
			readings[0] = 0;

			if (normalize) {
				readings[1] = 1;
			} else {
				readings[1] = range;
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
			if (ent.isOccupied() && ent.getOccupantID().equals(robot.getNetworkAddress())
			// && robotPosition.distanceTo(pos) <= ent.getRadius()
			// && robotPosition.distanceTo(pos) < minDistance
			) {
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
