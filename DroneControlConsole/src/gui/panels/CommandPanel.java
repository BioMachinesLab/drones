package gui.panels;

import gui.DroneGUI;
import gui.RobotGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SecondaryLoop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.EntitiesBroadcastMessage;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.LogMessage;
import commoninterface.network.messages.Message;
import commoninterface.utils.CIArguments;
import commoninterface.utils.ClassLoadHelper;

public class CommandPanel extends UpdatePanel {

	private static String CONTROLLERS_FOLDER = "controllers";

	private String myHostname = "";

	private UpdateThread thread;
	private JLabel statusMessage;
	private BehaviorMessage currentMessage;
	private JTextArea config;
	private JTextField logMessage;
	private JButton sendLog;
	private RobotGUI gui;
	private RobotControlConsole console;
	private boolean dronePanel = false;
	private JFrame neuralActivationsWindow;

	private ArrayList<String> availableBehaviors = new ArrayList<String>();
	private ArrayList<String> availableControllers = new ArrayList<String>();

	/*
	 * author: @miguelduarte42 This has to be this way because some behaviors
	 * are specific to the RaspberryController, and this project cannot include
	 * RaspberryController because of PI4J.
	 */
	private String[] hardcodedClasses = new String[] { "CalibrationCIBehavior" };

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

		JPanel comboBoxes = new JPanel(new BorderLayout());

		JComboBox<String> behaviors = new JComboBox<String>();
		behaviors.setPreferredSize(new Dimension(20, 20));
		populateBehaviors(behaviors);
		comboBoxes.add(behaviors, BorderLayout.NORTH);

		JComboBox<String> controllers = new JComboBox<String>();
		controllers.setPreferredSize(new Dimension(20, 20));
		populateControllers(controllers);
		comboBoxes.add(controllers, BorderLayout.SOUTH);

		controllers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadController((String) controllers.getSelectedItem());
			}
		});

		topPanel.add(comboBoxes, BorderLayout.NORTH);

		JButton start = new JButton("Start");
		JButton stop = new JButton("Stop");
		JButton deploy = new JButton("Deploy");
		JButton stopAll = new JButton("Stop All");

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

		JPanel buttons = new JPanel(new GridLayout(dronePanel ? 4 : 3, 2));
		buttons.add(start);
		buttons.add(stop);
		buttons.add(deploy);
		buttons.add(stopAll);
		buttons.add(logMessage);
		buttons.add(sendLog);

		if (dronePanel) {

			JButton entitiesButton = new JButton("Deploy Entities");

			buttons.add(entitiesButton);

			entitiesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deployEntities();
				}
			});
		}

		topPanel.add(buttons, BorderLayout.SOUTH);

		config = new JTextArea(7, 8);
		config.setFont(new Font("Monospaced", Font.PLAIN, 11));
		JScrollPane scroll = new JScrollPane(config);

		topPanel.add(scroll, BorderLayout.CENTER);

		statusMessage = new JLabel("");
		statusMessage.setPreferredSize(new Dimension(10, 20));

		JButton plotButton = new JButton("Plot Neural Activations");
		plotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				neuralActivationsWindow.setVisible(true);
			}
		});

		add(topPanel, BorderLayout.NORTH);
		add(plotButton);
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

	private void deployEntities() {
		DroneGUI droneGUI = (DroneGUI) gui;
		ArrayList<Entity> entities = droneGUI.getMapPanel().getEntities();
		EntitiesMessage m = new EntitiesMessage(entities, myHostname);
		deploy(m);

		EntitiesBroadcastMessage msg = new EntitiesBroadcastMessage(entities);
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

		new CommandSender(m, gui.getConnectionPanel().getCurrentAddresses(),
				this).start();
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
			for (String s : controllersFolder.list()) {
				if (s.endsWith(".conf"))
					list.addItem(s);
				availableControllers.add(s);
			}
		}
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

	public ArrayList<String> getAvailableBehaviors() {
		return availableBehaviors;
	}

	public ArrayList<String> getAvailableControllers() {
		return availableControllers;
	}

	public void updateHostname() {
		myHostname = NetworkUtils.getHostname();
	}

	public void setLogText(String text) {
		logMessage.setText(text);
		sendLog.doClick();
	}

	public void setConfiguration(String configStr) {
		config.setText(configStr);
	}
}
