package commoninterface.entities.target.motion;

import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import net.jafama.FastMath;

public class RotationMotionData extends MotionData {
	private static final long serialVersionUID = -6168333864512168808L;
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
		this.rotationCenterCartesian = CoordinateUtilities.GPSToCartesian(rotationCenter);
		this.angularVelocity = angularVelocity;
		this.rotationDirection = rotationDirection;

		initialRelativePosition = originalPositionCartesian;
		initialRelativePosition.sub(rotationCenterCartesian);
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
		currentAngle %= (2 * FastMath.PI);

		double distanceToCenter = initialRelativePosition.length();
		double initialAngle = initialRelativePosition.getAngle();

		Vector2d finalVector = new Vector2d(0, 0);
		finalVector.x = distanceToCenter * FastMath.cos(currentAngle + initialAngle);
		finalVector.y = distanceToCenter * FastMath.sin(currentAngle + initialAngle);

		finalVector.sub(initialRelativePosition);
		return finalVector;
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

	public boolean isRotating() {
		return rotate;
	}

	public LatLon getRotationCenter() {
		return rotationCenter;
	}

	@Override
	public MotionData clone() {
		RotationMotionData rmd = new RotationMotionData(entity.clone(), rotationCenter, angularVelocity,
				rotationDirection);
		rmd.setRotate(rotate);
		return rmd;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RotationMotionData) {
			RotationMotionData rmd = (RotationMotionData) obj;
			return motionType.equals(rmd.getMotionType()) && entity.equals(rmd.getEntity())
					&& rotate == rmd.isRotating() && rotationDirection == rmd.getRotationDirection()
					&& angularVelocity == rmd.getAngularVelocity()
					&& rotationCenter.equals(rmd.getRotationCenter())
					&& originalPosition.equals(rmd.getOriginalPosition());
		} else {
			return false;
		}
	}
}
