package threads;

import gui.panels.ConnectionPanel;
import main.DroneControlConsole;

public class ConnectionThread extends UpdateThread {
	
	private ConnectionPanel connectionPanel;

	public ConnectionThread(DroneControlConsole console, ConnectionPanel panel) {
		super(console, panel, null);
		this.connectionPanel = (ConnectionPanel)panel;
	}
	
	@Override
	public void run() {

		while (true) {
			
			connectionPanel.cleanupAddresses();
			
			try {
				Thread.sleep(connectionPanel.getSleepTime());
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}