package commoninterface.entities.formation.motion;

import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class LinearMotionData extends MotionData {
	private static final long serialVersionUID = -5074482521400228147L;

	private boolean move = true;

	private double movementVelocity;
	private double movementAzimuth;

	public LinearMotionData(GeoEntity entity, double movementVelocity, double movementAzimuth) {
		super(entity, MovementType.LINEAR);

		this.movementAzimuth = movementAzimuth;
		this.movementVelocity = movementVelocity;
	}

	@Override
	public LatLon calculatePosition(double step) {
		Vector2d currentPosition = new Vector2d(originalPositionCartesian);
		currentPosition.add(calculateTranslationVector(step));
		return CoordinateUtilities.cartesianToGPS(currentPosition);
	}

	@Override
	public Vector2d calculateTranslationVector(double step) {
		double walkedDistance = (movementVelocity / UPDATE_RATE) * (int) step;
		double azimuth = movementAzimuth;

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
			Vector2d velocityVector = new Vector2d(0, 0);
			velocityVector.setPolarCoordinates(movementVelocity / UPDATE_RATE, movementAzimuth);
			return velocityVector;
		} else {
			return new Vector2d(0, 0);
		}
	}

	public void setMove(boolean move) {
		this.move = move;
	}

	public boolean isMoving() {
		return move;
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

	@Override
	public LinearMotionData clone() {
		LinearMotionData lmd = new LinearMotionData(entity.clone(), movementVelocity, movementAzimuth);
		lmd.setMove(move);
		return lmd;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LinearMotionData) {
			LinearMotionData lmd = (LinearMotionData) obj;

			boolean a = motionType.equals(lmd.getMotionType());
			boolean b = move == lmd.isMoving();
			boolean c = movementAzimuth == lmd.getMovementAzimuth();
			boolean d = movementVelocity == lmd.getMovementVelocity();
			boolean e = originalPosition.equals(lmd.getOriginalPosition());
			boolean f = entity.equals(lmd.getEntity());

//			System.out.println("a=" + a);
//			System.out.println("b=" + b);
//			System.out.println("c=" + c);
//			System.out.println("d=" + d);
//			System.out.println("e=" + e);
//			System.out.println("f=" + f);
			
			return a && b && c && d && e && f;
		} else {
			return false;
		}
	}
}