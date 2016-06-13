package commoninterface.entities.target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import commoninterface.entities.GeoEntity;
import commoninterface.entities.target.motion.MotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import net.jafama.FastMath;

public class Formation extends GeoEntity {
	private static final long serialVersionUID = 8538984759821378679L;
	private ArrayList<Target> targets;
	private HashMap<Target, Vector2d> targetsRelativePositions;

	private MotionData motionData = null;
	private FormationType formationType;
	private int targetQnt;
	private double targetRadius;
	private Random random = null;

	// Flags and settings
	private boolean variateFormationParameters = false;
	private double lineFormationDelta = 2.0;
	private double circleFormation_radius = 7.5;
	private double arrowFormation_xDelta = lineFormationDelta;
	private double arrowFormation_yDelta = lineFormationDelta;
	private Vector2d initialTranslation = null;
	private double initialRotation = 0;

	public enum FormationType {
		line, circle, arrow, mix
	}

	public Formation(String name, LatLon latLon) {
		super(name, latLon);
		initialTranslation = CoordinateUtilities.GPSToCartesian(latLon);
	}

	/*
	 * Getters and setters
	 */
	public ArrayList<Target> getTargets() {
		return targets;
	}

	public int getTargetQuantity() {
		return targetQnt;
	}

	public MotionData getMotionData() {
		return motionData;
	}

	public void setMotionData(MotionData motionData) {
		this.motionData = motionData;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public void setVariateFormationParameters(boolean variateFormationParameters) {
		this.variateFormationParameters = variateFormationParameters;
	}

	public void setLineFormationDelta(double lineFormationDelta) {
		this.lineFormationDelta = lineFormationDelta;
	}

	public void setCircleFormationRadius(double circleFormation_radius) {
		this.circleFormation_radius = circleFormation_radius;
	}

	public void setInitialRotation(double initialRotation) {
		this.initialRotation = initialRotation;
	}

	public void setArrowFormationDeltas(Vector2d deltas) {
		this.arrowFormation_xDelta = deltas.x;
		this.arrowFormation_yDelta = deltas.y;
	}

	public FormationType getFormationType() {
		return formationType;
	}

	/*
	 * Default methods
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Formation] name=" + name + " (");

		for (int i = 0; i < targets.size(); i++) {
			str.append("target " + targets.get(i).getName());

			if (i < targets.size() - 1) {
				str.append(" ");
			}
		}
		str.append(")");

		return str.toString();
	}

	/*
	 * Build formation
	 */
	public void buildFormation(int targetQnt, FormationType formationType, double targetRadius) {
		this.formationType = formationType;
		this.targetQnt = targetQnt;
		this.targetRadius = targetRadius;

		// If we are in mix mode, just pick one of the formations to use
		if (formationType == FormationType.mix) {
			int bound = FormationType.values().length;
			int position = random.nextInt(bound - 1);
			formationType = FormationType.values()[position];
		}

		switch (formationType) {
		case arrow:
			generateArrowFormation();
			break;
		case circle:
			generateCircleFormation();
			break;
		case line:
			generateLineFormation();
			break;
		default:
			throw new IllegalArgumentException("[" + getClass().getName() + "] Non defined shape!");
		}

	}

	private void generateArrowFormation() {
		double xDelta = 0, yDelta = 0;
		if (variateFormationParameters) {
			xDelta = targetRadius * 1.5 + random.nextDouble() * arrowFormation_xDelta * 2;
			yDelta = targetRadius * 1.5 + random.nextDouble() * arrowFormation_yDelta * 2;
		} else {
			xDelta = arrowFormation_xDelta;
			yDelta = arrowFormation_yDelta;
		}

		double start_x, start_y;
		int vertice;
		if (targetQnt % 2 == 0) {
			start_x = -(xDelta + targetRadius) * (targetQnt / 2) + (xDelta + targetRadius) / 2;
			start_y = -(yDelta + targetRadius) * (targetQnt / 2) + (yDelta + targetRadius) / 2;
			vertice = targetQnt / 2;
		} else {
			start_x = -(xDelta + targetRadius) * ((targetQnt - 1) / 2);
			start_y = -(yDelta + targetRadius) * ((targetQnt - 1) / 2);

			if (targetQnt > 1) {
				if (targetQnt % 2 == 0) {
					vertice = (targetQnt + 1) / 2;
				} else {
					vertice = (targetQnt - 1) / 2;
				}
			} else {
				vertice = 1;
			}
		}

		Vector2d[] positions = new Vector2d[targetQnt];
		int factor = 1;
		for (int i = 0; i < targetQnt; i++) {
			if (i == vertice && targetQnt % 2 == 0) {
				factor = -factor;
			}

			positions[i] = new Vector2d(start_x + i * (xDelta + targetRadius),
					(start_y + i * (yDelta + targetRadius)) * factor);

			if (i == vertice && targetQnt % 2 != 0) {
				factor = -factor;
			}
		}

		placeTargets(positions);
	}

	private void generateCircleFormation() {
		double segmentsAngle = FastMath.PI * 2 / targetQnt;
		Vector2d[] positions = new Vector2d[targetQnt];

		double radius = 0;
		if (variateFormationParameters) {
			double minRadius = (targetRadius * targetQnt * 2.2) / (2 * FastMath.PI);
			radius = minRadius + random.nextDouble() * circleFormation_radius * 1.5;
		} else {
			radius = circleFormation_radius;
		}

		for (int i = 0; i < targetQnt; i++) {
			double angle = (segmentsAngle * i) % (FastMath.PI * 2);
			double x_pos = radius * FastMath.cos(angle);
			double y_pos = radius * FastMath.sin(angle);

			positions[i] = new Vector2d(x_pos, y_pos);
		}

		placeTargets(positions);
	}

	private void generateLineFormation() {
		double start_x, xDelta = 0;
		if (variateFormationParameters) {
			xDelta = targetRadius * 1.5 + random.nextDouble() * lineFormationDelta * 2;
		} else {
			xDelta = lineFormationDelta;
		}

		if (targetQnt % 2 == 0) {
			start_x = -(xDelta + targetRadius) * (targetQnt / 2) + (xDelta + targetRadius) / 2;
		} else {
			start_x = -(xDelta + targetRadius) * ((targetQnt - 1) / 2);
		}

		Vector2d[] positions = new Vector2d[targetQnt];
		for (int i = 0; i < targetQnt; i++) {
			positions[i] = new Vector2d(start_x + i * (xDelta + targetRadius), 0);
		}

		placeTargets(positions);
	}

	private void placeTargets(Vector2d[] positions) {
		Vector2d[] transformedPositions = transformPositions(positions, initialRotation, initialTranslation);
		targets = new ArrayList<Target>();
		targetsRelativePositions = new HashMap<Target, Vector2d>();

		for (int i = 0; i < transformedPositions.length; i++) {
			Target target = new Target("formation_target_" + i,
					CoordinateUtilities.cartesianToGPS(transformedPositions[i]), targetRadius);
			target.setFormation(this);
			targets.add(target);
			targetsRelativePositions.put(target, positions[i]);
		}
	}

	private Vector2d[] transformPositions(Vector2d[] positions, double rotationAngle, Vector2d translation) {
		Vector2d[] newPositions = new Vector2d[positions.length];

		double cos = FastMath.cos(rotationAngle);
		double sin = FastMath.sin(rotationAngle);
		for (int i = 0; i < positions.length; i++) {
			// Rotation given by a 2D rotation matrix:
			// x= x.cos(tetha) - y.sin(tetha)
			// Y= x.sin(tetha) + y.cos(tetha)
			newPositions[i] = new Vector2d();
			newPositions[i].x = positions[i].x * cos - positions[i].y * sin;
			newPositions[i].y = positions[i].x * sin + positions[i].y * cos;

			// Translation given by a 2D translation matrix:
			// x= x + dx
			// y= y + dy
			newPositions[i].x += translation.x;
			newPositions[i].y += translation.y;
		}

		return newPositions;
	}

	public void step(double time) {
		for (Target t : targets) {
			t.step(time);
		}
	}
}
