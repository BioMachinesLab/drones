package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public class TargetOccupancyCISensor extends CISensor {
	private static final long serialVersionUID = 6221903809782922861L;
	protected double[] readings = { 0 };

	public TargetOccupancyCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public void update(double time, Object[] entities) {
		Target target = getClosestTarget();

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

	private Target getClosestTarget() {
		Target target = null;

		Vector2d location = null;
		if (robot instanceof AquaticDroneCI) {
			location = CoordinateUtilities.GPSToCartesian(((AquaticDroneCI) robot).getGPSLatLon());
		} else {
			throw new NullPointerException("Incompatible robot instance!");
		}

		double minDistance = Double.MAX_VALUE;
		for (Entity ent : ((AquaticDroneCI) robot).getEntities()) {
			if (ent instanceof Target) {
				Vector2d pos = CoordinateUtilities.GPSToCartesian(((Target) ent).getLatLon());
				if (location.distanceTo(pos) < minDistance) {
					minDistance = location.distanceTo(pos);
					target = (Target) ent;
				}

			}
		}

		return target;
	}
}
