package commoninterface.entities.target;

import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;

public abstract class MotionData {
	protected Target[] targets;
	protected Vector2d[] targetsPositions;
	protected double movementVelocity;
	protected double movementDelta;
	protected double movementAzimuth;

	public MotionData(Target[] targets, double targetMovementVelocity, double targetMovementAzimuth) {
		this.targets = targets;

		this.movementVelocity = targetMovementVelocity;
		movementDelta = targetMovementVelocity / 10; // Assuming 100ms
														// simulation steps
		this.movementAzimuth = targetMovementAzimuth;

		targetsPositions = new Vector2d[targets.length];
		for (int i = 0; i < targets.length; i++) {
			targetsPositions[i] = CoordinateUtilities.GPSToCartesian(targets[i].getLatLon());
			targets[i].setMotionData(this);
		}
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

	public abstract void move();
}
