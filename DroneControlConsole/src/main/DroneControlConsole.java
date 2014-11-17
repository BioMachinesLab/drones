package main;

import gamepad.GamePad;
import gamepad.GamePad.GamePadType;
import gui.GUI;
import gui.IPandPortNumberRequestToUser;

import javax.swing.JOptionPane;

import network.InformationConnection;
import network.MotorConnection;
import network.MotorMessageSender;
import network.messages.CompassMessage;
import network.messages.GPSMessage;
import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;
import network.messages.Message;
import network.messages.SystemInformationsMessage;
import network.messages.SystemStatusMessage;
import threads.MotorUpdateThread;
import threads.UpdateThread;
import dataObjects.MotorSpeeds;

public class DroneControlConsole extends Thread {
	
	private GUI gui;
	
	// Connections Objects
	private InformationConnection informationConnection;
	private MotorConnection motorConnection;
	
	private GamePad gamePad;

	private MotorSpeeds motorSpeeds;
	private MotorMessageSender motorMessageSender;
	
	public DroneControlConsole() {
		/*Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (informationConnection != null) {
					if (motorsPanel != null) {
						motorSpeeds.setSpeeds(0, 0);
					}
					if (informationConnection != null) {
						informationConnection.closeConnection();
					}
					if (gpsThread.isAlive()) {
						gpsThread.interrupt();
					}

					if (messagesThread.isAlive()) {
						messagesThread.interrupt();
					}

					if (compassThread.isAlive()) {
						compassThread.interrupt();
					}
				}
			}
		});*/
	}
	
	@Override
	public void run() {
		
		motorSpeeds = new MotorSpeeds();
		
		connect();

		setupGUI();
		
		setupGamepad();
		
		if(informationConnection != null && motorConnection != null) {
			informationConnection.start();
			motorConnection.start();
			motorMessageSender.start();
		}
		
		gui.setVisible(true);
		
	}
	
	private void setupGamepad() {
		try {
			gamePad = new GamePad(this, GamePadType.GAMEPAD);
			gamePad.start();
		} catch(UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}
	
	private void setupGUI() {
		gui = new GUI();
		
		UpdateThread gpsThread = new UpdateThread(this, gui.getGPSPanel(), MessageType.GPS);
		UpdateThread messagesThread = new UpdateThread(this, gui.getMessagesPanel(), MessageType.SYSTEM_STATUS);
		UpdateThread motorsThread = new MotorUpdateThread(this, gui.getMotorsPanel());
		UpdateThread compassThread = new UpdateThread(this, gui.getCompassPanel(), MessageType.COMPASS);
//		UpdateThread infoThread = new UpdateThread(this, gui.getSysInfoPanel(), MessageType.SYSTEM_INFO);
		
		
		gpsThread.start();
		messagesThread.start();
		motorsThread.start();
		compassThread.start();
//		infoThread.run();
		
	}
	
	
	public void connect() {

		do {
			try {
				IPandPortNumberRequestToUser form = new IPandPortNumberRequestToUser();
				if (form.getIpAddress() == null) {
					continue;
				} else {

					informationConnection = new InformationConnection(this,
							form.getIpAddress());

					motorConnection = new MotorConnection(this,
							form.getIpAddress());
					

					motorMessageSender = new MotorMessageSender(
							motorConnection, motorSpeeds);

				}
			} catch (Exception | Error e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		} while (informationConnection == null);
	}
	
	public void processMessage(Message message) {

		if (message instanceof GPSMessage) {
			gui.getGPSPanel().displayData(((GPSMessage) message).getGPSData());
			gui.getMapPanel().displayData(((GPSMessage) message).getGPSData());
		} else if (message instanceof SystemInformationsMessage) {
			gui.getSysInfoPanel().displayData(((SystemInformationsMessage) message).getSysInformations());
		} else if (message instanceof SystemStatusMessage) {
			gui.getMessagesPanel().displayData((SystemStatusMessage) message);
		} else if (message instanceof CompassMessage) {
			gui.getCompassPanel().displayData((CompassMessage) message);
			gui.getMapPanel().displayData(((CompassMessage) message));
		} else {
			System.out.println("Received non recognise message type: " + message.getClass().toString());
		}
	}
	
	public MotorSpeeds getMotorSpeeds() {
		return motorSpeeds;
	}
	
	public GamePad getGamePad() {
		return gamePad;
	}
	
	public void sendData(Message message) {
		if(informationConnection != null)
			informationConnection.sendData(message);
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public static void main(String[] args) {
		new DroneControlConsole().start();
	}
}