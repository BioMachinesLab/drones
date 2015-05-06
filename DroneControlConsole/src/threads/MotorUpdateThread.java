package threads;

import gui.panels.MotorsPanel;
import gui.panels.UpdatePanel;
import main.RobotControlConsole;

public class MotorUpdateThread extends UpdateThread {
	
	private MotorsPanel motorPanel;

	public MotorUpdateThread(RobotControlConsole console, UpdatePanel panel) {
		super(console, panel, null);
		this.motorPanel = (MotorsPanel)panel;
		motorPanel.registerThread(this);
	}
	
	@Override
	public void run() {
		
		while (keepGoing) {
			panel.threadWait();
			console.getMotorSpeeds().setLimit(motorPanel.getMotorLimit());
			console.getMotorSpeeds().setOffset(motorPanel.getMotorOffset());
			console.getMotorSpeeds().setSpeeds(motorPanel.getLeftMotorPower(), motorPanel.getRightMotorPower());
			try {
				Thread.sleep(panel.getSleepTime());
			} catch (InterruptedException e) {}
		}
	}

}
