package main;

import commoninterface.network.messages.InformationRequest.MessageType;

import gui.RobotGUI;
import gui.ThymioGUI;
import network.ThymioConsoleMessageHandler;
import network.broadcast.ConsoleBroadcastHandler;
import threads.BehaviorMessageThread;
import threads.ConnectionThread;
import threads.MotorUpdateThread;
import threads.NetworkActivationsUpdateThread;
import threads.UpdateThread;
import dataObjects.ConsoleMotorSpeeds;

public class ThymioControlConsole extends RobotControlConsole {

	public ThymioControlConsole() {
		try {
			
			motorSpeeds = new ConsoleMotorSpeeds();
			
			gui = setupGUI();
			setupGamepad();
			
			new ConsoleBroadcastHandler(this);
			messageHandler = new ThymioConsoleMessageHandler(this);
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
		
		updateThreads.add(new UpdateThread(this, gui.getMessagesPanel(), MessageType.SYSTEM_STATUS));
		updateThreads.add(new UpdateThread(this, ((ThymioGUI)gui).getReadingsPanel(), MessageType.THYMIO_READINGS));
		updateThreads.add(new UpdateThread(this, ((ThymioGUI)gui).getCapturePanel(), MessageType.CAMERA_CAPTURE));
		updateThreads.add(new NetworkActivationsUpdateThread(this, ((ThymioGUI)gui).getNeuralActivationsPanel(), MessageType.NEURAL_ACTIVATIONS));
		updateThreads.add(new MotorUpdateThread(this, gui.getMotorsPanel()));
		updateThreads.add(new BehaviorMessageThread(this, gui.getBehaviorsPanel()));
		
		for(UpdateThread t : updateThreads)
			t.start();
		
	}

	@Override
	protected RobotGUI setupGUI() {
		return new ThymioGUI(this);
	}
	
	public static void main(String[] args) {
		new ThymioControlConsole();
	}
	
}
