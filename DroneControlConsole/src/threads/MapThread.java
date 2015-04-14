package threads;

import commoninterface.network.messages.EntityMessage;

import gui.panels.UpdatePanel;
import gui.panels.map.MapPanel;
import main.DroneControlConsole;

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
		}
	}
}