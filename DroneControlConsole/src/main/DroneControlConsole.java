package main;

import gamepad.GamePad;
import gui.GUI;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import network.ConsoleMessageHandler;
import network.InformationConnection;
import network.MotorConnection;
import network.MotorMessageSender;
import network.broadcast.BroadcastStatusThread;
import network.broadcast.ConsoleBroadcastHandler;
import network.broadcast.HeartbeatStatusThread;
import network.broadcast.PositionStatusThread;
import network.messages.InformationRequest.MessageType;
import network.messages.Message;
import threads.BehaviorMessageThread;
import threads.ConnectionThread;
import threads.MapThread;
import threads.MotorUpdateThread;
import threads.UpdateThread;
import dataObjects.ConsoleMotorSpeeds;
import dataObjects.GPSData;

public class DroneControlConsole {
	
	private GUI gui;
	
	private InformationConnection informationConnection;
	private MotorConnection motorConnection;
	
	private GamePad gamePad;

	private ConsoleMotorSpeeds motorSpeeds;
	private MotorMessageSender motorMessageSender;
	private ConsoleMessageHandler messageHandler;
	
	private ArrayList<UpdateThread> updateThreads = new ArrayList<UpdateThread>();
	
	private boolean connected = false;
	
	public DroneControlConsole() {
		try {
			
			motorSpeeds = new ConsoleMotorSpeeds();
			
			gui = new GUI(this);
			setupGamepad();
			
			new ConsoleBroadcastHandler(this);
			messageHandler = new ConsoleMessageHandler(this);
			messageHandler.start();
			
			//Special case which does not depend on a connection and should only be started once
			ConnectionThread t = new ConnectionThread(this, gui.getConnectionPanel());
			t.start();
			
			gui.setVisible(true);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setupGamepad() {
		try {
			gamePad = new GamePad(this);
			gamePad.start();
		} catch(UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}
	
	private void createUpdateThreads() {
		
		for(UpdateThread t : updateThreads)
			t.stopExecuting();
		
		updateThreads.clear();
		
		updateThreads.add(new UpdateThread(this, gui.getGPSPanel(), MessageType.GPS));
		updateThreads.add(new UpdateThread(this, gui.getMessagesPanel(), MessageType.SYSTEM_STATUS));
		updateThreads.add(new MotorUpdateThread(this, gui.getMotorsPanel()));
		updateThreads.add(new UpdateThread(this, gui.getCompassPanel(), MessageType.COMPASS));
		updateThreads.add(new BehaviorMessageThread(this, gui.getBehaviorsPanel()));
		updateThreads.add(new MapThread(this, gui.getMapPanel()));
		
		for(UpdateThread t : updateThreads)
			t.start();
		
//		updateThreads.add(new UpdateThread(this, gui.getSysInfoPanel(), MessageType.SYSTEM_INFO));
	}
	
	public synchronized void connect(String address) {
		try {
			
			if(connected)
				disconnect();
			
			gui.getMessagesPanel().clear();
				
			informationConnection = new InformationConnection(this, InetAddress.getByName(address));
			motorConnection = new MotorConnection(this, InetAddress.getByName(address));
			motorMessageSender = new MotorMessageSender(motorConnection, motorSpeeds);
			
			informationConnection.start();
			motorConnection.start();
			motorMessageSender.start();
			
			createUpdateThreads();
			
			connected = true;
			gui.getConnectionPanel().connectionOK(address);
		} catch (Exception | Error e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		
	}
	
	public synchronized void disconnect() {
		
		for(UpdateThread ut : updateThreads)
			ut.stopExecuting();
		
		updateThreads.clear();
		
		informationConnection.closeConnection();
		informationConnection = null;
		
		motorConnection.closeConnection();
		motorConnection = null;
		
		motorMessageSender.stopExecuting();
		motorMessageSender = null;
		
		gui.getConnectionPanel().disconnected();
		connected = false;
	}
	
	public void processMessage(Message message) {
		if(messageHandler != null)
			messageHandler.addMessage(message, null);
	}
	
	public ConsoleMotorSpeeds getMotorSpeeds() {
		return motorSpeeds;
	}
	
	public GamePad getGamePad() {
		return gamePad;
	}
	
	public void sendData(Message message) {
		if(informationConnection != null)
			informationConnection.sendData(message);
	}
	
	public void newBroadcastMessage(String address, String message) {
		
		String[] split = message.split(BroadcastStatusThread.MESSAGE_SEPARATOR);
		
		switch(split[0]) {
			case "HEARTBEAT":
				long timeElapsed = HeartbeatStatusThread.decode(message);
				gui.getConnectionPanel().newAddress(address);
				break;
			case "GPS":
				GPSData gpsData = PositionStatusThread.decode(address, message);
				if(gpsData != null)
					gui.getMapPanel().displayData(gpsData);
				break;
			default:
				System.out.println("Uncategorized message >"+message+" from "+address);
		}
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public static void main(String[] args) {
		new DroneControlConsole();
	}

}