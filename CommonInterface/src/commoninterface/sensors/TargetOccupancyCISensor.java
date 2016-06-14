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
import net.jafama.FastMath;

public class TargetOccupancyCISensor extends CISensor {
	private static final long serialVersionUID = 6221903809782922861L;
	protected double[] readings = { 0 };

	public TargetOccupancyCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public void update(double time, Object[] entities) {
		ArrayList<Target> targets = getTargetsOccupancy(entities);
		Target target = getClosestTarget(targets);
		;

		if (target.isOccupied()) {
			readings[0] = 1;
		} else {
			readings[0] = 0;
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

	private Target getClosestTarget(ArrayList<Target> targets) {
		Target target = null;

		Vector2d location = null;
		if (robot instanceof AquaticDroneCI) {
			location = CoordinateUtilities.GPSToCartesian(((AquaticDroneCI) robot).getGPSLatLon());
		} else {
			throw new NullPointerException("Incompatible robot instance!");
		}

		double minDistance = Double.MAX_VALUE;
		for (Target ent : targets) {
			Vector2d pos = CoordinateUtilities.GPSToCartesian(ent.getLatLon());
			if (location.distanceTo(pos) < minDistance) {
				minDistance = location.distanceTo(pos);
				target = ent;
			}
		}

		return target;
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

		for (Target t : targets) {
			RobotLocation robot = getClosestRobotToTarget(t, rls);
			if (isInsideTarget(robot, t)) {
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
		for (RobotLocation robot : rls) {
			Vector2d robotPosition = CoordinateUtilities.GPSToCartesian(robot.getLatLon());
			double distance = FastMath.abs(targetPosition.distanceTo(robotPosition));

			if (distance < minDistance) {
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
