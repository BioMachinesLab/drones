package environment.target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.target.FormationMotionData;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FormationMultiTargetEnvironment extends TargetEnvironment {
	private static final long serialVersionUID = -6746690210418679841L;

	public enum FormationType {
		line, circle, arrow, mix
	}

	private int targetsQuantity = 1;
	private boolean variateTargetsQuantity = false;
	private boolean variateFormationParameters = false;
	private boolean onePerOneRobotTarget = false;
	private boolean decideRandomExclusiveMoveType = false;

	private boolean injectFaults = false;
	private boolean varyFaultDuration = true;
	private AquaticDrone faultyRobot = null;
	private int faultDuration = 100;
	private int timestepToInjectFaults;

	private double rotationVelocity = 0.05;
	private boolean rotateFormation = false;
	private boolean rotationDirection = false;
	private boolean variateRotationVelocity = false;
	private boolean variateRotationDirection = false;

	private double lineFormationDelta = 2.0;
	private double circleFormation_radius = 7.5;
	private double arrowFormation_xDelta = lineFormationDelta;
	private double arrowFormation_yDelta = lineFormationDelta;

	private String formationShape = null;
	private FormationMotionData formation = null;
	private HashMap<Target, Vector2d> targetsRelativePositions = new HashMap<Target, Vector2d>();

	public FormationMultiTargetEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		targetsQuantity = args.getArgumentAsIntOrSetDefault("targetsQuantity", targetsQuantity);
		variateTargetsQuantity = args.getArgumentAsIntOrSetDefault("variateTargetsQnt", 0) == 1;
		onePerOneRobotTarget = args.getArgumentAsIntOrSetDefault("onePerOneRobotTarget", 0) == 1;

		rotateFormation = args.getArgumentAsIntOrSetDefault("rotateFormation", 0) == 1;
		rotationVelocity = args.getArgumentAsDoubleOrSetDefault("rotationVelocity", rotationVelocity);
		rotationDirection = args.getArgumentAsIntOrSetDefault("rotationDirection", 0) == 1;
		variateRotationVelocity = args.getArgumentAsIntOrSetDefault("variateRotationVelocity", 0) == 1;
		variateRotationDirection = args.getArgumentAsIntOrSetDefault("variateRotationDirection", 0) == 1;

		formationShape = args.getArgumentAsStringOrSetDefault("formationShape", null);
		variateFormationParameters = args.getArgumentAsIntOrSetDefault("variateFormationParameters", 0) == 1;

		lineFormationDelta = args.getArgumentAsDoubleOrSetDefault("lineFormation_xDelta", lineFormationDelta);
		circleFormation_radius = args.getArgumentAsDoubleOrSetDefault("circleFormation_radius", circleFormation_radius);
		arrowFormation_xDelta = args.getArgumentAsDoubleOrSetDefault("arrowFormation_xDelta", arrowFormation_xDelta);
		arrowFormation_yDelta = args.getArgumentAsDoubleOrSetDefault("arrowFormation_yDelta", arrowFormation_yDelta);
		injectFaults = args.getArgumentAsIntOrSetDefault("injectFaults", 0) == 1;
		decideRandomExclusiveMoveType = args.getArgumentAsIntOrSetDefault("decideRandomExclusiveMoveType", 0) == 1;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);

		int definedTargetsQuantity = 0;
		if (onePerOneRobotTarget) {
			definedTargetsQuantity = robots.size();
		} else {
			definedTargetsQuantity = targetsQuantity;
		}

		if (variateTargetsQuantity) {
			definedTargetsQuantity += definedTargetsQuantity * simulator.getRandom().nextDouble() * 3;
			if (definedTargetsQuantity == 0) {
				definedTargetsQuantity = 1;
			}
		}

		if (injectFaults) {
			if (varyFaultDuration) {
				// If faultDuration=100, the varied fault duration is in
				// [50,150] range
				faultDuration += (simulator.getRandom().nextDouble() - 0.5) * faultDuration;
			}

			timestepToInjectFaults = (int) (simulator.getRandom().nextDouble() * (steps - faultDuration));

			boolean choosed = false;
			while (!choosed) {
				int position = simulator.getRandom().nextInt(robots.size());

				if (robots.get(position) instanceof AquaticDrone) {
					faultyRobot = (AquaticDrone) robots.get(position);
					choosed = true;
				}
			}
		}

		if (definedTargetsQuantity <= 0) {
			throw new IllegalArgumentException(
					"Targets quantity needs to be equal or greater than 1 (currently " + definedTargetsQuantity + ")");
		}

		targets = new Target[definedTargetsQuantity];
		FormationType formation = FormationType.valueOf(formationShape);

		// If we are in mix mode, just pick one of the formations to use
		if (formation == FormationType.mix) {
			int bound = FormationType.values().length;
			int position = simulator.getRandom().nextInt(bound - 1);
			formation = FormationType.values()[position];
		}

		switch (formation) {
		case arrow:
			generateArrowFormation(definedTargetsQuantity);
			break;
		case circle:
			generateCircleFormation(definedTargetsQuantity);
			break;
		case line:
			generateLineFormation(definedTargetsQuantity);
			break;
		default:
			throw new IllegalArgumentException("[" + getClass().getName() + "] Non defined shape!");
		}

		for (Robot r : robots) {
			do {
				positionDroneInRandomPos((AquaticDrone) r, simulator);
				updateCollisions(0);
			} while (!safeForRobot(r, simulator));

			if (r instanceof AquaticDroneCI) {
				((AquaticDroneCI) r).setEntities(new ArrayList<Entity>(Arrays.asList(targets)));
			}
		}

		setup = true;
	}

	private void generateArrowFormation(int definedTargetsQuantity) {
		double xDelta = 0, yDelta = 0;
		if (variateFormationParameters) {
			xDelta = targetRadius * 1.5 + simulator.getRandom().nextDouble() * arrowFormation_xDelta * 2;
			yDelta = targetRadius * 1.5 + simulator.getRandom().nextDouble() * arrowFormation_yDelta * 2;
		} else {
			xDelta = arrowFormation_xDelta;
			yDelta = arrowFormation_yDelta;
		}

		double start_x, start_y;
		int vertice;
		if (definedTargetsQuantity % 2 == 0) {
			start_x = -(xDelta + targetRadius) * (definedTargetsQuantity / 2) + (xDelta + targetRadius) / 2;
			start_y = -(yDelta + targetRadius) * (definedTargetsQuantity / 2) + (yDelta + targetRadius) / 2;
			vertice = definedTargetsQuantity / 2;
		} else {
			start_x = -(xDelta + targetRadius) * ((definedTargetsQuantity - 1) / 2);
			start_y = -(yDelta + targetRadius) * ((definedTargetsQuantity - 1) / 2);

			if (definedTargetsQuantity > 1) {
				if (definedTargetsQuantity % 2 == 0) {
					vertice = (definedTargetsQuantity + 1) / 2;
				} else {
					vertice = (definedTargetsQuantity - 1) / 2;
				}
			} else {
				vertice = 1;
			}
		}

		Vector2d[] positions = new Vector2d[definedTargetsQuantity];
		int factor = 1;
		for (int i = 0; i < definedTargetsQuantity; i++) {
			if (i == vertice && definedTargetsQuantity % 2 == 0) {
				factor = -factor;
			}

			positions[i] = new Vector2d(start_x + i * (xDelta + targetRadius),
					(start_y + i * (yDelta + targetRadius)) * factor);

			if (i == vertice && definedTargetsQuantity % 2 != 0) {
				factor = -factor;
			}

		}

		placeTargets(positions, FormationType.arrow);
	}

	private void generateCircleFormation(int definedTargetsQuantity) {
		double segmentsAngle = FastMath.PI * 2 / definedTargetsQuantity;
		Vector2d[] positions = new Vector2d[definedTargetsQuantity];

		double radius = 0;
		if (variateFormationParameters) {
			double minRadius = (targetRadius * definedTargetsQuantity * 2.2) / (2 * FastMath.PI);
			radius = minRadius + simulator.getRandom().nextDouble() * circleFormation_radius * 1.5;
		} else {
			radius = circleFormation_radius;
		}

		for (int i = 0; i < definedTargetsQuantity; i++) {
			double angle = (segmentsAngle * i) % (FastMath.PI * 2);
			double x_pos = radius * FastMath.cos(angle);
			double y_pos = radius * FastMath.sin(angle);

			positions[i] = new Vector2d(x_pos, y_pos);
		}

		placeTargets(positions, FormationType.circle);
	}

	private void generateLineFormation(int definedTargetsQuantity) {
		double start_x, xDelta = 0;
		if (variateFormationParameters) {
			xDelta = targetRadius * 1.5 + simulator.getRandom().nextDouble() * lineFormationDelta * 2;
		} else {
			xDelta = lineFormationDelta;
		}

		if (definedTargetsQuantity % 2 == 0) {
			start_x = -(xDelta + targetRadius) * (definedTargetsQuantity / 2) + (xDelta + targetRadius) / 2;
		} else {
			start_x = -(xDelta + targetRadius) * ((definedTargetsQuantity - 1) / 2);
		}

		Vector2d[] positions = new Vector2d[definedTargetsQuantity];
		for (int i = 0; i < definedTargetsQuantity; i++) {
			positions[i] = new Vector2d(start_x + i * (xDelta + targetRadius), 0);
		}

		placeTargets(positions, FormationType.line);
	}

	private void placeTargets(Vector2d[] positions, FormationType type) {
		double targetsAzimuth = movementAzimuth;
		double targetsVelocity = movementVelocity;
		double angularVelocity = rotationVelocity;
		boolean direction = rotationDirection;

		if (variateTargetVelocity) {
			targetsVelocity = 0.1 * movementVelocity + simulator.getRandom().nextDouble() * movementVelocity * 2;
		}

		if (variateTargetAzimuth) {
			targetsAzimuth = simulator.getRandom().nextDouble() * Math.PI * 2;
		}

		if (variateRotationVelocity) {
			angularVelocity = 0.1 * rotationVelocity + simulator.getRandom().nextDouble() * rotationVelocity * 1.5;
		}

		if (variateRotationDirection) {
			direction = simulator.getRandom().nextBoolean();
		}

		if (decideRandomExclusiveMoveType) {
			// Rotate
			if (simulator.getRandom().nextDouble() < 0.5) {
				rotateFormation = true;
				moveTargets = false;

			} else {
				// Translation
				rotateFormation = false;
				moveTargets = true;
			}
		}

		double radius = radiusOfObjPositioning * simulator.getRandom().nextDouble();
		double orientation = (simulator.getRandom().nextDouble() * FastMath.PI * 2) % 360;
		double translation_x = radius * FastMath.cos(orientation);
		double translation_y = radius * FastMath.sin(orientation);
		double initialRotationAngle = simulator.getRandom().nextDouble() * 2 * FastMath.PI;

		Vector2d[] transformedPositons = transformPositions(positions, initialRotationAngle,
				new Vector2d(translation_x, translation_y));

		targetsRelativePositions = new HashMap<Target, Vector2d>();
		for (int i = 0; i < transformedPositons.length; i++) {
			Target target = new Target("target" + i, CoordinateUtilities.cartesianToGPS(transformedPositons[i]));
			target.setRadius(targetRadius);
			targets[i] = target;
			targetsRelativePositions.put(target, positions[i]);
		}

		formation = new FormationMotionData(targets, targetsRelativePositions, targetsAzimuth, targetsVelocity,
				angularVelocity);
		formation.setCentroid(new Vector2d(translation_x, translation_y));
		formation.setShapeRotationAngle(initialRotationAngle);
		formation.setRotationDirection(direction);
		formation.setRotate(rotateFormation);
		formation.setMove(moveTargets);
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

	@Override
	public void update(double time) {
		formation.move();

		if (injectFaults) {
			// Time to recover from fault?
			if (time > timestepToInjectFaults + faultDuration) {
				faultyRobot.setFault(false);
			} else {
				if (simulator.getTime() >= timestepToInjectFaults) {
					faultyRobot.setFault(true);
				}
			}
		}
	}

}
