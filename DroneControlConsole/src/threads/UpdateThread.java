package threads;

import commoninterface.network.NetworkUtils;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.InformationRequest.MessageType;
import gui.panels.UpdatePanel;
import main.RobotControlConsole;

public class UpdateThread extends Thread {

	protected UpdatePanel panel;
	protected RobotControlConsole console;
	protected boolean keepGoing = true;
	protected MessageType type;
	private String myHostname;

	protected final static long INITIAL_SLEEP = 2000;

	/**
	 * The UpdateThread is responsible for sending data requests, such as GPS,
	 * Compass or status messages, to the drone. It then waits for a response
	 * from the drone before another message is sent. This prevents the drone
	 * from being overrun with messages when connectivity issues appear.
	 */
	public UpdateThread(RobotControlConsole console, UpdatePanel panel,
			MessageType type) {
		this.console = console;
		this.panel = panel;
		this.type = type;
		panel.registerThread(this);
		updateHostname();
	}

	@Override
	public void run() {

		try {
			// Wait until the interface is ready before starting
			Thread.sleep(INITIAL_SLEEP);
		} catch (InterruptedException e) {
		}

		while (keepGoing) {
			console.sendData(new InformationRequest(type, myHostname));
			long timeAfterSending = System.currentTimeMillis();
			panel.threadWait();
			calculateSleep(timeAfterSending);
		}
	}

	/**
	 * Try to keep the desired refresh rate by taking into account the time lost
	 * waiting for an answer
	 */
	protected void calculateSleep(long timeAfterSending) {
		long timeElapsed = System.currentTimeMillis() - timeAfterSending;
		long sleepTime = panel.getSleepTime();

		long necessarySleep = sleepTime - timeElapsed;
		try {
			if (necessarySleep > 0)
				Thread.sleep(necessarySleep);
		} catch (InterruptedException e) {
		}
	}

	public void stopExecuting() {
		keepGoing = false;
	}

	private void updateHostname() {
		myHostname = NetworkUtils.getHostname();
	}
}