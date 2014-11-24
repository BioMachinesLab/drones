package threads;

import gui.UpdatePanel;
import gui.map.MapPanel;
import main.DroneControlConsole;
import network.messages.WaypointMessage;

public class MapThread extends UpdateThread {
	
	private MapPanel mapPanel;

	public MapThread(DroneControlConsole console, UpdatePanel panel) {
		super(console, panel, null);
		this.mapPanel = (MapPanel)panel;
		mapPanel.registerThread(this);
	}
	
	@Override
	public void run() {

		while (keepGoing) {
			panel.threadWait();
			
			WaypointMessage msg = mapPanel.getCurrentMessage();
			
			if(msg != null)
				console.sendData(msg);
		}
	}
}