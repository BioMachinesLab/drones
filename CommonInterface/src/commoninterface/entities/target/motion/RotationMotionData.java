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
	private Vector2d rotationCenter;
	private Vector2d initialRelativePosition;

	public RotationMotionData(GeoEntity entity, Vector2d rotationCenter, double angularVelocity,
			boolean rotationDirection) {
		super(entity, MovementType.ROTATIONAL);

		this.rotationCenter = rotationCenter;
		this.angularVelocity = angularVelocity;
		this.rotationDirection = rotationDirection;

		double x_rel_position_to_center = originalPositionCartesian.x - rotationCenter.x;
		double y_rel_position_to_center = originalPositionCartesian.y - rotationCenter.y;
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
		double currentAngle = (angularVelocity / UPDATE_RATE) * step;
		if (rotationDirection) {
			currentAngle = -currentAngle;
		}

		// Rotation given by a 2D rotation matrix:
		// x= x.cos(tetha) - y.sin(tetha)
		// Y= x.sin(tetha) + y.cos(tetha)
		double cos = FastMath.cos(currentAngle);
		double sin = FastMath.sin(currentAngle);
		Vector2d finalPosition = new Vector2d(0, 0);
		finalPosition.x = initialRelativePosition.x * cos - initialRelativePosition.y * sin;
		finalPosition.y = initialRelativePosition.x * sin + initialRelativePosition.y * cos;

		// Translation given by a 2D translation matrix:
		// x= x + dx
		// y= y + dy
		finalPosition.x += rotationCenter.x;
		finalPosition.y += rotationCenter.y;
		
		finalPosition.sub(initialRelativePosition);
		return finalPosition;
	}

	@Override
	public Vector2d getVelocityVector(double step) {
		if (!rotate) {
			return new Vector2d(0, 0);
		} else {
			double currentAngle = (angularVelocity / UPDATE_RATE) * step;
			if (rotationDirection) {
				currentAngle = -currentAngle;
			}
			currentAngle += initialRelativePosition.getAngle();

			// Given by the angle of vector (0,0)->(targetx,targety) +- Pi/2
			// (depending on the movement direction)
			double angular_momentum_angle = (FastMath.PI / 2) * (rotationDirection ? -1 : 1) + currentAngle;

			// Given by the formula v=r*w (v=linear velocity, r=radius,
			// w=angular
			// velocity), we obtain the linear velocity from the radius and the
			// angular velocity
			double angular_momentum_intensity = initialRelativePosition.length() * (angularVelocity / UPDATE_RATE);

			double x_coordinate = angular_momentum_intensity * FastMath.cos(angular_momentum_angle);
			double y_coordinate = angular_momentum_intensity * FastMath.sin(angular_momentum_angle);
			Vector2d angularMomentum = new Vector2d(x_coordinate, y_coordinate);

			return angularMomentum;
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

	public Vector2d getRotationCenter() {
		return rotationCenter;
	}
}
