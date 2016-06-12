package commoninterface.entities.target.motion;

import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class LinearMotionData extends MotionData {
	private boolean move = true;

	private double movementVelocity;
	private double movementAzimuth;

	public LinearMotionData(GeoEntity entity, LatLon originalPosition, double movementVelocity,
			double movementAzimuth) {
		super(entity, MovementType.LINEAR);

		this.movementAzimuth = movementAzimuth;
		this.movementVelocity = movementVelocity;

		velocityVector.setPolarCoordinates(movementVelocity, movementAzimuth);
	}

	@Override
	public LatLon calculatePosition(double step) {
		Vector2d currentPosition = new Vector2d(originalPositionCartesian);
		currentPosition.add(calculateTranslationVector(step));
		return CoordinateUtilities.cartesianToGPS(currentPosition);
	}

	@Override
	public Vector2d calculateTranslationVector(double step) {
		double walkedDistance = 0;
		double azimuth = getVelocityVector(0).getAngle();

		for (int i = 0; i < step; i++) {
			walkedDistance = (getVelocityVector(step).length()) / UPDATE_RATE;
		}

		Vector2d walkedVector = new Vector2d(0, 0);
		walkedVector.setPolarCoordinates(walkedDistance, azimuth);
		return walkedVector;
	}

	/*
	 * Getters and setters
	 */
	@Override
	public Vector2d getVelocityVector(double step) {
		if (move) {
			return super.getVelocityVector(step);
		} else {
			return new Vector2d(0, 0);
		}
	}

	public GeoEntity getEntity() {
		return entity;
	}

	public void setMove(boolean move) {
		this.move = move;
	}

	public double getMovementVelocity() {
		return movementVelocity;
	}

	public double getMovementAzimuth() {
		return movementAzimuth;
	}

	public void setMovementAzimuth(double targetMovementAzimuth) {
		this.movementAzimuth = targetMovementAzimuth;
	}

	public void setMovementVelocity(double targetMovementVelocity) {
		this.movementVelocity = targetMovementVelocity;
	}
}