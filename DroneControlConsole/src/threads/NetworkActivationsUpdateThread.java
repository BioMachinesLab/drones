package threads;

import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.InformationRequest.MessageType;

import gui.panels.UpdatePanel;
import main.RobotControlConsole;

public class NetworkActivationsUpdateThread extends UpdateThread {

	public NetworkActivationsUpdateThread(RobotControlConsole console, UpdatePanel panel, MessageType type) {
		super(console, panel, type);
	}

	@Override
	public void run() {
		
		panel.threadWait();

		while (keepGoing) {
			console.sendData(new InformationRequest(type));
			long timeAfterSending = System.currentTimeMillis();
			panel.threadWait();
			calculateSleep(timeAfterSending);
		}
	}
	
}
