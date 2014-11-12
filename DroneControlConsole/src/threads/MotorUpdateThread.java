package threads;

import gui.MotorsPanel;
import gui.UpdatePanel;
import main.DroneControlConsole;

public class MotorUpdateThread extends UpdateThread {
	
	private MotorsPanel motorPanel;

	public MotorUpdateThread(DroneControlConsole console, UpdatePanel panel) {
		super(console, panel, null);
		this.motorPanel = (MotorsPanel)panel;
		motorPanel.registerThread(this);
	}
	
	@Override
	public void run() {

		while (keepGoing) {
			sleepTime = panel.getSleepTime();
			
			console.getMotorSpeeds().setLimit(motorPanel.getMotorLimit());
			console.getMotorSpeeds().setOffset(motorPanel.getMotorOffset());
			console.getMotorSpeeds().setSpeeds(motorPanel.getLeftMotorPower(), motorPanel.getRightMotorPower());
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// we expect interruptions when the GPSPanel changes the refresh rate
			}
		}
	}

}
