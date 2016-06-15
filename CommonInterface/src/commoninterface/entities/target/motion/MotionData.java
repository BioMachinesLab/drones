package commoninterface.entities.target.motion;

import java.io.Serializable;

import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public abstract class MotionData implements Serializable {
	private static final long serialVersionUID = -7143870830419053233L;

	public static enum MovementType {
		LINEAR, ROTATIONAL, MIXED;
	}

	public static final int UPDATE_RATE = 10; // Assuming 100ms simulation
												// steps

	protected LatLon originalPosition;
	protected GeoEntity entity;
	protected Vector2d originalPositionCartesian;
	protected MovementType motionType;

	public MotionData(GeoEntity entity, MovementType motionType) {
		this(entity, entity.getLatLon(), motionType);
	}

	public MotionData(GeoEntity entity, LatLon position, MovementType motionType) {
		this.entity = entity;
		this.originalPosition = new LatLon(position);
		originalPositionCartesian = CoordinateUtilities.GPSToCartesian(position);
		this.motionType = motionType;
	}

	public MovementType getMotionType() {
		return motionType;
	}

	public abstract Vector2d getVelocityVector(double step);

	public LatLon getOriginalPosition() {
		return originalPosition;
	}

	public abstract LatLon calculatePosition(double step);

	public abstract Vector2d calculateTranslationVector(double step);

	@Override
	public abstract MotionData clone();
}
