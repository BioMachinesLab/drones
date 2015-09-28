package observers;

import java.util.ArrayList;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import gui.DroneGUI;
import gui.panels.map.MapPanel;
import network.server.shared.GPSServerData;
import network.server.shared.dataObjects.DroneData;
import network.server.shared.dataObjects.DronesSet;
import main.DroneControlConsole;

public class ForagingMissionMonitor extends Thread {
	
	protected DroneControlConsole console;
	protected double targetDistance = 5;
	protected double targetRobots = 2;
	
	public ForagingMissionMonitor(DroneControlConsole console) {
		this.console = console;
	}
	
	@Override
	public void run() {
		
		while(true) {
			
			updateStuff();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void updateStuff() {
		
		DronesSet set = console.getDronesSet();
		DroneGUI gui = (DroneGUI)console.getGUI();
		
		if(gui == null)
			return;
		
		MapPanel p = gui.getMapPanel();
		ArrayList<Waypoint> wps = new ArrayList<Waypoint>();
		
		for(Entity e : p.getEntities()) {
			if(e instanceof Waypoint)
				wps.add((Waypoint)e);
		}
		
		Waypoint chosen = null;
		
		for(Waypoint w : wps) {
			
			int closeRobots = 0;
			
			for(DroneData d : set.getDronesSet()) {
				
				if(System.currentTimeMillis() - d.getTimeSinceLastHeartbeat() < 10*1000) { //10 sec
					GPSServerData gps = d.getGPSData();
					double dist = CoordinateUtilities.distanceInMeters(new LatLon(gps.getLatitudeDecimal(),gps.getLongitudeDecimal()), w.getLatLon());
					if(dist < targetDistance) {
						closeRobots++;
					}
				}
			}
			
			if(closeRobots >= targetRobots) {
				chosen = w;
			}
		}
		
		if(chosen != null) {
			ArrayList<Entity> newEntities = new ArrayList<Entity>();
			for(Entity e : p.getEntities()) {
				if(!e.equals(chosen)) {
					
					if(e instanceof GeoFence) {
						GeoFence newGeoFence = new GeoFence(e.getName());
						for(Waypoint wp : ((GeoFence) e).getWaypoints())
							newGeoFence.addWaypoint(wp);
						newEntities.add(newGeoFence);
					} else
						newEntities.add(e);
				}
			}
			p.replaceEntities(newEntities);
			gui.getCommandPanel().deployEntities();
		}
		
	}

}
