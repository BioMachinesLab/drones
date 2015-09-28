package fieldtests;

import gui.panels.CommandPanel;
import gui.panels.map.MapPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import main.DroneControlConsole;
import network.CommandSender;
import network.server.shared.dataObjects.DroneData;
import network.server.shared.dataObjects.DronesSet;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.NetworkUtils;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.Message;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import commoninterface.utils.jcoord.LatLon;

public abstract class FieldTestScript {
	
	protected CommandPanel commandPanel;
	protected MapPanel mapPanel;
	protected DroneControlConsole console;
	
	public FieldTestScript(DroneControlConsole console, CommandPanel commandPanel, MapPanel mapPanel) {
		this.commandPanel = commandPanel;
		this.console = console;
	}
	
	/**
	 * API:
	 * 
	 * void		startControllers
	 * void		stopControllers
	 * void		goToWaypoint (sends to drones and replaces its entities BUT does not change the Console's map entities)
	 * 
	 * boolean	arrivedAtWaypoint
	 * 
	 * void		addEntityToMap
	 * void		deployMapEntities (sends to drones)
	 * void		clearMapEntities
	 * 
	 * GeoFence defineGeoFence (does not add to the Console's map)
	 * ArrayList<Waypoint> generateWaypointsInGeoFence  (does not add to the Console's map)
	 * 
	 */
	
	protected void start() {
		
	}	
	
	protected void startControllers(ArrayList<String> ips, String behavior, String description) {
		
		try {
			CIArguments args = readConfigurationFile(behavior);
			args.setArgument("description", description);
			BehaviorMessage msg = new BehaviorMessage(args.getArgumentAsString("type"), args.getCompleteArgumentString(), true, NetworkUtils.getHostname());
			deploy(msg,ips);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void stopControllers(ArrayList<String> ips, String description) {
		BehaviorMessage msg = new BehaviorMessage("ControllerCIBehavior", "", false, NetworkUtils.getHostname());
		deploy(msg,ips);
	}
	
	protected void deployMapEntities(ArrayList<String> ips) {
		ArrayList<Entity> entities = mapPanel.getEntities();
		EntitiesMessage m = new EntitiesMessage(entities, NetworkUtils.getHostname());
		deploy(m,ips);
	}
	
	protected void goToWaypoint(ArrayList<String> ips, Waypoint wp) {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		entities.add(wp);
		EntitiesMessage m = new EntitiesMessage(entities, NetworkUtils.getHostname());
		deploy(m,ips);
	}
	
	protected boolean arrivedAtWaypoint(ArrayList<String> ips, Waypoint wp, int distanceThresholdInMeters) {
		
		DronesSet set = console.getDronesSet();
		
		for(String ip : ips) {
			DroneData d = set.getDrone(ip);
			LatLon latLon = new LatLon(d.getGPSData().getLatitudeDecimal(), d.getGPSData().getLongitudeDecimal());
			if(latLon.distance(wp.getLatLon())*1000 > distanceThresholdInMeters) {
				return false;
			}
		}
		
		return true;
	}
	
	protected GeoFence defineGeoFence(LatLon center, int width, int height) {
		
		GeoFence fence = new GeoFence("fence");
		
		Vector2d vec = CoordinateUtilities.GPSToCartesian(center);
		
		vec.x-=width/2;
		vec.y+=height/2;
		
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x+=width;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.y-=height;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x-=width;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		
		return fence;
		
	}
	
	protected void clearMapEntities() {
		mapPanel.clearEntities();
	}
	
	protected void addEntityToMap(Entity e) {
		
		ArrayList<Entity> newEntities = new ArrayList<Entity>();
		newEntities.addAll(mapPanel.getEntities());
		newEntities.add(e);
		
		mapPanel.replaceEntities(newEntities);
	}
	
	protected ArrayList<Waypoint> generateWaypointsInGeoFence(GeoFence fence, int number, int minDistance, int maxDistance, long randomSeed) {
		ArrayList<Waypoint> wps = new ArrayList<Waypoint>();
		
		Random r = new Random(randomSeed);
		
		Vector2d min = new Vector2d(Double.MAX_VALUE, Double.MAX_VALUE);
		Vector2d max = new Vector2d(-Double.MAX_VALUE, -Double.MAX_VALUE);
		
		for(Waypoint wp : fence.getWaypoints()) {
			Vector2d v = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
			min.x = Math.min(min.x, v.x);
			min.y = Math.min(min.y, v.y);
			max.x = Math.max(max.x, v.x);
			max.y = Math.max(max.y, v.y);
		}
		
		ArrayList<Line> lines = new ArrayList<Line>();
		LinkedList<Waypoint> fenceWPs = fence.getWaypoints();
		for(int i = 1 ; i < fenceWPs.size() ; i++) {
			Waypoint wa = fenceWPs.get(i-1);
			Waypoint wb = fenceWPs.get(i);
			lines.add(getLine(wa,wb));
		}
		//loop around
		Waypoint wa = fenceWPs.get(fenceWPs.size()-1);
		Waypoint wb = fenceWPs.get(0);
		lines.add(getLine(wa,wb));
		
		for(int i = 0 ; i < number ; i++) {
			
			Vector2d pos = null;
			
			int tries = 0;
			
			do {
				double x = min.x + r.nextDouble()*(max.x-min.x);
				double y = min.y + r.nextDouble()*(max.y-min.y);
				pos = new Vector2d(x,y);
				
				if(tries++ > 100) {
					return null;
				}
				
			} while(!safePosition(pos, wps, lines, minDistance, maxDistance));
			
			if(i >= wps.size()) {
				Waypoint w = new Waypoint("wp"+i, CoordinateUtilities.cartesianToGPS(pos));
				wps.add(w);
			}
		}
		
		return wps;
	}
	
	private void deploy(Message m, ArrayList<String> ips) {
		new CommandSender(m, ips, commandPanel, false).start();
	}
	
	private CIArguments readConfigurationFile(String name) throws FileNotFoundException {
		
		File f = new File(CommandPanel.CONTROLLERS_FOLDER + "/" + name + ".conf");
		
		String result = "";
		Scanner scanner = new Scanner(f);
		
		while(scanner.hasNext())
			result += scanner.nextLine() + "\n";

		scanner.close();
		
		CIArguments translatedArgs = new CIArguments(result.replaceAll("\\s+", ""), true);
		
		return translatedArgs;
	}
	
	private Line getLine(Waypoint wa, Waypoint wb) {
		Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());
		return new Line(va.getX(), va.getY(), vb.getX(), vb.getY());
	}
	
	private boolean safePosition(Vector2d v, ArrayList<Waypoint> wps, ArrayList<Line> lines, double safetyDistance, double maxDistance) {
		
		if(insideBoundary(v, lines)) {
			
			double min = Double.MAX_VALUE;
		
			for(Waypoint wp : wps) {
				
				double distance = CoordinateUtilities.GPSToCartesian(wp.getLatLon()).distanceTo(v);
				
				min = Math.min(distance,min);
				
				if(distance < safetyDistance)
					return false;
			}
			return min < maxDistance || wps.isEmpty();
		}
		return false;
	}
	
	private boolean insideBoundary(Vector2d wp, ArrayList<Line> lines) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		
		for(Line l : lines) {
			if(l.intersectsWithLineSegment(wp, new Vector2d(0,-Integer.MAX_VALUE)) != null)
				count++;
		}
		return count % 2 != 0;
	}
	
}