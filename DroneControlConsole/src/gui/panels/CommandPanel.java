package gui.panels;

import gui.DroneGUI;
import gui.RobotGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import network.CommandSender;
import network.messages.BehaviorMessage;
import network.messages.EntitiesMessage;
import network.messages.LogMessage;
import network.messages.Message;
import threads.UpdateThread;
import utils.ClassLoadHelper;
import commoninterface.CIBehavior;
import commoninterface.objects.Entity;
import commoninterface.utils.CIArguments;

public class CommandPanel extends UpdatePanel{
	
	private UpdateThread thread;
	private JLabel statusMessage;
	private BehaviorMessage currentMessage;
	private JTextArea config;
	private RobotGUI gui; 
	private boolean dronePanel = false;
	
	private JFrame neuralActivationsWindow;
	
	public CommandPanel(RobotGUI gui) {
		
		if(gui instanceof DroneGUI)
			dronePanel = true;
		
		this.gui = gui;
		
		initNeuralActivationsWindow();
		
		setBorder(BorderFactory.createTitledBorder("Commands"));
		
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		
		topPanel.setLayout(new BorderLayout());
		
		JComboBox<Class<CIBehavior>> behaviors = new JComboBox<>();
		behaviors.setPreferredSize(new Dimension(20,20));
		
		populateBehaviors(behaviors);
		
		topPanel.add(behaviors, BorderLayout.NORTH);
		
		JButton start = new JButton("Start");
		JButton stop = new JButton("Stop");
		JButton deploy = new JButton("Deploy");
		JButton stopAll = new JButton("Stop All");
		
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((Class<CIBehavior>)behaviors.getSelectedItem(), true);
			}
		});
		
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessage((Class<CIBehavior>)behaviors.getSelectedItem(), false);
			}
		});
		
		deploy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployBehavior((Class<CIBehavior>)behaviors.getSelectedItem(), true);
			}
		});
		
		stopAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployBehavior((Class<CIBehavior>)behaviors.getSelectedItem(), false);
			}
		});
		
		JTextField logMessage = new JTextField();
		JButton sendLog = new JButton("Send Log");
		
		sendLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deployLog(logMessage.getText().trim());
			}
		});
		
		JPanel buttons = new JPanel(new GridLayout(dronePanel ? 4 : 3,2));
		buttons.add(start);
		buttons.add(stop);
		buttons.add(deploy);
		buttons.add(stopAll);
		buttons.add(logMessage);
		buttons.add(sendLog);
		
		if(dronePanel) {
			
			JButton entitiesButton = new JButton("Deploy Entities");
			
			buttons.add(entitiesButton);
			
			entitiesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deployEntities();
				}
			});
		}
		
		topPanel.add(buttons, BorderLayout.SOUTH);
		
		config = new JTextArea(10,10);
		JScrollPane scroll = new JScrollPane(config);
		
		topPanel.add(scroll,BorderLayout.CENTER);
		
		statusMessage = new JLabel("");
		statusMessage.setPreferredSize(new Dimension(10,20));
		
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
		neuralActivationsWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private synchronized void statusMessage(Class<CIBehavior> className, boolean status) {
		setText("");
		
		CIArguments translatedArgs = new CIArguments(config.getText().replaceAll("\\s+",""),true);
		
		if(status)
			currentMessage = new BehaviorMessage(className, translatedArgs.getCompleteArgumentString(), status);
		else
			currentMessage = new BehaviorMessage(className, "", status);
		
		notifyAll();
	}
	
	private void deployBehavior(Class<CIBehavior> className, boolean status) {
		CIArguments translatedArgs = new CIArguments(config.getText().replaceAll("\\s+",""),true);
		BehaviorMessage m;
		
		if(status)
			m = new BehaviorMessage(className, translatedArgs.getCompleteArgumentString(), status);
		else
			m = new BehaviorMessage(className, "", status);
		
		deploy(m);
	}
	
	private void deployEntities() {
		DroneGUI droneGUI = (DroneGUI)gui;
		LinkedList<Entity> entities = droneGUI.getMapPanel().getEntities();
		EntitiesMessage m = new EntitiesMessage(entities);
		deploy(m);
	}
	
	private void deployLog(String msg) {
		LogMessage m = new LogMessage(msg);
		deploy(m);
	}
	
	private synchronized void deploy(Message m) {
		setText("Deploying...");
		
		new CommandSender(m, gui.getConnectionPanel().getCurrentAddresses(), this).start();
	}
	
	public void setText(String text) {
		statusMessage.setText(text);
	}
	
	private void populateBehaviors(JComboBox<Class<CIBehavior>> list) {
		ArrayList<Class<?>> classes = ClassLoadHelper.findRelatedClasses(CIBehavior.class);
		for(Class<?> c : classes) {
			list.addItem((Class<CIBehavior>)c);
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
		while(currentMessage == null) {
			try {
				wait();
			} catch(Exception e){}
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
		String result = message.getSelectedBehavior().getSimpleName()+": ";
		result+= message.getSelectedStatus() ? "start" : "stop";
		
		setText(result);
	}
}
