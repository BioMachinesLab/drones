package commoninterface.entities.target.motion;

import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public abstract class MotionData {
	public static enum MovementType {
		LINEAR, ROTATIONAL, MIXED;
	}

	protected static final int UPDATE_RATE = 10; // Assuming 100ms simulation
													// steps

	protected LatLon originalPosition;
	protected GeoEntity entity;
	protected Vector2d originalPositionCartesian;
	protected MovementType motionType;
	protected Vector2d velocityVector;

	public MotionData(GeoEntity entity, MovementType motionType) {
		this.entity = entity;
		this.originalPosition = entity.getLatLon();
		originalPositionCartesian = CoordinateUtilities.GPSToCartesian(entity.getLatLon());
		this.motionType = motionType;
		velocityVector = new Vector2d(0, 0);
	}

	public MovementType getMotionType() {
		return motionType;
	}

	public Vector2d getVelocityVector(double step) {
		return velocityVector;
	}

	public LatLon getOriginalPosition() {
		return originalPosition;
	}

	public abstract LatLon calculatePosition(double step);

	public abstract Vector2d calculateTranslationVector(double step);
}
