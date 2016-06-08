package commoninterface.entities.target;

import java.util.HashMap;

import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import net.jafama.FastMath;

public class FormationMotionData extends MotionData {
	private boolean rotate = true;
	private boolean move = true;
	private boolean rotationDirection = false; // false for counter-clockwise
												// and true for clockwise

	private Vector2d centroid;
	private final HashMap<Target, Vector2d> relativePositions;
	private HashMap<Target, Vector2d> velocityVectors;
	private double formationRotation;
	private double angularVelocity; // In rad/sec

	public FormationMotionData(Target[] targets, HashMap<Target, Vector2d> relativePositions, double movementAzimuth,
			double movementVelocity, double rotationVelocity) {
		super(targets, movementVelocity, movementAzimuth);
		this.relativePositions = relativePositions;
		this.angularVelocity = rotationVelocity;

		velocityVectors = new HashMap<Target, Vector2d>();
		for (Target t : targets) {
			velocityVectors.put(t, new Vector2d(0, 0));
		}
	}

	public Target[] getTargets() {
		return targets;
	}

	public Vector2d[] getPosition() {
		return targetsPositions;
	}

	public void setTargets(Target[] targets) {
		this.targets = targets;
	}

	// public Vector2d getCentroid() {
	// double x_sum = 0, y_sum = 0;
	// for (int i = 0; i < targets.length; i++) {
	// x_sum += targetsPositions[i].x;
	// y_sum += targetsPositions[i].y;
	// }
	//
	// return new Vector2d(x_sum / targets.length, y_sum / targets.length);
	// }

	public void setShapeRotationAngle(double targetRotation) {
		this.formationRotation = targetRotation;
	}

	public void setCentroid(Vector2d centroid) {
		this.centroid = centroid;
	}

	public void setRotate(boolean rotate) {
		this.rotate = rotate;
	}

	public void setMove(boolean move) {
		this.move = move;
	}

	// false for counter-clockwise and true for clockwise
	public void setRotationDirection(boolean rotationDirection) {
		this.rotationDirection = rotationDirection;
	}

	@Override
	public void move() {
		double rotationDelta = 0;
		if (rotate) {
			rotationDelta = angularVelocity / 10; // Assuming 100ms steps
			rotationDelta *= rotationDirection ? -1 : 1;
		}
		formationRotation += rotationDelta;

		if (move) {
			centroid.x = FastMath.cos(movementAzimuth) * movementDelta + centroid.x;
			centroid.y = FastMath.sin(movementAzimuth) * movementDelta + centroid.y;
		}

		// double cos = ((FastMath.cos(formationRotation) < 1E-5) ? 0 :
		// FastMath.cos(formationRotation));
		double cos = FastMath.cos(formationRotation);
		double sin = FastMath.sin(formationRotation);
		for (int i = 0; i < targets.length; i++) {
			// Rotation given by a 2D rotation matrix:
			// x= x.cos(tetha) - y.sin(tetha)
			// Y= x.sin(tetha) + y.cos(tetha)
			Vector2d relativePosition = relativePositions.get(targets[i]);
			Vector2d position = new Vector2d(0, 0);
			position.x = relativePosition.x * cos - relativePosition.y * sin;
			position.y = relativePosition.x * sin + relativePosition.y * cos;

			// Translation given by a 2D translation matrix:
			// x= x + dx
			// y= y + dy
			position.x += centroid.x;
			position.y += centroid.y;

			targetsPositions[i] = position;
			targets[i].setLatLon(CoordinateUtilities.cartesianToGPS(position));

			velocityVectors.get(targets[i]);
			velocityVectors.put(targets[i], calculateVelocityVector(targets[i]));
		}
	}

	public Vector2d getVelocityVector(Target t) {
		return velocityVectors.get(t);
	}

	private Vector2d calculateVelocityVector(Target t) {
		double angVeloc = angularVelocity;
		double movVeloc = movementVelocity;
		if (!rotate) {
			angVeloc = 0;
		}

		if (!move) {
			movVeloc = 0;
		}

		Vector2d relativePosition = relativePositions.get(t);

		// Given by the angle of vector (0,0)->(targetx,targety) +- Pi/2
		// (depending on the movement direction)
		double angular_momentum_angle = (FastMath.PI / 2) * (rotationDirection ? -1 : 1) + relativePosition.getAngle()
				+ formationRotation;

		// Given by the formula v=r*w (v=linear velocity, r=radius, w=angular
		// velocity), we obtain the linear velocity from the radius and the
		// angular velocity
		double angular_momentum_intensity = relativePosition.length() * angVeloc;

		// Convert angular momentum coordinates from polar to Cartesian
		double x_coordinate = angular_momentum_intensity * FastMath.cos(angular_momentum_angle);
		double y_coordinate = angular_momentum_intensity * FastMath.sin(angular_momentum_angle);
		Vector2d angularMomentum = new Vector2d(x_coordinate, y_coordinate);

		// Convert linear velocity coordinates from polar to Cartesian
		x_coordinate = movVeloc * FastMath.cos(movementAzimuth);
		y_coordinate = movVeloc * FastMath.sin(movementAzimuth);
		Vector2d linearVelocity = new Vector2d(x_coordinate, y_coordinate);

		// Sum the two vectors
		Vector2d sumVector = new Vector2d(0, 0);

		sumVector.add(angularMomentum);
		sumVector.add(linearVelocity);

		return sumVector;
	}
}