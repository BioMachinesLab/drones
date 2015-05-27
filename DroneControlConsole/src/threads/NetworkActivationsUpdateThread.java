package threads;

import commoninterface.network.NetworkUtils;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.InformationRequest.MessageType;
import gui.panels.UpdatePanel;
import main.RobotControlConsole;

public class NetworkActivationsUpdateThread extends UpdateThread {
	private String myHostname = "";

	public NetworkActivationsUpdateThread(RobotControlConsole console,
			UpdatePanel panel, MessageType type) {
		super(console, panel, type);
		updateHostname();
	}

	@Override
	public void run() {

		panel.threadWait();

		while (keepGoing) {
			console.sendData(new InformationRequest(type, myHostname));
			long timeAfterSending = System.currentTimeMillis();
			panel.threadWait();
			calculateSleep(timeAfterSending);
		}
	}

	private void updateHostname() {
		myHostname = NetworkUtils.getHostname();
	}
}
