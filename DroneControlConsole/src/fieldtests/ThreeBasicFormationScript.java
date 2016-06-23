package fieldtests;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.joda.time.LocalDateTime;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Formation.FormationType;
import commoninterface.entities.target.motion.MotionData.MovementType;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import gui.DroneGUI;
import gui.panels.CommandPanel;
import gui.utils.FormationParametersPane;
import main.DroneControlConsole;
import network.mobileAppServer.shared.dataObjects.DroneData;
import network.mobileAppServer.shared.dataObjects.DronesSet;

public class ThreeBasicFormationScript extends FieldTestScript {
	private Vector2d INIT_FENCE_SIZE = new Vector2d(40, 40);
	private double WAYPOINTS_MIN_DISTANCE = 3.5;
	private double WAYPOINTS_MAX_DISTANCE = 40;

	private static String[] lastOptions = null;
	private LocalDateTime startTime = null;

	private String controller;
	private double radiusOfPositioning;
	private int nDrones;
	private int experimentsDuration;
	private int experimentsStartDelay;
	private String[] ips;
	private long randomSeed;
	private ArrayList<String> robots;

	private Waypoint centralPoint;
	private int nextFormation;
	private ArrayList<Formation> formations;
	private ArrayList<Waypoint> startingWaypoints;

	public ThreeBasicFormationScript(DroneControlConsole console, CommandPanel commandPanel) {
		super(console, commandPanel);
	}

	@Override
	public void run() {

		try {
			super.run();
			boolean firstRun = true;

			System.out.println("Getting options!");
			readUserOptions();

			/*
			 * Generate initial positions
			 */
			System.out.println("Generating starting positions!");
			generateInitialPositions(true);

			int ready = JOptionPane.showConfirmDialog(console.getGUI(), "Agree with start positions?", "Position check",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (ready != JOptionPane.YES_OPTION) {
				clearMapEntities();
				addEntityToMap(centralPoint);
				return;
			}

			/*
			 * Select formation location and shape
			 */
			System.out.println("Calculating formation positions...");
			generateFormations();

			nextFormation = selectStartingFormation();
			if (nextFormation == -1) {
				clearMapEntities();
				addEntityToMap(centralPoint);
				return;
			}

			/*
			 * Go to starting positions
			 */
			System.out.println("Heading to start positions!");
			clearMapEntities();
			for (Waypoint wp : startingWaypoints) {
				addEntityToMap(wp);
			}
			deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));

			for (int i = 0; i < robots.size(); i++) {
				goToWaypoint(singletonList(robots.get(i)), startingWaypoints.get(i));
				// System.out.println("Robot ID=" + robots.get(i) + "\t WP=" +
				// startingWaypoints.get(i));
			}

			/*
			 * Run experiments
			 */
			do {
				if (JOptionPane.showConfirmDialog(console.getGUI(),
						"Start formation " + formations.get(nextFormation).getFormationType().name() + " experiment?",
						"Confirm when ready", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

					if (!firstRun) {
						System.out.println("Generating starting positions!");

						clearMapEntities();
						centralPoint.setLatLon(getCentralPointAmongRobots());

						generateInitialPositions(false);

						ready = JOptionPane.showConfirmDialog(console.getGUI(), "Agree with start positions?",
								"Position check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (ready != JOptionPane.YES_OPTION) {
							clearMapEntities();
							addEntityToMap(centralPoint);
							return;
						}

						/*
						 * Go to starting positions
						 */
						System.out.println("Heading to start positions!");
						clearMapEntities();
						for (Waypoint wp : startingWaypoints) {
							addEntityToMap(wp);
						}
						deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));

						for (int i = 0; i < robots.size(); i++) {
							goToWaypoint(singletonList(robots.get(i)), startingWaypoints.get(i));
							// System.out.println("Robot ID=" + robots.get(i) +
							// "\t WP=" + startingWaypoints.get(i));
						}

						firstRun = false;
					}

					startExperiment(nextFormation);
				} else {
					stopControllers(new ArrayList<String>(Arrays.asList(ips)),
							"failed" + formations.get(nextFormation).getFormationType().name() + " experiment");
				}

				nextFormation++;
			} while (nextFormation < formations.size());

			clearMapEntities();
			addEntityToMap(centralPoint);
		} catch (Exception e) {
			System.err.println("Experiment aborted! -> " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void readUserOptions() {
		String[] options = getMultipleInputsDialog(
				new String[] { "Controller", "Radius of positioning: ", "Drones Quantity",
						"Experiments duration (seconds)", "Execute after (seconds)", "IPs", "Seed" },
				lastOptions != null ? lastOptions
						: new String[] { "rendition0", "15", "5", "240", "20", "1,2,3,4,5", "1" });
		if (options == null) {
			return;
		}
		lastOptions = options;

		controller = options[0];
		radiusOfPositioning = Double.parseDouble(options[1]);
		nDrones = Integer.parseInt(options[2]);
		experimentsDuration = Integer.parseInt(options[3]);
		experimentsStartDelay = Integer.parseInt(options[4]);
		ips = options[5].split("[;,\\-\\s]+");
		randomSeed = Long.parseLong(options[6]);

		robots = new ArrayList<String>();
		for (int i = 0; i < ips.length; i++) {
			robots.add("192.168.3." + ips[i]);
			// robots.add("192.168.1." + ips[i]);
		}
	}

	private void generateInitialPositions(boolean firstRun) {
		if (firstRun) {
			centralPoint = getCentralPoint();
		}
		clearMapEntities();

		GeoFence startFence = defineGeoFence(centralPoint.getLatLon(), INIT_FENCE_SIZE.getX(), INIT_FENCE_SIZE.getY());
		startingWaypoints = generateWaypointsInGeoFence(startFence, nDrones, WAYPOINTS_MIN_DISTANCE,
				WAYPOINTS_MAX_DISTANCE, randomSeed);

		for (Waypoint wp : startingWaypoints) {
			addEntityToMap(wp);
		}

		// addEntityToMap(centralPoint);
		addEntityToMap(startFence);
	}

	private void generateFormations() {
		Coordinate center = new Coordinate(centralPoint.getLatLon().getLat(), centralPoint.getLatLon().getLon());

		// Create a formation per shape
		formations = new ArrayList<Formation>();
		for (FormationType type : FormationType.values()) {
			if (type != FormationType.mix && type != FormationType.random) {

				HashMap<String, String> args = new HashMap<String, String>();
				args.put("formationShape", type.toString());
				args.put("lockFormationShape", "1");
				args.put("randomSeed", Long.toString(randomSeed));
				args.put("targetMovementType", MovementType.ROTATIONAL.name());
				args.put("formationMovementType", MovementType.LINEAR.name());
				args.put("radiusOfObjectPositioning", Double.toString(radiusOfPositioning));

				FormationParametersPane builder = new FormationParametersPane(args);
				builder.triggerPane();
				Formation formation = builder.buildFormation(center);
				formations.add(formation);

				clearMapEntities();
				addEntityToMap(centralPoint);
				addEntityToMap(formation);

				System.out.println("> Generated " + type.toString() + " formation");
			}
		}

		if (formations.isEmpty()) {
			return;
		}
	}

	private int selectStartingFormation() {
		ArrayList<String> names = new ArrayList<String>();
		for (Formation f : formations) {
			names.add(f.getName());
		}

		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(new JLabel("Starting formation:"));

		JComboBox<String> checkBox = new JComboBox<String>(names.toArray(new String[names.size()]));
		checkBox.setSelectedIndex(0);
		panel.add(checkBox);

		String[] options = { "Ok", "Cancel" };
		int response = JOptionPane.showOptionDialog(console.getGUI(), panel, "Choose starting formation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (response == 0) {
			return checkBox.getSelectedIndex();
		} else {
			return -1;
		}
	}

	private LatLon getCentralPointAmongRobots() {
		Vector2d position = new Vector2d(0, 0);
		DronesSet set = console.getDronesSet();

		for (String ip : ips) {
			DroneData d = set.getDrone(ip);
			LatLon latLon = new LatLon(d.getGPSData().getLatitudeDecimal(), d.getGPSData().getLongitudeDecimal());
			Vector2d pos = CoordinateUtilities.GPSToCartesian(latLon);

			position.x += pos.getX();
			position.y += pos.getY();
		}

		position.x /= ips.length;
		position.y /= ips.length;

		return CoordinateUtilities.cartesianToGPS(position);
	}

	private void startExperiment(int formationIndex) {
		clearMapEntities();
		addEntityToMap(formations.get(formationIndex));
		deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));

		((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(false);
		String description = startExperimentTimer(controller + "_" + formations.get(0).getFormationType());

		startControllers(robots, controller, description, experimentsStartDelay);
		System.out.println("Started controller. Waiting for start time!");

		// Wait for start time
		// while (new LocalDateTime().minusHours(1).isBefore(startTime)) {
		while (new LocalDateTime().isBefore(startTime)) {
			System.out.print(".");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}

		// GO GO GO Experiments running!
		((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(true);
		System.out.printf("Experiments started! Description: %s%n", description);
		try {
			Thread.sleep(experimentsDuration * 1000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}

		((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(false);
		stopControllers(robots, description);
		System.out.println("Experiment finished!");
	}

	private void startControllers(ArrayList<String> ips, String behavior, String description,
			int experimentsStartDelay) {
		try {
			CIArguments args = readConfigurationFile(behavior);
			args.setArgument("description", description);

			startTime = console.getGPSTime();
			LocalDateTime newStartTime = startTime.plusSeconds(experimentsStartDelay);

			String str = "year=" + newStartTime.getYear() + ",";
			str += "monthOfYear=" + newStartTime.getMonthOfYear() + ",";
			str += "dayOfMonth=" + newStartTime.getDayOfMonth() + ",";
			str += "hourOfDay=" + newStartTime.getHourOfDay() + ",";
			str += "minuteOfHour=" + newStartTime.getMinuteOfHour() + ",";
			str += "secondOfMinute=" + newStartTime.getSecondOfMinute();

			args.setArgument("startDate", str);
			args.setArgument("updateEntities", 1);
			System.out.println(args);

			startControllers(ips, args);

			System.out.printf("Starting experiments in %d seconds (%s)%n", experimentsStartDelay,
					newStartTime.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
