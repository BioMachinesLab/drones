package main;

import gamepad.GamePad;
import gamepad.GamePad.GamePadType;
import gui.GUI;
import gui.IPandPortNumberRequestToUser;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import network.ConsoleMessageHandler;
import network.InformationConnection;
import network.MotorConnection;
import network.MotorMessageSender;
import network.messages.InformationRequest.MessageType;
import network.messages.Message;
import threads.BehaviorMessageThread;
import threads.MotorUpdateThread;
import threads.UpdateThread;
import dataObjects.MotorSpeeds;

public class DroneControlConsole extends Thread {
	
	private GUI gui;
	
	private InformationConnection informationConnection;
	private MotorConnection motorConnection;
	
	private GamePad gamePad;

	private MotorSpeeds motorSpeeds;
	private MotorMessageSender motorMessageSender;
	
	private ConsoleMessageHandler messageHandler;
	
	private ArrayList<UpdateThread> updateThreads = new ArrayList<UpdateThread>();
	
	@Override
	public void run() {
		try {
			while(true) {
			
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
				
				while(informationConnection.connectionOK() && motorConnection.connectionOK()) {
					Thread.sleep(500);
				}
				reset("Lost connection to the drone!");
			}
		
		} catch(Exception e) {
			e.printStackTrace();
		}
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
		
		updateThreads.add(new UpdateThread(this, gui.getGPSPanel(), MessageType.GPS));
		updateThreads.add(new UpdateThread(this, gui.getMessagesPanel(), MessageType.SYSTEM_STATUS));
		updateThreads.add(new MotorUpdateThread(this, gui.getMotorsPanel()));
		updateThreads.add(new UpdateThread(this, gui.getCompassPanel(), MessageType.COMPASS));
		updateThreads.add(new BehaviorMessageThread(this, gui.getBehaviorsPanel()));
//		updateThreads.add(new UpdateThread(this, gui.getSysInfoPanel(), MessageType.SYSTEM_INFO));
		
		for(UpdateThread ut : updateThreads)
			ut.start();
	}
	
	private void reset(String reason) {
		for(UpdateThread ut : updateThreads)
			ut.stopExecuting();
		
		updateThreads.clear();
		
		gamePad.stopExecuting();
		gamePad = null;
		
		informationConnection.closeConnection();
		informationConnection = null;
		
		motorConnection.closeConnection();
		motorConnection = null;
		
		messageHandler.stopExecuting();
		messageHandler = null;
		
		motorMessageSender.stopExecuting();
		motorMessageSender = null;
		
		JOptionPane.showMessageDialog(gui, reason);
		
		gui.dispose();
		gui = null;
	}
	
	public void connect() {
		do {
			try {
				IPandPortNumberRequestToUser form = new IPandPortNumberRequestToUser();
				if (form.getIpAddress() == null) {
					continue;
				} else {
					
					messageHandler = new ConsoleMessageHandler(this);

					informationConnection = new InformationConnection(this, form.getIpAddress());
					motorConnection = new MotorConnection(this, form.getIpAddress());
					motorMessageSender = new MotorMessageSender(motorConnection, motorSpeeds);
					
					messageHandler.start();
				}
			} catch (Exception | Error e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		} while (informationConnection == null);
	}
	
	public void processMessage(Message message) {
		messageHandler.addMessage(message,null);
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