package commoninterface.entities.formation.motion;

import java.util.ArrayList;

import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class MixedMotionData extends MotionData {
	private static final long serialVersionUID = -9079816490525272672L;
	private ArrayList<MotionData> motionData = new ArrayList<MotionData>();

	public MixedMotionData(GeoEntity entity) {
		super(entity, MovementType.MIXED);
	}

	public MixedMotionData(GeoEntity entity, LatLon position) {
		super(entity, position, MovementType.MIXED);
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

		for (MotionData m : motionData) {

			Vector2d t = m.calculateTranslationVector(step);
			translationVector.add(t);
		}

		return translationVector;
	}

	public void addMotionData(MotionData m) {
		motionData.add(m);
	}

	public ArrayList<MotionData> getMotionDataComponents() {
		return motionData;
	}

	@Override
	public Vector2d getVelocityVector(double step) {
		Vector2d velocityVector = new Vector2d(0, 0);
		for (MotionData m : motionData) {

			Vector2d t = m.getVelocityVector(step);
			velocityVector.add(t);
		}

		return velocityVector;
	}

	@Override
	public MixedMotionData clone() {
		MixedMotionData m = new MixedMotionData(entity.clone(), new LatLon(originalPosition));

		for (MotionData motion : motionData) {
			m.addMotionData(motion.clone());
		}
		return m;
	}

	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MixedMotionData && motionType == ((MixedMotionData) obj).getMotionType()
				&& originalPosition.equals(((MixedMotionData) obj).getOriginalPosition())) {

			for (MotionData motion : ((MixedMotionData) obj).getMotionDataComponents()) {
				if (!motionData.contains(motion)) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}
}
