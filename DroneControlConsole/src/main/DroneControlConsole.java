package main;

import gui.DroneGUI;
import gui.RobotGUI;
import network.DroneConsoleMessageHandler;
import network.broadcast.ConsoleBroadcastHandler;
import threads.BehaviorMessageThread;
import threads.ConnectionThread;
import threads.MapThread;
import threads.MotorUpdateThread;
import threads.UpdateThread;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import commoninterface.network.messages.InformationRequest.MessageType;
import commoninterface.objects.RobotLocation;
import dataObjects.ConsoleMotorSpeeds;

public class DroneControlConsole extends RobotControlConsole {
	
	public DroneControlConsole() {
		try {
			
			motorSpeeds = new ConsoleMotorSpeeds();
			
			gui = setupGUI();
			setupGamepad();
			
			new ConsoleBroadcastHandler(this);
			messageHandler = new DroneConsoleMessageHandler(this);
			messageHandler.start();
			
			//Special case which does not depend on a connection and should only be started once
			ConnectionThread t = new ConnectionThread(this, gui.getConnectionPanel());
			t.start();
			
			gui.setVisible(true);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void createUpdateThreads() {
		
		for(UpdateThread t : updateThreads)
			t.stopExecuting();
		
		updateThreads.clear();
		
		updateThreads.add(new UpdateThread(this, ((DroneGUI)gui).getGPSPanel(), MessageType.GPS));
		updateThreads.add(new UpdateThread(this, gui.getMessagesPanel(), MessageType.SYSTEM_STATUS));
		updateThreads.add(new MotorUpdateThread(this, gui.getMotorsPanel()));
		updateThreads.add(new UpdateThread(this, ((DroneGUI)gui).getCompassPanel(), MessageType.COMPASS));
		updateThreads.add(new BehaviorMessageThread(this, gui.getBehaviorsPanel()));
		updateThreads.add(new MapThread(this, ((DroneGUI)gui).getMapPanel()));
		
		for(UpdateThread t : updateThreads)
			t.start();
		
//		updateThreads.add(new UpdateThread(this, gui.getSysInfoPanel(), MessageType.SYSTEM_INFO));
	}
	
	@Override
	public RobotGUI setupGUI() {
		return new DroneGUI(this);
	}

	public static void main(String[] args) {
		new DroneControlConsole();
	}
	
}