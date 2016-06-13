package gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.EntitiesBroadcastMessage;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.Message;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import commoninterface.utils.jcoord.LatLon;
import fieldtests.FieldTestScript;
import gui.DroneGUI;
import gui.RobotGUI;
import main.DroneControlConsole;
import main.RobotControlConsole;
import network.CommandSender;
import observers.ForagingMissionMonitor;
import threads.UpdateThread;

public class CommandPanel extends UpdatePanel {
	private static final long serialVersionUID = 4038133860317693008L;

	public static String CONTROLLERS_FOLDER = "controllers";
	public static String SCRIPTS_LIST = "scripts.txt";

	private String myHostname = "";

	private JLabel statusMessage;
	private BehaviorMessage currentMessage;
	private JTextArea config;
	private JTextField logMessage;
	private JTextPane selectedDrones;
	private JTextArea autoDeployArea;
	private JLabel timerLabel;
	public JButton sendLog;
	private RobotGUI gui;
	private RobotControlConsole console;
	private boolean dronePanel = false;
	private JFrame neuralActivationsWindow;
	public JButton deploy;
	public JButton stopAll;
	public JButton entitiesButton;

	private JTextField gpsCoordinate = new JTextField("Latitude/ Longitude", 15);
	private JButton setWaypointButton = new JButton("Set waypoint");

	private ArrayList<String> availableBehaviors = new ArrayList<String>();
	private ArrayList<String> availableControllers = new ArrayList<String>();

	private JPanel presetsPanel = new JPanel();
	private HashMap<String, String> presetsConfig = new HashMap<String, String>();
	private JCheckBox autoDeployCheckBox;

	private ArrayList<Entity> mapEntities = new ArrayList<Entity>();

	private Timer timer;

	private JLabel currentExperiment;

	private UpdateThread thread;
	private ForagingMissionMonitor monitor;
	private FieldTestScript currentScript;

	/*
	 * author: @miguelduarte42 This has to be this way because some behaviors
	 * are specific to the RaspberryController, and this project cannot include
	 * RaspberryController because of PI4J.
	 */
	private String[] hardcodedClasses = new String[] { "CalibrationCIBehavior", "ShutdownCIBehavior",
			"LogDronesCIBehavior" };

	public CommandPanel(RobotControlConsole console, final RobotGUI gui) {
		updateHostname();

		this.console = console;

		this.gui = gui;

		if (gui instanceof DroneGUI) {
			dronePanel = true;
			monitor = new ForagingMissionMonitor((DroneControlConsole) console);
			// monitor.start();
		}

		initNeuralActivationsWindow();

		setBorder(BorderFactory.createTitledBorder("Commands"));

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel controllersPanel = new JPanel(new BorderLayout());

		populateControllers();
		populateScripts();

		selectedDrones = new JTextPane();
		selectedDrones.setText("Drones IDs");
		selectedDrones.setForeground(selectedDrones.getDisabledTextColor());
		StyledDocument doc = (StyledDocument) selectedDrones.getDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		selectedDrones.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if (selectedDrones.getText() == null || selectedDrones.getText().isEmpty()
						|| selectedDrones.getText().length() == 0) {
					selectedDrones.setText("Drones IDs");
					selectedDrones.setForeground(selectedDrones.getDisabledTextColor());
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (selectedDrones.getText().contains("Drones IDs")) {
					selectedDrones.setText("");
				}
				selectedDrones.setForeground(Color.BLACK);
			}
		});
		JScrollPane selectDronesScroll = new JScrollPane(selectedDrones);

		JPanel selectedPanel = new JPanel(new BorderLayout());
		selectedPanel.add(selectDronesScroll, BorderLayout.CENTER);
		selectedPanel.setBorder(BorderFactory.createTitledBorder("Drones selection"));

		presetsPanel.setBorder(BorderFactory.createTitledBorder("Controllers"));

		controllersPanel.add(presetsPanel, BorderLayout.SOUTH);
		topPanel.add(controllersPanel, BorderLayout.NORTH);

		controllersPanel.add(selectedPanel, BorderLayout.NORTH);

		deploy = new JButton("Deploy");
		stopAll = new JButton("Stop All");

		stopAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deployBehavior("ControllerCIBehavior", false);
			}
		});

		config = new JTextArea(7, 8);
		config.setFont(new Font("Monospaced", Font.PLAIN, 11));

		statusMessage = new JLabel("");
		statusMessage.setPreferredSize(new Dimension(10, 20));

		JPanel actionsPanel = new JPanel(new GridLayout(2, 2));

		if (dronePanel) {
			entitiesButton = new JButton("Deploy Entities");

			actionsPanel.add(entitiesButton);
			entitiesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deployEntities();
				}
			});

			JPanel autoDeployPanel = new JPanel(new GridLayout(1, 2));
			JLabel autoDeployLabel = new JLabel("Auto Deploy");
			autoDeployCheckBox = new JCheckBox();

			autoDeployPanel.add(autoDeployLabel);
			autoDeployPanel.add(autoDeployCheckBox);
			// buttons.add(autoDeployPanel);
		}

		actionsPanel.add(stopAll);

		JButton placeDronesButton = new JButton("Place Drones Randomly");
		placeDronesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				placeDronesRandomly();
			}
		});

		JButton plotButton = new JButton("Plot Neural Activations");
		plotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				neuralActivationsWindow.setVisible(true);
			}
		});

		JButton calibrateButton = new JButton("Calibrate");
		calibrateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Yes", "No" };
				int result = JOptionPane.showOptionDialog(null, "Start calibration ?", "Calibration",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

				if (result == 0) {
					statusMessage("CalibrationCIBehavior", true);
				} else if (result == 1) {
					// TODO: revert to old calibration
				}
			}
		});

		actionsPanel.add(placeDronesButton);
		actionsPanel.add(calibrateButton);
		// actionsPanel.add(plotButton);

		actionsPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

		add(topPanel, BorderLayout.NORTH);
		add(actionsPanel, BorderLayout.CENTER);

		JPanel deploy = new JPanel(new BorderLayout());
		deploy.setBorder(BorderFactory.createTitledBorder("Drone Deploy Area"));

		autoDeployArea = new JTextArea("Possible args: SEED, SIZE, SAFETY, SPECIAL\n(; delimiter)", 2, 30);
		autoDeployArea.setLineWrap(true);
		autoDeployArea.setForeground(autoDeployArea.getDisabledTextColor());
		autoDeployArea.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if (autoDeployArea.getText() == null || autoDeployArea.getText().isEmpty()
						|| autoDeployArea.getText().length() == 0) {
					autoDeployArea.setText("Possible args: SEED, SIZE, SAFETY, SPECIAL\n(; delimiter)");
					autoDeployArea.setForeground(autoDeployArea.getDisabledTextColor());
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (autoDeployArea.getText().contains("Possible args: SEED, SIZE, SAFETY, SPECIAL\n(; delimiter)")) {
					autoDeployArea.setText("");
					autoDeployArea.setForeground(Color.BLACK);
				}
			}
		});
		JScrollPane autoDeployAreaScroll = new JScrollPane(autoDeployArea);
		deploy.add(autoDeployAreaScroll, BorderLayout.CENTER);

		setWaypointButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				Scanner s = new Scanner(gpsCoordinate.getText());
				try {
					double lat = s.nextDouble();
					double lon = s.nextDouble();

					Waypoint wp = new Waypoint("wp", new LatLon(lat, lon));

					((DroneGUI) gui).getMapPanel().clearWaypoints();
					((DroneGUI) gui).getMapPanel().addWaypoint(wp);
				} catch (InputMismatchException ex) {
					throwErrorMessage("Illegal latitude/longitude format!");
				} finally {
					s.close();
				}

			}
		});
		gpsCoordinate.setHorizontalAlignment(JTextField.CENTER);
		gpsCoordinate.setForeground(gpsCoordinate.getDisabledTextColor());
		gpsCoordinate.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if (gpsCoordinate.getText() != null && !gpsCoordinate.getText().isEmpty()
						&& gpsCoordinate.getText().length() > 0) {
					Scanner s = new Scanner(gpsCoordinate.getText());
					try {
						s.nextDouble();
						s.nextDouble();
					} catch (InputMismatchException ex) {
						gpsCoordinate.setText("Latitude/ Longitude");
						gpsCoordinate.setForeground(gpsCoordinate.getDisabledTextColor());
					} finally {
						s.close();
					}
				} else {
					gpsCoordinate.setText("Latitude/ Longitude");
					gpsCoordinate.setForeground(gpsCoordinate.getDisabledTextColor());
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				if (gpsCoordinate.getText().contains("Latitude/ Longitude")) {
					gpsCoordinate.setText("");
					gpsCoordinate.setForeground(Color.BLACK);
				}
			}
		});

		JPanel waypointPanel = new JPanel();
		waypointPanel.add(gpsCoordinate);
		waypointPanel.add(setWaypointButton);

		JPanel south = new JPanel(new GridLayout(2, 2));
		south.add(statusMessage);
		south.add(new JLabel());

		timerLabel = new JLabel("");
		south.add(timerLabel);

		currentExperiment = new JLabel();
		south.add(currentExperiment);

		deploy.add(south, BorderLayout.SOUTH);
		deploy.add(waypointPanel, BorderLayout.NORTH);

		add(deploy, BorderLayout.SOUTH);

		timer = new Timer();
		timer.start();
	}

	private void initNeuralActivationsWindow() {
		neuralActivationsWindow = new JFrame("Neural Network Activations");
		neuralActivationsWindow.setSize(950, 600);
		neuralActivationsWindow.setLocationRelativeTo(gui);
		neuralActivationsWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private synchronized void statusMessage(String className, boolean status) {
		setText("");

		CIArguments translatedArgs = new CIArguments(config.getText().replaceAll("\\s+", ""), true);

		if (status)
			currentMessage = new BehaviorMessage(className, translatedArgs.getCompleteArgumentString(), status,
					myHostname);
		else
			currentMessage = new BehaviorMessage(className, "", status, myHostname);

		notifyAll();
	}

	private void deployBehavior(String className, boolean status) {
		CIArguments translatedArgs = new CIArguments(config.getText().replaceAll("\\s+", ""), true);
		BehaviorMessage m;

		if (status) {

			String description = getExperimentDescription(className);
			translatedArgs.setArgument("description", description);

			m = new BehaviorMessage(className, translatedArgs.getCompleteArgumentString(), status, myHostname);
			sendBroacastMessage("STARTING " + description);
		} else {
			m = new BehaviorMessage(className, "", status, myHostname);
			if (!currentExperiment.getText().isEmpty())
				sendBroacastMessage("STOPPING " + currentExperiment.getText());
		}

		deploy(m);
		if (status)
			timer.startTimer();
		else
			timer.stopTimer();
	}

	public String getExperimentDescription(String name) {
		DateTime now = new DateTime();
		String description = now.toString(DateTimeFormat.forPattern("HH-mm-ss")) + "-" + name;
		currentExperiment.setText(description);
		return description;
	}

	private void deployPreset(String presetName, String arguments) {
		CIArguments translatedArgs = new CIArguments(arguments.replaceAll("\\s+", ""), true);
		String type = translatedArgs.getArgumentAsString("type");

		if (type != null) {

			String description = getExperimentDescription(presetName);
			System.out.println(presetName + " " + description);
			translatedArgs.setArgument("description", description);
			sendBroacastMessage("STARTING " + description);

			BehaviorMessage m = new BehaviorMessage(type, translatedArgs.getCompleteArgumentString(), true, myHostname);
			deploy(m);
			timer.startTimer();
		} else
			JOptionPane.showMessageDialog(null, "Contoller type not defined on preset configuration file!");

	}

	public void deployEntities() {
		deployEntities(false);
	}

	private void sendBroacastMessage(String msg) {
		if (console instanceof DroneControlConsole) {
			// Messages get lost sometimes!!!
			for (int j = 0; j < 5; j++) {
				(((DroneControlConsole) console).getConsoleBroadcastHandler()).sendMessage(msg);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void deployEntities(boolean dynamicActiveId) {
		EntitiesMessage m = new EntitiesMessage(mapEntities, myHostname);
		deploy(m, dynamicActiveId);

		// This part is for other DroneControlConsoles to receive the updated
		// entities
		EntitiesBroadcastMessage msg = new EntitiesBroadcastMessage(mapEntities);
		sendBroacastMessage(msg.encode()[0]);
	}

	private synchronized void deploy(Message m) {
		deploy(m, false);
	}

	private synchronized void deploy(Message m, boolean dynamicIds) {
		setText("Deploying...");

		ArrayList<String> addresses = getSelectedAddresses();

		console.log("Deploying " + m + ";ADDRESSES;"
				+ addresses.toString().replace(", ", ";").replace("[", "").replace("]", ""));
		new CommandSender(m, addresses, this, dynamicIds).start();
	}

	public ArrayList<String> getSelectedAddresses() {

		String[] addresses = gui.getConnectionPanel().getCurrentAddresses();

		ArrayList<String> selectedAddresses = new ArrayList<String>();

		for (String s : addresses) {
			if (s != null && isSelectedAddress(s)) {
				selectedAddresses.add(s);
			}
		}

		return selectedAddresses;
	}

	public boolean isSelectedAddress(String s) {

		String str = selectedDrones.getText();

		if (str.trim().isEmpty())
			return true;

		if (str.contains("-")) {
			String[] list = str.split("-");
			int first = Integer.parseInt(list[0]);
			int last = Integer.parseInt(list[1]);

			for (int i = first; i <= last; i++) {
				if (s.endsWith("." + i))
					return true;
			}

		}

		if (str.contains(",")) {
			String[] list = str.split(",");

			for (String sList : list) {
				if (s.endsWith("." + sList))
					return true;
			}
		}

		return s.endsWith("." + str);
	}

	public void setText(String text) {
		statusMessage.setText(text);
	}

	private void throwErrorMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void populateControllers() {
		File controllersFolder = new File(CONTROLLERS_FOLDER);
		if (controllersFolder.exists() && controllersFolder.isDirectory()) {

			presetsPanel.setLayout(new GridLayout(0, 2));

			for (String s : controllersFolder.list()) {
				if (s.endsWith(".conf")) {
					if (s.startsWith("preset")) {
						String buttonName = s.split("\\.")[0].replace("preset_", "");
						JButton b = new JButton(buttonName);

						b.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								JButton b = (JButton) e.getSource();
								readConfigForButton(b.getText());
								String arguments = presetsConfig.get(b.getText());
								deployPreset(b.getText(), arguments);
							}
						});

						presetsPanel.add(b);

						readConfigForButton(b.getText());
					}
				}
				availableControllers.add(s);
			}
		}
	}

	private void populateScripts() {
		File scripts = new File(SCRIPTS_LIST);
		if (!scripts.exists()) {
			System.out.println("******** SCRIPTS FILE NOT FOUND ********");
		} else {
			try {
				BufferedReader br = new BufferedReader(new FileReader(scripts));
				String line = null;
				while ((line = br.readLine()) != null) {
					@SuppressWarnings("rawtypes")
					Class c = Class.forName(line);
					@SuppressWarnings("rawtypes")
					Constructor[] constr = c.getDeclaredConstructors();

					JButton b = new JButton(c.getSimpleName());
					b.setForeground(Color.BLUE);
					b.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (currentScript != null)
								currentScript.interrupt();
							try {
								currentScript = (FieldTestScript) constr[0].newInstance(console, CommandPanel.this);
								currentScript.start();
							} catch (Exception ex) {
								currentExperiment = null;
								ex.printStackTrace();
							}
						}
					});
					presetsPanel.add(b);
				}
				br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		JButton b = new JButton("Stop Script");
		b.setForeground(Color.BLUE);
		presetsPanel.add(b);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentScript != null)
					currentScript.interrupt();
			}
		});
	}

	private void readConfigForButton(String buttonText) {
		try {
			String config = readConfigurationFile(
					new File(CONTROLLERS_FOLDER + "/" + "preset_" + buttonText + ".conf"));
			presetsConfig.put(buttonText, config);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private String readConfigurationFile(File file) throws FileNotFoundException {
		String result = "";
		Scanner scanner = new Scanner(file);

		while (scanner.hasNext())
			result += scanner.nextLine() + "\n";

		scanner.close();
		return result;
	}

	public BehaviorMessage getCurrentMessage() {
		BehaviorMessage result = currentMessage;
		currentMessage = null;
		return result;
	}

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	@Override
	public synchronized void threadWait() {
		while (currentMessage == null) {
			try {
				wait();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public long getSleepTime() {
		return 0;
	}

	public JFrame getNeuralActivationsWindow() {
		return neuralActivationsWindow;
	}

	public void displayData(BehaviorMessage message) {
		String result = message.getSelectedBehavior() + ": ";
		result += message.getSelectedStatus() ? "start" : "stop";

		setText(result);
	}

	// public JTextArea getSelectedDronesTextField() {
	// return selectedDrones;
	// }

	public JTextPane getSelectedDronesTextField() {
		return selectedDrones;
	}

	public ArrayList<String> getAvailableBehaviors() {
		return availableBehaviors;
	}

	public ArrayList<String> getAvailableControllers() {
		return availableControllers;
	}

	public void updateMapInfo(ArrayList<Entity> updatedEntities) {
		mapEntities.clear();
		mapEntities.addAll(updatedEntities);

		if (autoDeployCheckBox.isSelected())
			deployEntities();

	}

	public void updateHostname() {
		myHostname = NetworkUtils.getHostname();
	}

	public void setLogText(String text) {
		logMessage.setText(text);
	}

	public void setConfiguration(String configStr) {
		config.setText(configStr);
	}

	public void placeDronesRandomly() {

		if (presetsConfig.get("waypoint") == null) {
			JOptionPane.showMessageDialog(null,
					"There is no default Waypoint controller defined! Create a file called \"preset_waypoint.conf\" in the \"controllers\" folder.");
			return;
		}

		int randomSeed = 1111;

		double maxDistance = 40;
		double safetyDistance = 15;

		DroneGUI droneGUI = (DroneGUI) gui;
		Random r = new Random(randomSeed);

		ArrayList<String> selectedAddresses = getSelectedAddresses();

		int robots = selectedAddresses.size();
		// TODO
		// System.out.println("DEBUG DEBUG DEBUG COMMANDPANEL");
		// robots = 10;

		if (robots == 0) {
			JOptionPane.showMessageDialog(null, "No robots selected!");
			return;
		}

		GeoFence fence = null;

		ArrayList<Waypoint> chosenWPs = new ArrayList<Waypoint>();

		for (Entity e : mapEntities) {
			if (e instanceof GeoFence) {
				fence = (GeoFence) e;
				break;
			}
		}

		int special = 0;

		double width = 63;
		double height = 63;

		if (!autoDeployArea.getText().isEmpty()) {

			Scanner s = new Scanner(autoDeployArea.getText());
			s.useDelimiter(";");

			try {

				while (s.hasNext()) {
					String token = s.next();
					if (token.equals("SEED")) {
						token = s.next();
						randomSeed = Integer.parseInt(token);
						r = new Random(randomSeed);
					}
					if (token.equals("SIZE")) {
						token = s.next();
						width = Double.parseDouble(token);
						height = width;
					}
					if (token.equals("SAFETY")) {
						token = s.next();
						safetyDistance = Double.parseDouble(token);
					}
					if (token.equals("SPECIAL")) {
						special = Integer.parseInt(s.next());
					}

				}

				s.close();

			} catch (Exception e) {
				s.close();
				JOptionPane.showMessageDialog(null, e.getMessage());
				return;
			}
		}

		if (fence != null) {

			droneGUI.getMapPanel().clearWaypoints();

			LinkedList<Waypoint> wps = fence.getWaypoints();

			Vector2d center = null;

			if (wps.size() >= 4) {

				center = new Vector2d();

				Vector2d corner = CoordinateUtilities.GPSToCartesian(wps.getFirst().getLatLon());
				center.x = corner.x;
				center.y = corner.y;

				for (int i = 1; i < wps.size(); i++) {
					corner = CoordinateUtilities.GPSToCartesian(wps.get(i).getLatLon());
					center.x += corner.x;
					center.y += corner.y;
				}
				center.x /= wps.size();
				center.y /= wps.size();

			}

			if (center == null && wps.size() != 1) {
				JOptionPane.showMessageDialog(null, "Incorrect number of GeoFence corners!");
			}

			if (center == null)
				center = CoordinateUtilities.GPSToCartesian(wps.get(0).getLatLon());

			if (special == 0) {

				LatLon tl = CoordinateUtilities
						.cartesianToGPS(new Vector2d(center.x - width / 2, center.y + height / 2));
				LatLon tr = CoordinateUtilities
						.cartesianToGPS(new Vector2d(center.x + width / 2, center.y + height / 2));
				LatLon bl = CoordinateUtilities
						.cartesianToGPS(new Vector2d(center.x - width / 2, center.y - height / 2));
				LatLon br = CoordinateUtilities
						.cartesianToGPS(new Vector2d(center.x + width / 2, center.y - height / 2));

				Vector2d tlv = CoordinateUtilities.GPSToCartesian(tl);
				Vector2d brv = CoordinateUtilities.GPSToCartesian(br);

				tlv.x -= 15;
				tlv.y += 15;

				brv.x += 15;
				brv.y -= 15;

				// System.out.println(CoordinateUtilities.cartesianToGPS(tlv));
				// System.out.println(CoordinateUtilities.cartesianToGPS(brv));

				// System.out.println(tl.getLat()+" "+tl.getLon());
				// System.out.println(br.getLat()+" "+br.getLon());

				fence = new GeoFence("geofence");
				fence.addWaypoint(tl);
				fence.addWaypoint(tr);
				fence.addWaypoint(br);
				fence.addWaypoint(bl);

			} else {
				int i = 0;
				if (special == 1) {

					fence = new GeoFence("geofence");

					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					center.x += 166.7;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					center.y -= 60;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					center.x -= 166.7;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));

				} else if (special == 2) {
					fence = new GeoFence("geofence");

					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					center.x += 115.47;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					// center.y-=115.47;
					center.y -= 57.74;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					center.x -= 57.74;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					center.y -= 57.74;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
					center.x -= 57.73;
					fence.addWaypoint(new Waypoint("wp" + (i++), CoordinateUtilities.cartesianToGPS(center)));
				}
			}

			wps = fence.getWaypoints();

			droneGUI.getMapPanel().clearGeoFence();
			droneGUI.getMapPanel().addGeoFence(fence);

			Vector2d min = new Vector2d(Double.MAX_VALUE, Double.MAX_VALUE);
			Vector2d max = new Vector2d(-Double.MAX_VALUE, -Double.MAX_VALUE);

			for (Waypoint wp : fence.getWaypoints()) {
				Vector2d v = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
				min.x = Math.min(min.x, v.x);
				min.y = Math.min(min.y, v.y);
				max.x = Math.max(max.x, v.x);
				max.y = Math.max(max.y, v.y);
			}

			ArrayList<Line> lines = new ArrayList<Line>();
			LinkedList<Waypoint> fenceWPs = fence.getWaypoints();
			for (int i = 1; i < fenceWPs.size(); i++) {
				Waypoint wa = fenceWPs.get(i - 1);
				Waypoint wb = fenceWPs.get(i);
				lines.add(getLine(wa, wb));
			}
			// loop around
			Waypoint wa = fenceWPs.get(fenceWPs.size() - 1);
			Waypoint wb = fenceWPs.get(0);
			lines.add(getLine(wa, wb));

			for (int i = 0; i < robots; i++) {

				Vector2d pos = null;

				int tries = 0;

				do {
					double x = min.x + r.nextDouble() * (max.x - min.x);
					double y = min.y + r.nextDouble() * (max.y - min.y);
					pos = new Vector2d(x, y);

					if (tries++ > 100) {
						JOptionPane.showMessageDialog(null, "Can't place the waypoints inside the GeoFence!");
						return;
					}

				} while (!safePosition(pos, chosenWPs, lines, safetyDistance, maxDistance));

				if (i >= chosenWPs.size()) {
					Waypoint w = new Waypoint("wp" + i, CoordinateUtilities.cartesianToGPS(pos));
					chosenWPs.add(w);
				}

			}

			for (Waypoint w : chosenWPs)
				droneGUI.getMapPanel().addWaypoint(w);

			String str = "GEOFENCE;";

			str += "SEED;" + randomSeed;

			str += ";SIZE;" + (int) width;
			str += ";SAFETY;" + (int) safetyDistance;

			if (special > 0) {
				str = "SPECIAL;" + special + ";SEED;" + randomSeed;
			}

			console.log(str);
			autoDeployArea.setText(str);

			deployEntities(true);
			deployPreset("waypoint", presetsConfig.get("waypoint"));

		} else {
			JOptionPane.showMessageDialog(null, "Please define a GeoFence first!");
			return;
		}
	}

	private Line getLine(Waypoint wa, Waypoint wb) {
		Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());
		return new Line(va.getX(), va.getY(), vb.getX(), vb.getY());
	}

	private boolean safePosition(Vector2d v, ArrayList<Waypoint> wps, ArrayList<Line> lines, double safetyDistance,
			double maxDistance) {

		if (insideBoundary(v, lines)) {

			double min = Double.MAX_VALUE;

			for (Waypoint wp : wps) {

				double distance = CoordinateUtilities.GPSToCartesian(wp.getLatLon()).distanceTo(v);

				min = Math.min(distance, min);

				if (distance < safetyDistance)
					return false;
			}
			return min < maxDistance || wps.isEmpty();
		}
		return false;
	}

	private boolean insideBoundary(Vector2d wp, ArrayList<Line> lines) {
		// http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;

		for (Line l : lines) {
			if (l.intersectsWithLineSegment(wp, new Vector2d(0, -Integer.MAX_VALUE)) != null)
				count++;
		}
		return count % 2 != 0;
	}

	public class Timer extends Thread {

		DateTime startTime = null;
		boolean enabled = false;

		public void startTimer() {
			startTime = new DateTime();
			enabled = true;
		}

		public void stopTimer() {
			startTime = null;
			enabled = false;
		}

		@Override
		public void run() {
			while (true) {

				DateTime temp = startTime;

				if (enabled && temp != null) {
					long elapsed = new DateTime().getMillis() - startTime.getMillis();
					timerLabel.setText("Time Elapsed: " + (elapsed / 1000));
				} else {
					timerLabel.setText("Time Elapsed: ");
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public Timer getTimer() {
		return timer;
	}
}