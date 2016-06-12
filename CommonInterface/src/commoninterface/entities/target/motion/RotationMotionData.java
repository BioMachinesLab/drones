package commoninterface.entities.target.motion;

import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import net.jafama.FastMath;

public class RotationMotionData extends MotionData {
	private boolean rotate = true;
	private boolean rotationDirection = false; // false for counter-clockwise
	// and true for clockwise

	private double angularVelocity; // In rad/sec
	private LatLon rotationCenter;
	private Vector2d rotationCenterCartesian;
	private Vector2d initialRelativePosition;

	public RotationMotionData(GeoEntity entity, LatLon rotationCenter, double angularVelocity,
			boolean rotationDirection) {
		super(entity, MovementType.ROTATIONAL);

		this.rotationCenter = rotationCenter;
		rotationCenterCartesian = CoordinateUtilities.GPSToCartesian(rotationCenter);
		this.angularVelocity = angularVelocity;
		this.rotationDirection = rotationDirection;

		double x_rel_position_to_center = originalPositionCartesian.x - rotationCenterCartesian.x;
		double y_rel_position_to_center = originalPositionCartesian.y - rotationCenterCartesian.y;
		initialRelativePosition = new Vector2d(x_rel_position_to_center, y_rel_position_to_center);
	}

	@Override
	public LatLon calculatePosition(double step) {
		Vector2d currentPosition = new Vector2d(originalPositionCartesian);
		currentPosition.add(calculateTranslationVector(step));
		return CoordinateUtilities.cartesianToGPS(currentPosition);
	}

	@Override
	public Vector2d calculateTranslationVector(double step) {
		Vector2d translationVector = new Vector2d(0, 0);
		for (int i = 0; i < step; i++) {
			translationVector.add(getVelocityVector(i));
		}

		return translationVector;
	}

	@Override
	public Vector2d getVelocityVector(double step) {
		if (!rotate) {
			return new Vector2d(0, 0);
		} else {
			double initialRotationAngle = initialRelativePosition.getAngle();
			double currentAngle = initialRotationAngle * angularVelocity * step;

			if (rotationDirection) {
				currentAngle = -currentAngle;
			}

			// Given by the formula v=r*w (v=linear velocity, r=radius,
			// w=angular velocity), we obtain the linear velocity from the
			// radius and the angular velocity
			double angular_momentum_intensity = initialRelativePosition.length() * angularVelocity;

			double x_coordinate = angular_momentum_intensity * FastMath.cos(currentAngle);
			double y_coordinate = angular_momentum_intensity * FastMath.sin(currentAngle);
			return new Vector2d(x_coordinate, y_coordinate);
		}
	}

	/*
	 * Getters and setters
	 */
	public double getAngularVelocity() {
		return angularVelocity;
	}

	// False for counter-clockwise and true for clockwise
	public boolean getRotationDirection() {
		return rotationDirection;
	}

	public void setAngularVelocity(double angularVelocity) {
		this.angularVelocity = angularVelocity;
	}

	public void setRotationDirection(boolean rotationDirection) {
		this.rotationDirection = rotationDirection;
	}

	public void setRotate(boolean rotate) {
		this.rotate = rotate;
	}

	public LatLon getRotationCenter() {
		return rotationCenter;
	}
}
