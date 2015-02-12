package main;

import gamepad.GamePad;
import gui.GUI;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import objects.DroneLocation;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import network.ConsoleMessageHandler;
import network.InformationConnection;
import network.MotorConnection;
import network.MotorMessageSender;
import network.broadcast.ConsoleBroadcastHandler;
import network.messages.InformationRequest.MessageType;
import network.messages.Message;
import threads.BehaviorMessageThread;
import threads.ConnectionThread;
import threads.MapThread;
import threads.MotorUpdateThread;
import threads.UpdateThread;
import dataObjects.ConsoleMotorSpeeds;

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
		
		if(informationConnection != null) {
			informationConnection.closeConnection();
			informationConnection = null;
		}
		
		if(motorConnection != null) {
			motorConnection.closeConnection();
			motorConnection = null;
		}
		
		if(motorMessageSender != null) {
			motorMessageSender.stopExecuting();
			motorMessageSender = null;
		}
		
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
		
		String[] split = message.split(BroadcastMessage.MESSAGE_SEPARATOR);
		
		switch(split[0]) {
			case "HEARTBEAT":
				long timeElapsed = HeartbeatBroadcastMessage.decode(message);
				gui.getConnectionPanel().newAddress(address);
				break;
			case "GPS":
				DroneLocation di = PositionBroadcastMessage.decode(address, message);
				if(di != null)
					gui.getMapPanel().displayData(di);
				break;
			default:
				System.out.println("Uncategorized message > "+message+" < from "+address);
		}
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public static void main(String[] args) {
		new DroneControlConsole();
	}

}