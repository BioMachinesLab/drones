package threads;

import gui.UpdatePanel;
import main.DroneControlConsole;
import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;

public class UpdateThread extends Thread {
	
	protected UpdatePanel panel;
	protected DroneControlConsole console;
	protected boolean keepGoing = true;
	protected MessageType type;
	
	public UpdateThread(DroneControlConsole console, UpdatePanel panel, MessageType type) {
		this.console = console;
		this.panel = panel;
		this.type = type;
		panel.registerThread(this);
	}
	
	@Override
	public void run() {

		while (keepGoing) {
			console.sendData(new InformationRequest(type));
			panel.threadSleep();
		}
	}
	
	public void stopExecuting() {
		keepGoing = false;
	}

}
