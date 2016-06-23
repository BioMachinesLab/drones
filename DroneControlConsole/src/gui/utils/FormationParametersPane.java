package gui.utils;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import commoninterface.entities.GeoEntity;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Formation.FormationType;
import commoninterface.entities.target.Target;
import commoninterface.entities.target.motion.LinearMotionData;
import commoninterface.entities.target.motion.MixedMotionData;
import commoninterface.entities.target.motion.MotionData;
import commoninterface.entities.target.motion.MotionData.MovementType;
import commoninterface.entities.target.motion.RotationMotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.jcoord.LatLon;

public class FormationParametersPane {
	private final String PARAMETERS_FILE = "formationParameters.conf";

	private HashMap<String, String> args;
	private HashMap<String, String> extraArgs;

	private JPanel mainPanel;
	private JPanel formationOptionsPanel;
	private JPanel motionOptionsPanel;
	private JPanel movementPatchPanel;

	private int paneAnswer = 0;
	private Random random;

	/*
	 * Formation options panel
	 */
	private JTextField targetsQuantityTextField;
	private JComboBox<FormationType> formationTypeComboBox;
	private JTextField lineFormationDeltaTextField;
	private JTextField arrowFormationXDeltaTextField;
	private JTextField arrowFormationYDeltaTextField;
	private JTextField circleFormationRadiusTextField;
	private JCheckBox variateFormationParametersCheckBox;
	private JTextField initialRotationTextField;
	private JTextField targetRadiusTextField;
	private JTextField safetyDistanceTextField;
	private JTextField radiusOfObjPositioningTextField;
	private JTextField randomSeedTextField;

	// Parameters
	private int targetQuantity;
	private double lineFormationDelta;
	private double arrowFormationXDelta;
	private double arrowFormationYDelta;
	private double circleFormationRadius;
	private boolean variateFormationParameters;
	private double initialRotation;
	private double targetRadius;
	private double safetyDistance;
	private double radiusOfObjPositioning;
	private Long randomSeed;
	private FormationType formationType;

	/*
	 * Motion options panel
	 */
	private JTextField targetMovementVelocityTextField;
	private JTextField targetMovementAzimuthTextField;
	private JCheckBox moveTargetsCheckBox;
	private JCheckBox variateTargetTranslationSpeedCheckBox;
	private JCheckBox variateTargetAzimuthCheckBox;

	private JTextField rotationVelocityVelocityTextField;
	private JCheckBox rotateTargetsCheckBox;
	private JCheckBox variateTargetRotationSpeedCheckBox;
	private JCheckBox targetsRotateDirectionCheckBox;
	private JCheckBox variateTargetRotationDirectionCheckBox;

	// Parameters
	private double targetMovementVelocity;
	private double targetMovementAzimuth;
	private boolean moveTarget;
	private boolean variateTargetsSpeed;
	private boolean variateTargetsAzimuth;
	private double rotationVelocity;
	private boolean rotateFormation;
	private boolean variateRotationVelocity;
	private boolean rotationDirection;
	private boolean variateRotationDirection;

	/*
	 * Motion patch
	 */
	private JComboBox<MovementType> formationMovementTypeComboBox;
	private JComboBox<MovementType> targetMovementTypeComboBox;

	// Parameters
	MovementType formationMovementType;
	MovementType targetMovementType;

	public FormationParametersPane(HashMap<String, String> extraArgs) {
		this.extraArgs = extraArgs;
		args = readArgsFromFile(PARAMETERS_FILE);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		buildFormationOptionsPanel();
		buildMotionOptionsPanel();
		buildMovementPatchPanel();

		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		all.add(formationOptionsPanel);
		all.add(motionOptionsPanel);
		all.add(movementPatchPanel);

		JScrollPane scrollPane = new JScrollPane(all, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		mainPanel.add(scrollPane);
	}

	public void triggerPane() {
		paneAnswer = JOptionPane.showConfirmDialog(null, mainPanel, "Formation parameters",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (paneAnswer == JOptionPane.OK_OPTION) {
			try {
				// Formation
				targetQuantity = Integer.parseInt(targetsQuantityTextField.getText());
				lineFormationDelta = Double.parseDouble(lineFormationDeltaTextField.getText());
				arrowFormationXDelta = Double.parseDouble(arrowFormationXDeltaTextField.getText());
				arrowFormationYDelta = Double.parseDouble(arrowFormationYDeltaTextField.getText());
				circleFormationRadius = Double.parseDouble(circleFormationRadiusTextField.getText());
				variateFormationParameters = variateFormationParametersCheckBox.isSelected();
				initialRotation = Double.parseDouble(initialRotationTextField.getText());
				targetRadius = Double.parseDouble(targetRadiusTextField.getText());
				safetyDistance = Double.parseDouble(safetyDistanceTextField.getText());
				radiusOfObjPositioning = Double.parseDouble(radiusOfObjPositioningTextField.getText());
				randomSeed = Long.parseLong(randomSeedTextField.getText());
				random = new Random(randomSeed);
				formationType = (FormationType) formationTypeComboBox.getSelectedItem();

				// Movement
				targetMovementVelocity = Double.parseDouble(targetMovementVelocityTextField.getText());
				targetMovementAzimuth = Double.parseDouble(targetMovementAzimuthTextField.getText());
				moveTarget = moveTargetsCheckBox.isSelected();
				variateTargetsSpeed = variateTargetTranslationSpeedCheckBox.isSelected();
				variateTargetsAzimuth = variateTargetAzimuthCheckBox.isSelected();

				rotationVelocity = Double.parseDouble(rotationVelocityVelocityTextField.getText());
				rotateFormation = rotateTargetsCheckBox.isSelected();
				variateRotationVelocity = variateTargetRotationSpeedCheckBox.isSelected();
				rotationDirection = targetsRotateDirectionCheckBox.isSelected();
				variateRotationDirection = variateTargetRotationDirectionCheckBox.isSelected();

				// Movement patch
				formationMovementType = (MovementType) formationMovementTypeComboBox.getSelectedItem();
				targetMovementType = (MovementType) targetMovementTypeComboBox.getSelectedItem();
			} catch (NumberFormatException e) {
				paneAnswer = JOptionPane.CANCEL_OPTION;
				JOptionPane.showMessageDialog(null, "Illegal argument(s)!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void buildFormationOptionsPanel() {
		formationOptionsPanel = new JPanel();
		formationOptionsPanel.setBorder(BorderFactory.createTitledBorder("Formation options"));
		formationOptionsPanel.setLayout(new BoxLayout(formationOptionsPanel, BoxLayout.Y_AXIS));

		JPanel topPanel = new JPanel(new GridLayout(6, 2));
		topPanel.add(new JLabel("Targets quantity:"));
		targetsQuantityTextField = new JTextField(10);
		targetsQuantityTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("targetsQuantity") != null) {
			targetsQuantityTextField.setText(args.get("targetsQuantity"));
		}
		topPanel.add(targetsQuantityTextField);

		topPanel.add(new JLabel("Formation shape"));
		formationTypeComboBox = new JComboBox<FormationType>(FormationType.values());
		DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		formationTypeComboBox.setRenderer(dlcr);
		int index = 0;
		if (args.get("formationShape") != null) {
			index = FormationType.valueOf(args.get("formationShape")).ordinal();
		}
		if (extraArgs != null && extraArgs.get("formationShape") != null) {
			index = FormationType.valueOf(extraArgs.get("formationShape")).ordinal();
			formationTypeComboBox.setSelectedIndex(index);
			formationTypeComboBox.setEnabled(false);
		}
		formationTypeComboBox.setSelectedIndex(index);
		topPanel.add(formationTypeComboBox);

		topPanel.add(new JLabel("Line formation delta (m):"));
		lineFormationDeltaTextField = new JTextField(10);
		lineFormationDeltaTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("lineFormationDelta") != null) {
			lineFormationDeltaTextField.setText(args.get("lineFormationDelta"));
		}
		topPanel.add(lineFormationDeltaTextField);

		topPanel.add(new JLabel("Circle formation radius (m):"));
		circleFormationRadiusTextField = new JTextField(10);
		circleFormationRadiusTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("circleFormation_radius") != null) {
			circleFormationRadiusTextField.setText(args.get("circleFormation_radius"));
		}
		topPanel.add(circleFormationRadiusTextField);

		topPanel.add(new JLabel("Safety random position distance (m):"));
		safetyDistanceTextField = new JTextField(10);
		safetyDistanceTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("safetyRandomPositionDistance") != null) {
			safetyDistanceTextField.setText(args.get("safetyRandomPositionDistance"));
		}
		topPanel.add(safetyDistanceTextField);

		topPanel.add(new JLabel("Radius of object positioning (m):"));
		radiusOfObjPositioningTextField = new JTextField(10);
		radiusOfObjPositioningTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("radiusOfObjectPositioning") != null) {
			radiusOfObjPositioningTextField.setText(args.get("radiusOfObjectPositioning"));
		}
		if (extraArgs != null && extraArgs.get("radiusOfObjectPositioning") != null) {
			radiusOfObjPositioningTextField.setText(extraArgs.get("radiusOfObjectPositioning"));
		}
		topPanel.add(radiusOfObjPositioningTextField);
		formationOptionsPanel.add(topPanel);

		JPanel deltasPanel = new JPanel(new GridLayout(2, 2));
		deltasPanel.setBorder(BorderFactory.createTitledBorder("Arrow formation deltas (m)"));
		JLabel arrowFormation_xDeltaLabel = new JLabel("X=");
		arrowFormation_xDeltaLabel.setHorizontalAlignment(JLabel.CENTER);
		deltasPanel.add(arrowFormation_xDeltaLabel);
		arrowFormationXDeltaTextField = new JTextField(10);
		arrowFormationXDeltaTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("arrowFormation_xDelta") != null) {
			arrowFormationXDeltaTextField.setText(args.get("arrowFormation_xDelta"));
		}
		deltasPanel.add(arrowFormationXDeltaTextField);
		JLabel arrowFormation_yDeltaLabel = new JLabel("Y=");
		arrowFormation_yDeltaLabel.setHorizontalAlignment(JLabel.CENTER);
		deltasPanel.add(arrowFormation_yDeltaLabel);
		arrowFormationYDeltaTextField = new JTextField(10);
		arrowFormationYDeltaTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("arrowFormation_yDelta") != null) {
			arrowFormationYDeltaTextField.setText(args.get("arrowFormation_yDelta"));
		}
		deltasPanel.add(arrowFormationYDeltaTextField);
		formationOptionsPanel.add(deltasPanel);

		JPanel bottomPanel = new JPanel(new GridLayout(3, 2));
		bottomPanel.add(new JLabel("Initial rotation (º):"));
		initialRotationTextField = new JTextField(10);
		initialRotationTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("initialRotation") != null) {
			initialRotationTextField.setText(args.get("initialRotation"));
		}
		if (extraArgs != null && extraArgs.get("initialRotation") != null) {
			initialRotationTextField.setText(extraArgs.get("initialRotation"));
			initialRotationTextField.setEditable(false);
		}
		bottomPanel.add(initialRotationTextField);

		bottomPanel.add(new JLabel("Target radius (m):"));
		targetRadiusTextField = new JTextField(10);
		targetRadiusTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("targetRadius") != null) {
			targetRadiusTextField.setText(args.get("targetRadius"));
		}
		bottomPanel.add(targetRadiusTextField);

		bottomPanel.add(new JLabel("Random seed:"));
		randomSeedTextField = new JTextField(10);
		randomSeedTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("randomSeed") != null) {
			randomSeedTextField.setText(args.get("randomSeed"));
		}
		if (extraArgs != null && extraArgs.get("randomSeed") != null) {
			randomSeedTextField.setText(extraArgs.get("randomSeed"));
			randomSeedTextField.setEditable(false);
		}
		bottomPanel.add(randomSeedTextField);
		formationOptionsPanel.add(bottomPanel);

		JPanel dummyPanel = new JPanel(new GridLayout(1, 1));
		variateFormationParametersCheckBox = new JCheckBox("Add noise to formation parameters");
		// variateFormationParametersCheckBox.setHorizontalAlignment(JCheckBox.LEFT);
		if (args.get("noiseInParameters") != null) {
			variateFormationParametersCheckBox.setSelected((args.get("noiseInParameters").equals("1")));
		} else {
			variateFormationParametersCheckBox.setSelected(false);
		}
		dummyPanel.add(variateFormationParametersCheckBox);
		formationOptionsPanel.add(dummyPanel);
	}

	private void buildMotionOptionsPanel() {
		motionOptionsPanel = new JPanel();
		motionOptionsPanel.setLayout(new BoxLayout(motionOptionsPanel, BoxLayout.Y_AXIS));
		motionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Motion options"));

		// Translation
		JPanel translationPanel = new JPanel();
		translationPanel.setLayout(new BoxLayout(translationPanel, BoxLayout.Y_AXIS));
		translationPanel.setBorder(BorderFactory.createTitledBorder("Translation"));
		JPanel translationTextFieldsPanel = new JPanel(new GridLayout(2, 2));
		translationTextFieldsPanel.add(new JLabel("Movement velocity (m/s):"));
		targetMovementVelocityTextField = new JTextField(10);
		targetMovementVelocityTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("targetMovementVelocity") != null) {
			targetMovementVelocityTextField.setText(args.get("targetMovementVelocity"));
		}
		translationTextFieldsPanel.add(targetMovementVelocityTextField);

		translationTextFieldsPanel.add(new JLabel("Target movement azimuth (º):"));
		targetMovementAzimuthTextField = new JTextField(10);
		targetMovementAzimuthTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("targetMovementAzimuth") != null) {
			targetMovementAzimuthTextField.setText(args.get("targetMovementAzimuth"));
		}
		translationTextFieldsPanel.add(targetMovementAzimuthTextField);
		translationPanel.add(translationTextFieldsPanel);

		JPanel translationCheckBoxesPanel = new JPanel(new GridLayout(3, 1));
		moveTargetsCheckBox = new JCheckBox("Move formation");
		// moveTargetsCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("moveTarget") != null) {
			moveTargetsCheckBox.setSelected((args.get("moveTarget").equals("1")));
		} else {
			moveTargetsCheckBox.setSelected(false);
		}
		translationCheckBoxesPanel.add(moveTargetsCheckBox);

		variateTargetTranslationSpeedCheckBox = new JCheckBox("Add noise to translation velocity");
		// variateTargetTranslationSpeedCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("variateTargetsSpeed") != null) {
			variateTargetTranslationSpeedCheckBox.setSelected((args.get("variateTargetsSpeed").equals("1")));
		} else {
			variateTargetTranslationSpeedCheckBox.setSelected(false);
		}
		translationCheckBoxesPanel.add(variateTargetTranslationSpeedCheckBox);

		variateTargetAzimuthCheckBox = new JCheckBox("Add noise to translation azimuth");
		// variateTargetAzimuthCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("variateTargetsAzimuth") != null) {
			variateTargetAzimuthCheckBox.setSelected((args.get("variateTargetsAzimuth").equals("1")));
		} else {
			variateTargetAzimuthCheckBox.setSelected(false);
		}
		translationCheckBoxesPanel.add(variateTargetAzimuthCheckBox);
		translationPanel.add(translationCheckBoxesPanel);
		motionOptionsPanel.add(translationPanel);

		// Rrotation
		JPanel rotationPanel = new JPanel();
		rotationPanel.setLayout(new BoxLayout(rotationPanel, BoxLayout.Y_AXIS));
		rotationPanel.setBorder(BorderFactory.createTitledBorder("Rotation"));
		JPanel rotationTextFieldsPanel = new JPanel(new GridLayout(1, 2));
		rotationTextFieldsPanel.add(new JLabel("Rotation velocity (rad/s):"));
		rotationVelocityVelocityTextField = new JTextField(10);
		rotationVelocityVelocityTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("rotationVelocity") != null) {
			rotationVelocityVelocityTextField.setText(args.get("rotationVelocity"));
		}
		rotationTextFieldsPanel.add(rotationVelocityVelocityTextField);
		rotationPanel.add(rotationTextFieldsPanel);

		JPanel rotationCheckBoxesPanel = new JPanel(new GridLayout(4, 1));
		rotateTargetsCheckBox = new JCheckBox("Rotate formation");
		// rotateTargetsCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("rotateFormation") != null) {
			rotateTargetsCheckBox.setSelected((args.get("rotateFormation").equals("1")));
		} else {
			rotateTargetsCheckBox.setSelected(false);
		}
		rotationCheckBoxesPanel.add(rotateTargetsCheckBox);

		variateTargetRotationSpeedCheckBox = new JCheckBox("Add noise to rotation velocity");
		// variateTargetRotationSpeedCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("variateRotationVelocity") != null) {
			variateTargetRotationSpeedCheckBox.setSelected((args.get("variateRotationVelocity").equals("1")));
		} else {
			variateTargetRotationSpeedCheckBox.setSelected(false);
		}
		rotationCheckBoxesPanel.add(variateTargetRotationSpeedCheckBox);

		targetsRotateDirectionCheckBox = new JCheckBox("Rotation direction (true is clockwise)");
		// targetsRotateDirectionCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("rotationDirection") != null) {
			targetsRotateDirectionCheckBox.setSelected((args.get("rotationDirection").equals("1")));
		} else {
			targetsRotateDirectionCheckBox.setSelected(false);
		}
		rotationCheckBoxesPanel.add(targetsRotateDirectionCheckBox);

		variateTargetRotationDirectionCheckBox = new JCheckBox("Add noise to rotation direction");
		// variateTargetRotationDirectionCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("variateRotationDirection") != null) {
			variateTargetRotationDirectionCheckBox.setSelected((args.get("variateRotationDirection").equals("1")));
		} else {
			variateTargetRotationDirectionCheckBox.setSelected(false);
		}
		rotationCheckBoxesPanel.add(variateTargetRotationDirectionCheckBox);
		rotationPanel.add(rotationCheckBoxesPanel);
		motionOptionsPanel.add(rotationPanel);
	}

	private void buildMovementPatchPanel() {
		movementPatchPanel = new JPanel(new GridLayout(2, 2));
		// movementPatchPanel.setLayout(new BoxLayout(movementPatchPanel,
		// BoxLayout.Y_AXIS));
		movementPatchPanel.setBorder(BorderFactory.createTitledBorder("Movement assign"));

		movementPatchPanel.add(new JLabel("Formation movement"));
		formationMovementTypeComboBox = new JComboBox<MovementType>(MovementType.values());
		DefaultListCellRenderer dlcr_1 = new DefaultListCellRenderer();
		dlcr_1.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		formationMovementTypeComboBox.setRenderer(dlcr_1);
		int index = 0;
		if (args.get("formationMovementType") != null) {
			index = MovementType.valueOf(args.get("formationMovementType")).ordinal();
		}
		if (extraArgs != null && extraArgs.get("formationMovementType") != null) {
			index = MovementType.valueOf(extraArgs.get("formationMovementType")).ordinal();
			formationMovementTypeComboBox.setEnabled(false);
		}
		formationMovementTypeComboBox.setSelectedIndex(index);
		movementPatchPanel.add(formationMovementTypeComboBox);

		movementPatchPanel.add(new JLabel("Target movement"));
		targetMovementTypeComboBox = new JComboBox<MovementType>(MovementType.values());
		DefaultListCellRenderer dlcr_2 = new DefaultListCellRenderer();
		dlcr_2.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		targetMovementTypeComboBox.setRenderer(dlcr_2);
		index = 0;
		if (args.get("targetMovementType") != null) {
			index = MovementType.valueOf(args.get("targetMovementType")).ordinal();
		}
		if (extraArgs != null && extraArgs.get("targetMovementType") != null) {
			index = MovementType.valueOf(extraArgs.get("targetMovementType")).ordinal();
			targetMovementTypeComboBox.setEnabled(false);
		}
		targetMovementTypeComboBox.setSelectedIndex(index);
		movementPatchPanel.add(targetMovementTypeComboBox);
	}

	private HashMap<String, String> readArgsFromFile(String fileName) {
		HashMap<String, String> args = new HashMap<String, String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));

			String line = br.readLine();
			while (line != null) {
				if (!line.startsWith("#")) {
					String[] elements = line.trim().split("=");
					if (elements.length >= 2) {
						args.put(elements[0].trim(), elements[1].trim());
					}
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			System.err.printf("[%s] File %s not found!%n", getClass().getName(), fileName);
		} catch (IOException e) {
			System.err.printf("[%s] Error reading file %s!%n", getClass().getName(), fileName);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.err.printf("[%s] Error closing file %s%n%s%n", getClass().getName(), fileName,
							e.getMessage());
				}
			}
		}

		return args;
	}

	public Formation buildFormation(Coordinate c) {
		if (paneAnswer == JOptionPane.OK_OPTION) {
			Formation formation = new Formation("formation_" + formationType, new LatLon(c.getLat(), c.getLon()));
			formation.setLineFormationDelta(lineFormationDelta);
			formation.setArrowFormationDeltas(new Vector2d(arrowFormationXDelta, arrowFormationYDelta));
			formation.setCircleFormationRadius(circleFormationRadius);
			formation.setVariateFormationParameters(variateFormationParameters);
			formation.setInitialRotation(initialRotation * Math.PI / 180);
			formation.setRandomSeed(randomSeed);
			formation.setSafetyDistance(safetyDistance);
			formation.setRadiusOfObjPositioning(radiusOfObjPositioning);
			formation.buildFormation(targetQuantity, formationType, targetRadius);

			setMotionData(formation, formationMovementType);

			for (Target t : formation.getTargets()) {
				setMotionData(t, targetMovementType, formation.getLatLon());
			}

			return formation;
		} else {
			return null;
		}
	}

	public void setMotionData(GeoEntity entity, MovementType type, LatLon... rotationCenter) {
		MotionData motionData = null;
		switch (type) {
		case LINEAR:
			motionData = generateLinearMotionData(entity);
			break;
		case ROTATIONAL:
			motionData = generateRotationalMotionData(entity, rotationCenter[0]);
			break;
		case MIXED:
			MotionData linearMotionData = generateLinearMotionData(entity);
			MotionData rotationalMotionData = generateRotationalMotionData(entity, rotationCenter[0]);

			motionData = new MixedMotionData(entity);
			if (linearMotionData != null) {
				((MixedMotionData) motionData).addMotionData(linearMotionData);
			}

			if (rotationalMotionData != null) {
				((MixedMotionData) motionData).addMotionData(rotationalMotionData);
			}
			break;
		}

		if (entity instanceof Target) {
			((Target) entity).setMotionData(motionData);
		} else if (entity instanceof Formation) {
			((Formation) entity).setMotionData(motionData);
		}
	}

	private LinearMotionData generateLinearMotionData(GeoEntity entity) {
		double targetsVelocity = targetMovementVelocity;
		if (variateTargetsSpeed) {
			targetsVelocity = 0.1 * targetMovementVelocity + random.nextDouble() * targetMovementVelocity * 2;
		}

		double targetsAzimuth = (targetMovementAzimuth * Math.PI / 180);
		if (variateTargetsAzimuth) {
			targetsAzimuth = random.nextDouble() * Math.PI * 2;
		}

		if (moveTarget) {
			return new LinearMotionData(entity, targetsVelocity, targetsAzimuth);
		} else {
			return null;
		}
	}

	private RotationMotionData generateRotationalMotionData(GeoEntity entity, LatLon rotationCenter) {
		double angularVelocity = rotationVelocity;
		if (variateRotationVelocity) {
			angularVelocity = 0.1 * rotationVelocity + random.nextDouble() * rotationVelocity * 1.5;
		}

		boolean direction = rotationDirection;
		if (variateRotationDirection) {
			direction = random.nextBoolean();
		}

		if (rotateFormation) {
			return new RotationMotionData(entity, rotationCenter, angularVelocity, direction);
		} else {
			return null;
		}

	}

	public static void main(String[] args) {
		new FormationParametersPane(null).triggerPane();

	}
}
