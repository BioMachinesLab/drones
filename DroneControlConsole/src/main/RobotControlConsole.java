package main;

import gamepad.GamePad;
import gui.DroneGUI;
import gui.RobotGUI;

import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import network.InformationConnection;
import network.MotorConnection;
import network.MotorMessageSender;
import threads.UpdateThread;
import commoninterface.network.MessageHandler;
import commoninterface.network.messages.Message;
import dataObjects.ConsoleMotorSpeeds;

public abstract class RobotControlConsole {

	protected RobotGUI gui;

	private GamePad gamePad;

	protected ConsoleMotorSpeeds motorSpeeds;
	protected MotorMessageSender motorMessageSender;
	protected MessageHandler messageHandler;

	protected InformationConnection informationConnection;
	protected MotorConnection motorConnection;

	protected ArrayList<UpdateThread> updateThreads = new ArrayList<UpdateThread>();

	public void setupGamepad() {
		try {
			gamePad = new GamePad(this);
			gamePad.start();
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}

	public synchronized void connect(String address) {
		try {
			gui.getMessagesPanel().clear();

			informationConnection = new InformationConnection(this,
					InetAddress.getByName(address));
			motorConnection = new MotorConnection(this,
					InetAddress.getByName(address));
			motorMessageSender = new MotorMessageSender(motorConnection,
					motorSpeeds);

			informationConnection.start();
			motorConnection.start();
			motorMessageSender.start();

			createUpdateThreads();

			gui.getConnectionPanel().setDroneConnected(true);
			gui.getConnectionPanel().connectionOK(address);
		} catch (Exception | Error e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

	}

	public synchronized void disconnect() {

		for (UpdateThread ut : updateThreads)
			ut.stopExecuting();

		updateThreads.clear();

		if (informationConnection != null) {
			informationConnection.closeConnection();
			informationConnection = null;
		}

		if (motorConnection != null) {
			motorConnection.closeConnection();
			motorConnection = null;
		}

		if (motorMessageSender != null) {
			motorMessageSender.stopExecuting();
			motorMessageSender = null;
		}

		gui.getConnectionPanel().disconnected();
		
		if(gui instanceof DroneGUI)
			((DroneGUI) gui).getGPSPanel().clearPanelInformation();
		
		gui.getConnectionPanel().setDroneConnected(false);
	}

	public void processMessage(Message message) {
		if (messageHandler != null)
			messageHandler.addMessage(message, null);
	}

	public ConsoleMotorSpeeds getMotorSpeeds() {
		return motorSpeeds;
	}

	public GamePad getGamePad() {
		return gamePad;
	}

	public void sendData(Message message) {
		if (informationConnection != null)
			informationConnection.sendData(message);
	}

	public RobotGUI getGUI() {
		return gui;
	}

	protected abstract RobotGUI setupGUI();

	public abstract void createUpdateThreads();

	protected void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				disconnect();
			}
		});
	}
	
	public void log(String s) {
		System.err.println("Logger not implemented!");
	}
}
