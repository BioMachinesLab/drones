package fieldtests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JOptionPane;

import org.joda.time.LocalDateTime;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.motion.MotionData.MovementType;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import gui.DroneGUI;
import gui.panels.CommandPanel;
import gui.utils.FormationParametersPane;
import main.DroneControlConsole;

public class FaultInjectionFormationScript extends FieldTestScript {
	private Vector2d INIT_FENCE_SIZE = new Vector2d(40, 40);
	private double WAYPOINTS_MIN_DISTANCE = 3.5;
	private double WAYPOINTS_MAX_DISTANCE = 40;
	private int FAULT_DURATION = 50; // In seconds
	private boolean VARY_FAULT_DURATION = true;

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
	private Formation formation;
	private ArrayList<Waypoint> startingWaypoints;

	private int secondToInjectFault;
	private Random random;
	private String faultyRobot;

	private double faultDuration;

	public FaultInjectionFormationScript(DroneControlConsole console, CommandPanel commandPanel) {
		super(console, commandPanel);
	}

	@Override
	public void run() {

		try {
			super.run();

			System.out.println("Getting options!");
			readUserOptions();

			/*
			 * Choose the drone and set the time step to initiate fault
			 * condition
			 */
			random = new Random(randomSeed);
			if (VARY_FAULT_DURATION) {
				// If faultDuration=500, the varied fault duration is in
				// [250,750] range
				faultDuration = (random.nextDouble() - 0.5) * FAULT_DURATION;
			}

			secondToInjectFault = (int) (random.nextDouble() * (experimentsDuration - faultDuration));
			faultyRobot = ips[random.nextInt(ips.length)];
			System.out.println("Robot " + faultyRobot + " will fail at time=" + secondToInjectFault);

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
			if (JOptionPane.showConfirmDialog(console.getGUI(),
					"Start formation " + formation.getFormationType().name() + " experiment?", "Confirm when ready",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
				startExperiment();
			} else {
				stopControllers(new ArrayList<String>(Arrays.asList(ips)),
						"failed" + formation.getFormationType().name() + " fault injection experiment");
			}

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
						: new String[] { "preset_rendition0", "15", "5", "240", "20", "1,2,3,4,5", "1" });
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
			ips[i] = "192.168.3." + ips[i];
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

		HashMap<String, String> args = new HashMap<String, String>();
		args.put("randomSeed", Long.toString(randomSeed));
		args.put("targetMovementType", MovementType.ROTATIONAL.name());
		args.put("formationMovementType", MovementType.LINEAR.name());
		args.put("radiusOfObjectPositioning", Double.toString(radiusOfPositioning));

		FormationParametersPane builder = new FormationParametersPane(args);
		builder.triggerPane();
		formation = builder.buildFormation(center);

		clearMapEntities();
		addEntityToMap(centralPoint);
		addEntityToMap(formation);

		System.out.println("> Generated " + formation.getFormationType() + " formation");
	}

	private void startExperiment() {
		clearMapEntities();
		addEntityToMap(formation);
		deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));

		((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(false);

		String description = startExperimentTimer(controller + "_" + formation.getFormationType() + "_faultinjection");
		startControllers(robots, controller, description, experimentsStartDelay);

		// Wait for start time
		// while (new LocalDateTime().minusHours(1).isBefore(startTime)) {
		while (startTime.isAfter(console.getGPSTime())) {
			System.out.print(".");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}

		System.out.println();
		// GO GO GO Experiments running!
		((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(true);
		System.out.printf("Experiments started at %s. Description: %s%n", console.getGPSTime().toString(), description);
		try {
			Thread.sleep(secondToInjectFault * 1000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("Injecting faults on " + faultyRobot + " robot");
		stopControllers(singletonList(faultyRobot), description);

		try {
			Thread.sleep((experimentsDuration - secondToInjectFault) * 1000);
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

			startTime = new LocalDateTime(console.getGPSTime()).plusSeconds(experimentsStartDelay);

			String str = "year=" + startTime.getYear() + ",";
			str += "monthOfYear=" + startTime.getMonthOfYear() + ",";
			str += "dayOfMonth=" + startTime.getDayOfMonth() + ",";
			str += "hourOfDay=" + startTime.getHourOfDay() + ",";
			str += "minuteOfHour=" + startTime.getMinuteOfHour() + ",";
			str += "secondOfMinute=" + startTime.getSecondOfMinute();

			args.setArgument("startDate", str);
			args.setArgument("updateEntities", 1);
			System.out.println(args);

			startControllers(ips, args);

			System.out.printf("Starting experiments in %d seconds (%s). Waiting for start time!%n",
					experimentsStartDelay, startTime.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
