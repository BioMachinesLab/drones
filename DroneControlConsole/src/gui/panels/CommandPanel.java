package gui.panels;

import gui.DroneGUI;
import gui.RobotGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.DroneControlConsole;
import main.RobotControlConsole;
import network.CommandSender;
import threads.UpdateThread;
import commoninterface.CIBehavior;
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
import commoninterface.utils.ClassLoadHelper;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import commoninterface.utils.jcoord.LatLon;

public class CommandPanel extends UpdatePanel {
	private static final long serialVersionUID = 4038133860317693008L;
	
	private static String CONTROLLERS_FOLDER = "controllers";

	private String myHostname = "";

	private UpdateThread thread;
	private JLabel statusMessage;
	private BehaviorMessage currentMessage;
	private JTextArea config;
	private JTextField logMessage;
	private JTextArea selectedDrones;
	private JTextArea autoDeployArea;
	public JButton sendLog;
	private RobotGUI gui;
	private RobotControlConsole console;
	private boolean dronePanel = false;
	private JFrame neuralActivationsWindow;
	public JButton deploy;
	public JButton stopAll;
	public JButton entitiesButton;
	private JComboBox<String> behaviors;
	private JComboBox<String> controllers;

	private ArrayList<String> availableBehaviors = new ArrayList<String>();
	private ArrayList<String> availableControllers = new ArrayList<String>();

	private JPanel presetsPanel = new JPanel();
	private HashMap<String, String> presetsConfig = new HashMap<String, String>();
	private JCheckBox autoDeployCheckBox;
	
	private ArrayList<Entity> mapEntities = new ArrayList<Entity>();
	
	/*
	 * author: @miguelduarte42 This has to be this way because some behaviors
	 * are specific to the RaspberryController, and this project cannot include
	 * RaspberryController because of PI4J.
	 */
	private String[] hardcodedClasses = new String[] { "CalibrationCIBehavior", "ShutdownCIBehavior", "LogDronesCIBehavior" };

	public CommandPanel(RobotControlConsole console, RobotGUI gui) {
		updateHostname();

		this.console = console;

		this.gui = gui;

		if (gui instanceof DroneGUI)
			dronePanel = true;
		
		initNeuralActivationsWindow();

		setBorder(BorderFactory.createTitledBorder("Commands"));

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel controllersPanel = new JPanel(new BorderLayout());
		JPanel comboBoxes = new JPanel(new BorderLayout());

		behaviors = new JComboBox<String>();
		behaviors.setPreferredSize(new Dimension(20, 20));
		populateBehaviors(behaviors);
		comboBoxes.add(behaviors, BorderLayout.NORTH);

		controllers = new JComboBox<String>();
		controllers.setPreferredSize(new Dimension(20, 20));
		populateControllers(controllers);
		comboBoxes.add(controllers, BorderLayout.SOUTH);

		controllers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadController((String) controllers.getSelectedItem());
			}
		});
		
		JLabel selectedDronesLabel = new JLabel("Selected drones");
		selectedDronesLabel.setHorizontalAlignment(JLabel.HORIZONTAL);
		selectedDrones = new JTextArea(2,30);
		selectedDrones.setLineWrap(true);
		JScrollPane selectDronesScroll = new JScrollPane(selectedDrones);
		
		autoDeployArea = new JTextArea(2,30);
		autoDeployArea.setLineWrap(true);
		JScrollPane autoDeployAreaScroll = new JScrollPane(autoDeployArea);
		
		presetsPanel.setBorder(BorderFactory.createTitledBorder("Controllers"));
		
		controllersPanel.add(presetsPanel, BorderLayout.SOUTH);
//		controllersPanel.add(comboBoxes, BorderLayout.SOUTH);
		topPanel.add(controllersPanel, BorderLayout.NORTH);
		
		JPanel selectedPanel = new JPanel();
		selectedPanel.add(selectDronesScroll);
		
		selectedPanel.setBorder(BorderFactory.createTitledBorder("Selection"));
		
		controllersPanel.add(selectedPanel, BorderLayout.NORTH);

		deploy = new JButton("Deploy");
		stopAll = new JButton("Stop All");

		deploy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployBehavior((String) behaviors.getSelectedItem(), true);
			}
		});

		stopAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployBehavior("ControllerCIBehavior", false);
			}
		});

		config = new JTextArea(7, 8);
		config.setFont(new Font("Monospaced", Font.PLAIN, 11));
		JScrollPane scroll = new JScrollPane(config);

		statusMessage = new JLabel("");
		statusMessage.setPreferredSize(new Dimension(10, 20));

		JPanel actionsPanel = new JPanel(new GridLayout(2, 2));
		
		if (dronePanel) {

			entitiesButton = new JButton("Deploy Entities");

			actionsPanel.add(entitiesButton);

			entitiesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deployEntities();
				}
			});
			
			JPanel autoDeployPanel = new JPanel(new GridLayout(1,2));
			JLabel autoDeployLabel = new JLabel("Auto Deploy");
			autoDeployCheckBox = new JCheckBox();
			
			autoDeployPanel.add(autoDeployLabel);
			autoDeployPanel.add(autoDeployCheckBox);
//			buttons.add(autoDeployPanel);
		}
		
		actionsPanel.add(stopAll);
		
		JButton placeDronesButton = new JButton("Place Drones Randomly");
		placeDronesButton.addActionListener(new ActionListener() {
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
				Object[] options = { "Yes" ,"No"};
				int result = JOptionPane.showOptionDialog(null, "Start calibration ?", "Calibration", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				
				if(result == 0) {
					statusMessage("CalibrationCIBehavior", true);
				}else if(result == 1){
					//TODO: revert to old calibration
				}
			}
		});

		actionsPanel.add(placeDronesButton);
		actionsPanel.add(calibrateButton);
//		actionsPanel.add(plotButton);
		
		actionsPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
		
		add(topPanel, BorderLayout.NORTH);
		add(actionsPanel, BorderLayout.CENTER);
		
		JPanel deploy = new JPanel(new BorderLayout());
		deploy.setBorder(BorderFactory.createTitledBorder("Drone Deploy Area"));
		deploy.add(autoDeployAreaScroll, BorderLayout.CENTER);
		deploy.add(statusMessage, BorderLayout.SOUTH);
		add(deploy, BorderLayout.SOUTH);
	}

	private void initNeuralActivationsWindow() {
		neuralActivationsWindow = new JFrame("Neural Network Activations");
		neuralActivationsWindow.setSize(950, 600);
		neuralActivationsWindow.setLocationRelativeTo(gui);
		neuralActivationsWindow
				.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private synchronized void statusMessage(String className, boolean status) {
		setText("");
		
		CIArguments translatedArgs = new CIArguments(config.getText()
				.replaceAll("\\s+", ""), true);

		if (status)
			currentMessage = new BehaviorMessage(className,
					translatedArgs.getCompleteArgumentString(), status,
					myHostname);
		else
			currentMessage = new BehaviorMessage(className, "", status,
					myHostname);

		notifyAll();
	}

	private void deployBehavior(String className, boolean status) {
		CIArguments translatedArgs = new CIArguments(config.getText()
				.replaceAll("\\s+", ""), true);
		BehaviorMessage m;

		if (status)
			m = new BehaviorMessage(className,
					translatedArgs.getCompleteArgumentString(), status,
					myHostname);
		else
			m = new BehaviorMessage(className, "", status, myHostname);

		deploy(m);
	}
	
	private void deployPreset(String arguments){
		CIArguments translatedArgs = new CIArguments(arguments.replaceAll("\\s+", ""), true);
		String type = translatedArgs.getArgumentAsString("type");
		
		if(type != null){
			BehaviorMessage m = new BehaviorMessage(type,translatedArgs.getCompleteArgumentString(), true, myHostname);
			deploy(m);
		}else
			JOptionPane.showMessageDialog(null, "Contoller type not defined on preset configuration file!");
		
	}
	private void deployEntities() {
		deployEntities(false);
	}

	private void deployEntities(boolean dynamicActiveId) {
		EntitiesMessage m = new EntitiesMessage(mapEntities, myHostname);
		deploy(m,dynamicActiveId);

		//This part is for other DroneControlConsoles to receive the updated entities
		EntitiesBroadcastMessage msg = new EntitiesBroadcastMessage(mapEntities);
		if (console instanceof DroneControlConsole) {
			// Messages get lost sometimes!!!
			for (int j = 0; j < 5; j++) {
				(((DroneControlConsole) console).getConsoleBroadcastHandler())
						.sendMessage(msg.encode()[0]);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	private synchronized void deploy(Message m) {
		deploy(m, false);
	}
	
	private synchronized void deploy(Message m, boolean dynamicIds) {
		setText("Deploying...");
		
		ArrayList<String> addresses = getSelectedAddresses();
		
		console.log("Deploying "+m+";ADDRESSES;"+addresses.toString().replace(", ", ";").replace("[", "").replace("]", ""));
		new CommandSender(m, addresses, this, dynamicIds).start();
	}
	
	private ArrayList<String> getSelectedAddresses() {

		String[] addresses = gui.getConnectionPanel().getCurrentAddresses();
		
		ArrayList<String> selectedAddresses = new ArrayList<String>();
		
		for(String s : addresses) {
			if(s != null && isSelectedAddress(s)) {
				selectedAddresses.add(s);
			}
		}
		
		return selectedAddresses;
	}
	
	public boolean isSelectedAddress(String s) {
		
		String str = selectedDrones.getText();
		
		if(str.trim().isEmpty())
			return true;
		
		if(str.contains("-")) {
			String[] list = str.split("-");
			int first = Integer.parseInt(list[0]);
			int last = Integer.parseInt(list[1]);
			
			for(int i = first ; i <= last ; i++) {
				if(s.endsWith("."+i))
					return true;
			}
			
		}
		
		if(str.contains(",")) {
			String[] list = str.split(",");
			
			for(String sList : list) {
				if(s.endsWith("."+sList))
					return true;
			}
		}
		
		return s.endsWith("."+str);
	}

	public void setText(String text) {
		statusMessage.setText(text);
	}

	private void populateBehaviors(JComboBox<String> list) {
		ArrayList<Class<?>> classes = ClassLoadHelper
				.findRelatedClasses(CIBehavior.class);
		for (Class<?> c : classes) {
			list.addItem(c.getSimpleName());
			availableBehaviors.add(c.getSimpleName());
		}

		for (String s : hardcodedClasses) {
			list.addItem(s);
			availableBehaviors.add(s);
		}
	}

	private void populateControllers(JComboBox<String> list) {
		list.addItem("");
		
		File controllersFolder = new File(CONTROLLERS_FOLDER);

		if (controllersFolder.exists() && controllersFolder.isDirectory()) {
			
			int numberOfPreset = 0;
			for (String s : controllersFolder.list()) {
				if (s.endsWith(".conf")){
					if(s.startsWith("preset")){
						numberOfPreset++;
					}
				}
			}
			
			if(numberOfPreset > 2){
				int rows = (int)Math.ceil(numberOfPreset/2.0);
				
				presetsPanel.setLayout(new GridLayout(rows,2));
			}else
				presetsPanel.setLayout(new GridLayout(1, 2));
			
			for (String s : controllersFolder.list()) {
				if (s.endsWith(".conf")){
					if(s.startsWith("preset")){
						String buttonName = s.split("\\.")[0].replace("preset_", "");
						JButton b = new JButton(buttonName);
						
						b.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								JButton b = (JButton) e.getSource();
								String arguments = presetsConfig.get(b.getText());
								deployPreset(arguments);
							}
						});
						
						presetsPanel.add(b);
						
						try {
							String config = readConfigurationFile(new File(CONTROLLERS_FOLDER + "/" + s));
							presetsConfig.put(buttonName, config);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}else{
						list.addItem(s);
					}
				}
				availableControllers.add(s);
			}
		}

	}

	private String readConfigurationFile(File file) throws FileNotFoundException {
		String result = "";
		Scanner scanner = new Scanner(file);
		
		while(scanner.hasNext())
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

	private void loadController(String file) {

		if (!file.isEmpty()) {
			File f = new File(CONTROLLERS_FOLDER + "/" + file);

			if (f.exists()) {
				Scanner s = null;
				try {
					s = new Scanner(f);
					String result = "";

					while (s.hasNextLine())
						result += s.nextLine() + "\n";

					config.setText(result);
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (s != null)
						s.close();
				}
			}
		} else {
			config.setText("");
		}
	}

	public JTextArea getSelectedDronesTextField() {
		return selectedDrones;
	}
	
	public ArrayList<String> getAvailableBehaviors() {
		return availableBehaviors;
	}

	public ArrayList<String> getAvailableControllers() {
		return availableControllers;
	}
	
	public void updateMapInfo(ArrayList<Entity> updatedEntities){
		mapEntities.clear();
		mapEntities.addAll(updatedEntities);
		
		if(autoDeployCheckBox.isSelected())
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

	public void setSeletedJComboBoxBehavior(String str) {
		for (int i = 0; i < behaviors.getItemCount(); i++) {
			if (behaviors.getItemAt(i).contains(str)) {
				behaviors.setSelectedIndex(i);
				break;
			}
		}
	}

	public void setSeletedJComboBoxConfigurationFile(String str) {
		for (int i = 0; i < controllers.getItemCount(); i++) {
			if (controllers.getItemAt(i).contains(str)) {
				controllers.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public void placeDronesRandomly() {
		
		if(presetsConfig.get("waypoint") == null) {
			JOptionPane.showMessageDialog(null, "There is no default Waypoint controller defined! Create a file called \"preset_waypoint.conf\" in the \"controllers\" folder.");
			return;
		}
		
		int randomSeed = (int)(Math.random()*1000.0);
		double safetyDistance = 3;
		DroneGUI droneGUI = (DroneGUI)gui;
		Random r = new Random(randomSeed);
		
		ArrayList<String> selectedAddresses = getSelectedAddresses();
	
		int robots = selectedAddresses.size();
		//TODO
		robots = 10;
		
		if(robots == 0) {
			JOptionPane.showMessageDialog(null, "No robots selected!");
			return;
		}
		
		GeoFence fence = null;
		
		ArrayList<Waypoint> chosenWPs = new ArrayList<Waypoint>();
		
		for(Entity e : mapEntities) {
			if(e instanceof GeoFence) {
				fence = (GeoFence)e;
				break;
			}
		}
		
		double width = 100;
		double height = 100;
		
		if(!autoDeployArea.getText().isEmpty()) {
			
			Scanner s = new Scanner(autoDeployArea.getText());
			s.useDelimiter(";");
			
			try {
			
				while(s.hasNext()) {
					String token = s.next();
					if(token.equals("SEED")) {
						token = s.next();
						randomSeed = Integer.parseInt(token);
						r = new Random(randomSeed);
					}
					if(token.equals("WIDTH")) {
						token = s.next();
						width = Double.parseDouble(token);
					}
					if(token.equals("HEIGHT")) {
						token = s.next();
						height = Double.parseDouble(token);
					}
					if(token.equals("SAFETY")) {
						token = s.next();
						safetyDistance = Double.parseDouble(token);
					}
					
				}
				
				s.close();
			
			} catch(Exception e) {
				s.close();
				JOptionPane.showMessageDialog(null, e.getMessage());
				return;
			}
		}
		
		if(fence != null) {
			
			droneGUI.getMapPanel().clearWaypoints();
			
			LinkedList<Waypoint> wps = fence.getWaypoints();
			
			if(wps.size() == 1) {
				
				Vector2d center = CoordinateUtilities.GPSToCartesian(wps.get(0).getLatLon());
				
				LatLon tl = CoordinateUtilities.cartesianToGPS(new Vector2d(center.x-width/2,center.y+height/2));
				LatLon tr = CoordinateUtilities.cartesianToGPS(new Vector2d(center.x+width/2,center.y+height/2));
				LatLon bl = CoordinateUtilities.cartesianToGPS(new Vector2d(center.x-width/2,center.y-height/2));
				LatLon br = CoordinateUtilities.cartesianToGPS(new Vector2d(center.x+width/2,center.y-height/2));
				
				fence = new GeoFence("geofence");
				fence.addWaypoint(tl);
				fence.addWaypoint(tr);
				fence.addWaypoint(br);
				fence.addWaypoint(bl);
				
				wps = fence.getWaypoints();
				
				droneGUI.getMapPanel().clearGeoFence();
				droneGUI.getMapPanel().addGeoFence(fence);
				
			} else if(wps.size() > 4) {
				JOptionPane.showMessageDialog(null, "The GeoFence should have a maximum of 4 points!");
				return;
			}
			
			Vector2d min = new Vector2d(Double.MAX_VALUE, Double.MAX_VALUE);
			Vector2d max = new Vector2d(-Double.MAX_VALUE, -Double.MAX_VALUE);
			
			for(Waypoint wp : fence.getWaypoints()) {
				Vector2d v = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
				min.x = Math.min(min.x, v.x);
				min.y = Math.min(min.y, v.y);
				max.x = Math.max(max.x, v.x);
				max.y = Math.max(max.y, v.y);
			}
			
			ArrayList<Line> lines = new ArrayList<Line>();
			LinkedList<Waypoint> fenceWPs = fence.getWaypoints();
			for(int i = 1 ; i < fenceWPs.size() ; i++) {
				Waypoint wa = fenceWPs.get(i-1);
				Waypoint wb = fenceWPs.get(i);
				lines.add(getLine(wa,wb));
			}
			//loop around
			Waypoint wa = fenceWPs.get(fenceWPs.size()-1);
			Waypoint wb = fenceWPs.get(0);
			lines.add(getLine(wa,wb));
			
			for(int i = 0 ; i < robots ; i++) {
				
				Vector2d pos = null;
				
				int tries = 0;
				
				do {
					double x = min.x + r.nextDouble()*(max.x-min.x);
					double y = min.y + r.nextDouble()*(max.y-min.y);
					pos = new Vector2d(x,y);
					
					if(tries++ > 100) {
						JOptionPane.showMessageDialog(null, "Can't place the waypoints inside the GeoFence!");
						return;
					}
					
				} while(!safePosition(pos, chosenWPs, lines, safetyDistance));
				
				if(i >= chosenWPs.size()) {
					Waypoint w = new Waypoint("wp"+i, CoordinateUtilities.cartesianToGPS(pos));
					chosenWPs.add(w);
				}
				
			}
			
			for(Waypoint w : chosenWPs)
				droneGUI.getMapPanel().addWaypoint(w);
			
			System.out.println(wps.size());
			
			String str = "GEOFENCE AUTO DISPERSE;";
			
			str+="SEED;"+randomSeed;
			
			str+=";WIDTH;"+width;
			str+=";HEIGHT;"+height;
			str+=";SAFETY;"+safetyDistance;
			
			console.log(str);
			autoDeployArea.setText(str);
			
			deployEntities(true);
			deployPreset(presetsConfig.get("waypoint"));
			
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
	
	private boolean safePosition(Vector2d v, ArrayList<Waypoint> wps, ArrayList<Line> lines, double safetyDistance) {
		
		if(insideBoundary(v, lines)) {
		
			for(Waypoint wp : wps) {
				if(CoordinateUtilities.GPSToCartesian(wp.getLatLon()).distanceTo(v) < safetyDistance)
					return false;
			}
			
			return true;
		}
		return false;
	}
	
	private boolean insideBoundary(Vector2d wp, ArrayList<Line> lines) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		
		for(Line l : lines) {
			if(l.intersectsWithLineSegment(wp, new Vector2d(0,-Integer.MAX_VALUE)) != null)
				count++;
		}
		return count % 2 != 0;
	}
}