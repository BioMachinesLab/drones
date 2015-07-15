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
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.Waypoint;
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.EntitiesBroadcastMessage;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.LogMessage;
import commoninterface.network.messages.Message;
import commoninterface.utils.CIArguments;
import commoninterface.utils.ClassLoadHelper;

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
	public JButton sendLog;
	private RobotGUI gui;
	private RobotControlConsole console;
	private boolean dronePanel = false;
	private JFrame neuralActivationsWindow;
	public JButton start;
	public JButton stop;
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

		controllersPanel.add(presetsPanel, BorderLayout.NORTH);
		controllersPanel.add(comboBoxes, BorderLayout.SOUTH);
		
		topPanel.add(controllersPanel, BorderLayout.NORTH);

		start = new JButton("Start");
		stop = new JButton("Stop");
		JLabel selectedDronesLabel = new JLabel("Selected drones");
		selectedDronesLabel.setHorizontalAlignment(JLabel.HORIZONTAL);
		selectedDrones = new JTextArea(1,5);
		selectedDrones.setLineWrap(true);
		JScrollPane selectDronesScroll = new JScrollPane(selectedDrones);
		deploy = new JButton("Deploy");
		stopAll = new JButton("Stop All");

		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((String) behaviors.getSelectedItem(), true);
			}
		});

		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((String) behaviors.getSelectedItem(), false);
			}
		});

		deploy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployBehavior((String) behaviors.getSelectedItem(), true);
			}
		});

		stopAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployBehavior((String) behaviors.getSelectedItem(), false);
			}
		});

		logMessage = new JTextField();
		sendLog = new JButton("Send Log");

		sendLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployLog(logMessage.getText().trim());
			}
		});

		JPanel buttons = new JPanel(new GridLayout(dronePanel ? 5 : 4, 2));
		buttons.add(start);
		buttons.add(stop);
		buttons.add(selectDronesScroll);
		buttons.add(selectedDronesLabel);
		buttons.add(deploy);
		buttons.add(stopAll);
		buttons.add(logMessage);
		buttons.add(sendLog);

		if (dronePanel) {

			entitiesButton = new JButton("Deploy Entities");

			buttons.add(entitiesButton);

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
			buttons.add(autoDeployPanel);
		}

		topPanel.add(buttons, BorderLayout.SOUTH);

		config = new JTextArea(7, 8);
		config.setFont(new Font("Monospaced", Font.PLAIN, 11));
		JScrollPane scroll = new JScrollPane(config);

		topPanel.add(scroll, BorderLayout.CENTER);

		statusMessage = new JLabel("");
		statusMessage.setPreferredSize(new Dimension(10, 20));

		JPanel bigButtonsPanel = new JPanel(new GridLayout(2, 1));
		
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
				Object[] options = { "Yes" ,"No" , "Cancel"};
				int result = JOptionPane.showOptionDialog(null, "Start calibation ?", "Calibration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				
				if(result == 0)
					statusMessage("CalibrationCIBehavior", true);
				else if(result == 2){
					//TODO: revert to old calibration
				}
			}
		});

		bigButtonsPanel.add(calibrateButton);
		bigButtonsPanel.add(plotButton);
		
		add(topPanel, BorderLayout.NORTH);
		add(bigButtonsPanel);
		add(statusMessage, BorderLayout.SOUTH);
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
			JOptionPane.showMessageDialog(null, "Contoller type no defined on preset configuration file!");
		
	}

	private void deployEntities() {
		DroneGUI droneGUI = (DroneGUI) gui;
//		ArrayList<Entity> entities = droneGUI.getMapPanel().getEntities();
		EntitiesMessage m = new EntitiesMessage(mapEntities, myHostname);
		deploy(m);

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

	private void deployLog(String msg) {
		LogMessage m = new LogMessage(msg, myHostname);
		deploy(m);
	}

	private synchronized void deploy(Message m) {
		setText("Deploying...");
		
		String[] addresses = gui.getConnectionPanel().getCurrentAddresses();
		ArrayList<String> selectedAddresses = new ArrayList<String>();
		
		for(String s : addresses)
			if(selectedAddress(s)) {
				selectedAddresses.add(s);
			}

		new CommandSender(m, selectedAddresses, this).start();
	}
	
	public boolean selectedAddress(String s) {
		
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
						numberOfPreset ++;
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
	
}
