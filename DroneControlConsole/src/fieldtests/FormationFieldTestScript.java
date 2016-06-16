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
import commoninterface.entities.target.Formation.FormationType;
import commoninterface.entities.target.motion.MotionData.MovementType;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import gui.DroneGUI;
import gui.panels.CommandPanel;
import gui.utils.FormationParametersPane;
import main.DroneControlConsole;

public class FormationFieldTestScript extends FieldTestScript {
	private static String[] lastOptions = null;
	private LocalDateTime startTime = null;

	public FormationFieldTestScript(DroneControlConsole console, CommandPanel commandPanel) {
		super(console, commandPanel);
	}

	@Override
	public void run() {

		try {
			super.run();
			/*
			 * Read options
			 */
			System.out.println("Getting options!");
			String[] options = getMultipleInputsDialog(
					new String[] { "Controller", "Distance (meters) ", "Radius of positioning: ", "Drones Quantity",
							"Walking time (seconds)", "Execute after (seconds)", "IPs", "Seed" },
					lastOptions != null ? lastOptions
							: new String[] { "preset_simple_controller0", "0", "15", "3", "240", "20",
									listToString(getSelectedIPs()), "1" });
			if (options == null) {
				return;
			}
			lastOptions = options;

			String controller = options[0];
			int distance = Integer.parseInt(options[1]);
			double radiusOfPositioning = Double.parseDouble(options[2]);
			int nDrones = Integer.parseInt(options[3]);
			int time = Integer.parseInt(options[4]);
			int experimentsStartDelay = Integer.parseInt(options[5]);
			String[] ips = options[6].split("[;,\\-\\s]+");
			long seed = Long.parseLong(options[7]);

			ArrayList<String> robots = new ArrayList<String>();
			for (int i = 0; i < ips.length; i++) {
				robots.add(ips[i]);
			}

			/*
			 * Generate starting positions
			 */
			System.out.println("Generating starting positions!");
			Waypoint startWP = getCentralPoint();
			clearMapEntities();

			Vector2d vec = CoordinateUtilities.GPSToCartesian(startWP.getLatLon());
			vec.y -= distance;

			Waypoint dispWP = new Waypoint("disp", CoordinateUtilities.cartesianToGPS(vec));

			GeoFence startFence = defineGeoFence(dispWP.getLatLon(), 40, 40);
			ArrayList<Waypoint> startWPs = generateWaypointsInGeoFence(startFence, nDrones, 3.5, 40, seed);

			for (Waypoint wp : startWPs) {
				addEntityToMap(wp);
			}
			// addEntityToMap(startWP);
			addEntityToMap(startFence);

			/*
			 * Confirm starting positions
			 */
			int ready = JOptionPane.showConfirmDialog(console.getGUI(), "Agree with start positions?", "Position check",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (ready != JOptionPane.YES_OPTION) {
				super.clearMapEntities();
				super.addEntityToMap(startWP);
				return;
			}

			/*
			 * Select formation location and shape
			 */
			System.out.println("Calculating formation position... ");
			Random r = new Random(seed);
			double radius = radiusOfPositioning * r.nextDouble();
			double orientation = (r.nextDouble() * Math.PI * 2) % 360;

			double x = radius * Math.cos(orientation);
			double y = radius * Math.sin(orientation);
			Coordinate center = new Coordinate(dispWP.getLatLon().getLat(), dispWP.getLatLon().getLon());

			// Create a formation per shape
			ArrayList<Formation> formations = new ArrayList<Formation>();
			for (FormationType type : FormationType.values()) {
				if (type != FormationType.mix) {
					HashMap<String, String> args = new HashMap<String, String>();
					args.put("formationShape", type.toString());
					args.put("lockFormationShape", "1");
					args.put("targetMovementType", MovementType.ROTATIONAL.name());
					args.put("formationMovementType", MovementType.LINEAR.name());

					FormationParametersPane builder = new FormationParametersPane(args);
					builder.triggerPane();
					formations.add(builder.buildFormation(center));
				}
			}

			if (formations.isEmpty()) {
				return;
			}

			addEntityToMap(formations.get(0));
			deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));

			/*
			 * Confirm starting positions
			 */
			int set = JOptionPane.showConfirmDialog(console.getGUI(), "Agree with formation position?",
					"Position check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (set != JOptionPane.YES_OPTION) {
				clearMapEntities();
				addEntityToMap(startWP);
				return;
			}
			/*
			 * Go to starting positions
			 */
			System.out.println("Heading to start positions!");
			for (int i = 0; i < robots.size(); i++) {
				goToWaypoint(singletonList(robots.get(i)), startWPs.get(i));
				System.out.println("Robot ID=" + robots.get(i) + "\t WP=" + startWPs.get(i));
			}

			/*
			 * Ask for permission to start
			 */
			if (JOptionPane.showConfirmDialog(console.getGUI(),
					"Yes to start formation experiment, no to kill (" + formations.get(0).getFormationType().name()
							+ " formation)",
					"Confirm when ready", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

				addEntityToMap(formations.get(0));
				deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));
				((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(false);

				String description = super.startExperimentTimer(controller);

				// First formation
				System.out.println("Started controller. Waiting for start time!");
				startControllers(robots, controller, description, experimentsStartDelay);

				// Wait for start time
				// while (new LocalDateTime().minusHours(1).isBefore(startTime))
				// {
				while (new LocalDateTime().isBefore(startTime)) {
					System.out.print(".");
					Thread.sleep(100);
				}

				// GO GO GO Experiments running!
				((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(true);
				System.out.printf("Experiments started! Description: %s\tFormation: %s\n", description,
						formations.get(0).getFormationType());
				Thread.sleep(time * 1000);

				((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(false);
				super.stopControllers(robots, description);
				System.out.println("Experiment finished!");
				stopControllers(robots, description);

				if (formations.size() > 1) {
					for (int i = 1; i < formations.size(); i++) {
						if (JOptionPane.showConfirmDialog(console.getGUI(),
								"Next experiment (" + formations.get(i).getFormationType().name() + " formation)?",
								"Confirm when ready", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

							clearMapEntities();

							// Recover initial positions
							for (Waypoint wp : startWPs) {
								addEntityToMap(wp);
							}
							addEntityToMap(startWP);
							addEntityToMap(startFence);
							addEntityToMap(formations.get(i));
							deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));

							/*
							 * Go to starting positions
							 */
							System.out.println("Heading to start positions!");
							for (int j = 0; j < robots.size(); j++) {
								goToWaypoint(singletonList(robots.get(j)), startWPs.get(j));
							}

							if (JOptionPane.showConfirmDialog(console.getGUI(),
									"Yes to start formation experiment, no to kill ("
											+ formations.get(i).getFormationType().name() + " formation)",
									"Confirm when ready", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
								addEntityToMap(formations.get(i));
								deployMapEntities(new ArrayList<String>(Arrays.asList(ips)));
								((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(false);

								// First formation
								description = super.startExperimentTimer(controller);
								System.out.println("Started controller. Waiting for start time!");
								startControllers(robots, controller, description, experimentsStartDelay);

								// Wait for start time
								while (new LocalDateTime().isBefore(startTime)) {
									System.out.print(".");
									Thread.sleep(100);
								}

								// GO GO GO Experiments running!
								((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(true);
								System.out.printf("Experiments started! Description: %s\tFormation: %s\n", description,
										formations.get(i).getFormationType());
								Thread.sleep(time * 1000);

								((DroneGUI) console.getGUI()).getMapPanel().setFormationUpdate(false);
								super.stopControllers(robots, description);
								System.out.println("Experiment finished!");

								stopControllers(robots, description);
							}
						}
					}
				}
			} else {
				stopControllers(new ArrayList<String>(Arrays.asList(ips)), "failed");
			}
		} catch (Exception e) {
			System.err.println("Experiment aborted! -> " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void startControllers(ArrayList<String> ips, String behavior, String description,
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

			System.out.printf("Starting experiments in %d seconds (%s)\n", experimentsStartDelay,
					newStartTime.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
