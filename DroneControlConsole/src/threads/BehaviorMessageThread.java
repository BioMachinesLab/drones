package threads;

import gui.panels.BehaviorsPanel;
import gui.panels.UpdatePanel;
import main.DroneControlConsole;
import network.messages.BehaviorMessage;

public class BehaviorMessageThread extends UpdateThread {
	
	private BehaviorsPanel behaviorsPanel;

	public BehaviorMessageThread(DroneControlConsole console, UpdatePanel panel) {
		super(console, panel, null);
		this.behaviorsPanel = (BehaviorsPanel)panel;
		behaviorsPanel.registerThread(this);
	}
	
	@Override
	public void run() {

		while (keepGoing) {
			panel.threadWait();
			
			BehaviorMessage msg = behaviorsPanel.getCurrentMessage();
			
			if(msg != null)
				console.sendData(msg);
		}
	}
}