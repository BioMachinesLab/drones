package commoninterface.network.broadcast;

import java.util.ArrayList;
import java.util.Scanner;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;

public class EntitiesBroadcastMessage extends BroadcastMessage {

	public static final String IDENTIFIER = "ENTITIES";
	private static final int UPDATE_TIME = Integer.MAX_VALUE; //no update
	private ArrayList<Entity> entities;
	
	public EntitiesBroadcastMessage(ArrayList<Entity> entities) {
		super(UPDATE_TIME, IDENTIFIER);
		this.entities = entities;
	}
	
	@Override
	public String getMessage() {
		
		String result = "";
		
		for(Entity e : entities) {
			result+=getLogString(e);
		}
		
		return result;
	}
	
	private String getLogString(Entity e) {
		String result = "";
		
		result+=e.getClass().getSimpleName()+MESSAGE_SEPARATOR;
		result+=e.getName()+MESSAGE_SEPARATOR;
		
		if(e instanceof GeoEntity) {
			GeoEntity ge = (GeoEntity)e;
			result+=ge.getLatLon().getLat()+MESSAGE_SEPARATOR;
			result+=ge.getLatLon().getLon()+MESSAGE_SEPARATOR;
		} else if(e instanceof GeoFence){
			GeoFence gf = (GeoFence)e;
			result+=gf.getWaypoints().size()+MESSAGE_SEPARATOR;
			for(Waypoint wp : gf.getWaypoints())
				result+=getLogString(wp);
		}
		
		return result;
	}
	
	public static ArrayList<Entity> decode(String address, String message) {
		
		ArrayList<Entity> entities = new ArrayList<Entity>();
		try {
			Scanner s = new Scanner(message);
			s.useDelimiter(MESSAGE_SEPARATOR);
			s.next();
			
			while(s.hasNext()) {
				
				String className = s.next();
				String name = s.next();
				
				if(className.equals(GeoFence.class.getSimpleName())) {
					
					String number = s.next();
					GeoFence gf = new GeoFence(name);
					
					for(int i = 0 ; i < Integer.parseInt(number) ; i++) {
						s.next();//class
						s.next();//name
						String lat = s.next();
						String lon = s.next();
						gf.addWaypoint(new LatLon(Double.parseDouble(lat),Double.parseDouble(lon)));
					}
					
					entities.add(gf);
					
				} else if(className.equals(Waypoint.class.getSimpleName())) {
					String lat = s.next();
					String lon = s.next();
					entities.add(new Waypoint(name, new LatLon(Double.parseDouble(lat),Double.parseDouble(lon))));
				} else if(className.equals(ObstacleLocation.class.getSimpleName())) {
					String lat = s.next();
					String lon = s.next();
					entities.add(new ObstacleLocation(name, new LatLon(Double.parseDouble(lat),Double.parseDouble(lon))));
				}
			}
			
			s.close();
		
		}catch(Exception e) {
			e.printStackTrace();	
		}
		
		return entities;
		
	}

}
