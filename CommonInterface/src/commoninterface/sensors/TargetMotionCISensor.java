package commoninterface.sensors;

import java.util.ArrayList;
import java.util.HashMap;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.target.FormationMotionData;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import net.jafama.FastMath;

public class TargetMotionCISensor extends CISensor {
	private static final long serialVersionUID = 6221903809782922861L;
	protected double[] readings = { 0, 0 };
	private boolean excludeOccupied = false;
	private boolean stabilize = false;
	private boolean normalize = false;

	// XXX
	private boolean alternative = false;
	private boolean vascos = false;

	private double range = 100;
	private int historySize = 10;
	private Target[] lastSeenTargets;
	private int pointer = 0;

	public TargetMotionCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);

		excludeOccupied = args.getArgumentAsIntOrSetDefault("excludeOccupied", 0) == 1;
		stabilize = args.getArgumentAsIntOrSetDefault("stabilize", 0) == 1;
		historySize = args.getArgumentAsIntOrSetDefault("historySize", historySize);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
		normalize = args.getArgumentAsIntOrSetDefault("normalize", 0) == 1;

		// XXX
		alternative = args.getArgumentAsIntOrSetDefault("normalize", 0) == 1;
		vascos = args.getArgumentAsIntOrSetDefault("vascos", 0) == 1;

		lastSeenTargets = new Target[historySize];
		for (int i = 0; i < lastSeenTargets.length; i++) {
			lastSeenTargets[i] = null;
		}
	}

	@Override
	public void update(double time, Object[] entities) {
		Target target = getClosestTarget(excludeOccupied, entities);
		if (vascos) {
			if (target != null) {
				Vector2d vel = ((FormationMotionData) target.getTargetMotionData()).getVelocityVector(target);
				readings[0] = vel.getAngle() / Math.PI / 2;
				readings[1] = vel.length();

				// Move the readings to the positive side of the scale
				if (readings[0] < 0) {
					readings[0] += FastMath.PI * 2;
				}
				readings[0] %= FastMath.PI * 2;

				// Normalization and detection range
				if (robot instanceof AquaticDroneCI) {
					// Is the sensed target inside the sensor range?
					double distance = ((AquaticDroneCI) robot).getGPSLatLon().distanceInMeters(target.getLatLon());
					if (target.getTargetMotionData() instanceof FormationMotionData && distance < range) {
						if (normalize) {
							readings[0] /= (FastMath.PI * 2);
						}
						// In case the target is outside the range
					} else {
						readings[0] = 0;
						readings[1] = 0;
					}
				} else {
					readings[0] = 0;
					readings[1] = 0;
				}
			}
		} else {
			// Sancho's version
			if (target != null) {
				Vector2d vel = ((FormationMotionData) target.getTargetMotionData()).getVelocityVector(target);
				readings[0] = vel.length();
				readings[1] = (vel.getAngle() + Math.PI) / Math.PI / 2;
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

	private Target getClosestTarget(boolean excludeOccupied, Object[] entities) {
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
		for (Object ent : ((AquaticDroneCI) robot).getEntities()) {
			if (ent instanceof Target) {
				Vector2d pos = CoordinateUtilities.GPSToCartesian(((Target) ent).getLatLon());

				// XXX
				if (alternative) {
					if (((Target) ent).isOccupied() && robotPosition.distanceTo(pos) <= ((Target) ent).getRadius()
							&& robotPosition.distanceTo(pos) < minDistance) {
						minDistance = robotPosition.distanceTo(pos);
						closest = (Target) ent;
					} else {
						if (!((Target) ent).isOccupied() && robotPosition.distanceTo(pos) < minDistance) {
							minDistance = robotPosition.distanceTo(pos);
							closest = (Target) ent;
						}
					}
				} else {
					if (!excludeOccupied || (excludeOccupied && !((Target) ent).isOccupied())) {
						if (robotPosition.distanceTo(pos) < minDistance) {
							minDistance = robotPosition.distanceTo(pos);
							closest = (Target) ent;
						}
					}
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
}
