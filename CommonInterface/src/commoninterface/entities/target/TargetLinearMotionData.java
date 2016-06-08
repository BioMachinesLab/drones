package commoninterface.entities.target;

import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import net.jafama.FastMath;

public class TargetLinearMotionData extends MotionData {

	public TargetLinearMotionData(Target target, double targetMovementVelocity, double targetMovementAzimuth) {
		super(new Target[] { target }, targetMovementVelocity, targetMovementAzimuth);
	}

	public Target getTarget() {
		return targets[0];
	}

	public Vector2d getPosition() {
		return targetsPositions[0];
	}

	@Override
	public void move() {
		double delta_x = FastMath.cos(movementAzimuth) * movementDelta + targetsPositions[0].x;
		double delta_y = FastMath.sin(movementAzimuth) * movementDelta + targetsPositions[0].y;

		targetsPositions[0] = new Vector2d(delta_x, delta_y);
		targets[0].setLatLon(CoordinateUtilities.cartesianToGPS(targetsPositions[0]));
	}
}