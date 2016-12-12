package commoninterface.entities.formation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import commoninterface.entities.GeoEntity;
import commoninterface.entities.formation.motion.MotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import net.jafama.FastMath;

public class Formation extends GeoEntity {
	private static final long serialVersionUID = 8538984759821378679L;
	private List<Target> targets;
	private HashMap<Target, Vector2d> targetsRelativePositions;

	private MotionData motionData = null;
	private FormationType formationType;
	private int targetQnt;
	private double targetRadius;
	private Random random = null;
	private long randomSeed;

	// Flags and settings
	private boolean variateFormationParameters = false;
	private double lineFormationDelta = 2.0;
	private double circleFormation_radius = 7.5;
	private double arrowFormation_xDelta = lineFormationDelta;
	private double arrowFormation_yDelta = lineFormationDelta;
	private Vector2d initialTranslation = null;
	private double initialRotation = 0;
	private double safetyDistance = 0;
	private double radiusOfObjPositioning = 0;

	public enum FormationType {
		line, circle, arrow, random, mix
	}

	public Formation(String name, LatLon latLon) {
		super(name, latLon);
		initialTranslation = CoordinateUtilities.GPSToCartesian(latLon);
	}

	/*
	 * Getters and setters
	 */
	public int getTargetQuantity() {
		return targets.size();
	}

	public List<Target> getTargets() {
		return targets;
	}

	public void setTargets(ArrayList<Target> targets) {
		this.targets = targets;
		this.targetQnt = targets.size();
	}

	public MotionData getMotionData() {
		return motionData;
	}

	public void setMotionData(MotionData motionData) {
		this.motionData = motionData;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long seed) {
		this.random = new Random(seed);
		this.randomSeed = seed;
	}

	public boolean getVariateFormationParameters() {
		return variateFormationParameters;
	}

	public void setVariateFormationParameters(boolean variateFormationParameters) {
		this.variateFormationParameters = variateFormationParameters;
	}

	public double getLineFormationDelta() {
		return lineFormationDelta;
	}

	public void setLineFormationDelta(double lineFormationDelta) {
		this.lineFormationDelta = lineFormationDelta;
	}

	public double getCircleFormationRadius() {
		return circleFormation_radius;
	}

	public void setCircleFormationRadius(double circleFormation_radius) {
		this.circleFormation_radius = circleFormation_radius;
	}

	public double getInitialRotation() {
		return initialRotation;
	}

	public void setInitialRotation(double initialRotation) {
		this.initialRotation = initialRotation;
	}

	public Vector2d getArrowFormationDeltas() {
		return new Vector2d(arrowFormation_xDelta, arrowFormation_yDelta);
	}

	public void setArrowFormationDeltas(Vector2d deltas) {
		this.arrowFormation_xDelta = deltas.x;
		this.arrowFormation_yDelta = deltas.y;
	}

	public FormationType getFormationType() {
		return formationType;
	}

	public void setFormationType(FormationType formationType) {
		this.formationType = formationType;
	}

	public double getTargetRadius() {
		return targetRadius;
	}

	public void setTargetRadius(double targetRadius) {
		this.targetRadius = targetRadius;
	}

	public double getSafetyDistance() {
		return safetyDistance;
	}

	public void setSafetyDistance(double safetyDistance) {
		this.safetyDistance = safetyDistance;
	}

	public double getRadiusOfObjPositioning() {
		return radiusOfObjPositioning;
	}

	public void setRadiusOfObjPositioning(double radiusOfObjPositioning) {
		this.radiusOfObjPositioning = radiusOfObjPositioning;
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

	@Override
	public Formation clone() {
		Formation form = new Formation(name, new LatLon(latLon));
		form.setRandomSeed(randomSeed);
		form.setMotionData(motionData.clone());
		form.setVariateFormationParameters(variateFormationParameters);
		form.setLineFormationDelta(lineFormationDelta);
		form.setLineFormationDelta(lineFormationDelta);
		form.setInitialRotation(initialRotation);
		form.setArrowFormationDeltas(new Vector2d(arrowFormation_xDelta, arrowFormation_yDelta));
		form.buildFormation(targetQnt, formationType, targetRadius);

		return form;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Formation) {
			Formation form = (Formation) obj;

			HashMap<String, Target> ts = new HashMap<String, Target>();
			for (Target t : targets) {
				ts.put(t.getName(), t);
			}

			for (Target t : form.getTargets()) {
				if (!ts.get(t.getName()).equals(t)) {
					return false;
				}
			}

			boolean b = form.getFormationType().equals(formationType);
			boolean c = form.getName().equals(name);
			boolean d = form.getTargetQuantity() == targetQnt;
			boolean e = form.getTargetRadius() == targetRadius;
			boolean f = form.getRandomSeed() == randomSeed;
			boolean g = form.getVariateFormationParameters() == variateFormationParameters;
			boolean h = form.getLineFormationDelta() == lineFormationDelta;
			boolean i = form.getCircleFormationRadius() == circleFormation_radius;
			boolean j = form.getArrowFormationDeltas().getX() == arrowFormation_xDelta;
			boolean k = form.getArrowFormationDeltas().getY() == arrowFormation_yDelta;
			boolean l = form.getLatLon().equals(latLon);
			boolean m = form.getInitialRotation() == initialRotation;
			boolean n = form.getSafetyDistance() == safetyDistance;
			boolean o = form.getRadiusOfObjPositioning() == radiusOfObjPositioning;
			boolean p = form.getTargets().size() == targets.size();

			return b && c && d && e && f && g && h && i && j && k && l && m && n && o && p;
		} else {
			return false;
		}
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
		case random:
			generateRandomFormation();
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
			start_y = -(yDelta + targetRadius) * (targetQnt / 2) + (yDelta + targetRadius);
			vertice = (targetQnt / 2);
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
				positions[i] = new Vector2d(start_x + i * (xDelta + targetRadius),
						(start_y + (i - 1) * (yDelta + targetRadius)) * factor);

				factor = -factor;

				if (targetQnt > 2) {
					positions[i + 1] = new Vector2d(start_x + (i + 1) * (xDelta + targetRadius),
							(start_y + i * (yDelta + targetRadius)) * factor);
					i++;
				}
			} else if (i > vertice && targetQnt % 2 == 0) {
				positions[i] = new Vector2d(start_x + i * (xDelta + targetRadius),
						(start_y + (i - 1) * (yDelta + targetRadius)) * factor);
			} else {
				positions[i] = new Vector2d(start_x + i * (xDelta + targetRadius),
						(start_y + i * (yDelta + targetRadius)) * factor);
			}

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

	private void generateRandomFormation() {
		Vector2d[] positions = new Vector2d[targetQnt];
		for (int i = 0; i < targetQnt; i++) {
			positions[i] = null;
		}

		for (int i = 0; i < targetQnt; i++) {
			Vector2d position = null;
			do {
				double radius = radiusOfObjPositioning;
				if (variateFormationParameters) {
					radius *= random.nextDouble();
				}

				double orientation = (random.nextDouble() * FastMath.PI * 2) % 360;

				double x = radius * FastMath.cos(orientation);
				double y = radius * FastMath.sin(orientation);
				position = new Vector2d(x, y);

			} while (!safeForTarget(position, positions));

			positions[i] = position;
		}

		placeTargets(positions);
	}

	private boolean safeForTarget(Vector2d position, Vector2d[] positions) {
		for (Vector2d p : positions) {
			if (p == null) {
				continue;
			}

			if ((position.distanceTo(p) - targetRadius * 2) - safetyDistance <= 0) {
				return false;
			}
		}

		return true;
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

		if (getMotionData() != null) {
			setLatLon(getMotionData().calculatePosition(time));
		}
	}
}
