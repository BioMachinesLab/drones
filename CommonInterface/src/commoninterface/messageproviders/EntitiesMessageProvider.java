package commoninterface.messageproviders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.Waypoint;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;

public class EntitiesMessageProvider implements MessageProvider{
	
	private RobotCI robot;
	
	public EntitiesMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {
		
		if(m instanceof EntitiesMessage) {
			EntitiesMessage wm = (EntitiesMessage)m;
			LinkedList<Entity> entities = wm.getEntities();
			
			synchronized(robot.getEntities()) {
			
				Iterator<Entity> i = robot.getEntities().iterator();
				
				while(i.hasNext()) {
					Entity e = i.next();
					if(e instanceof GeoFence ||
							e instanceof Waypoint ||
							e instanceof ObstacleLocation) {
						log("entity removed "+e.getName());
						i.remove();
					}
				}
				
				robot.getEntities().addAll(entities);
			}
			
			for(Entity e : entities) {
				if(e instanceof GeoEntity) {
					GeoEntity ge = (GeoEntity)e;
					String str = ge.getLatLon().getLat()+" "+ge.getLatLon().getLon();
					log("entity added "+e.getClass().getSimpleName()+" "+e.getName()+" "+str);
				} else if(e instanceof GeoFence) {
					GeoFence gf = (GeoFence)e;
					
					String str = "";
					
					for(Waypoint w : gf.getWaypoints())
						str+= w.getLatLon().getLat()+" "+w.getLatLon().getLon()+" ";
					
					log("entity added "+e.getClass().getSimpleName()+" "+e.getName()+" "+gf.getWaypoints().size()+" "+str);
				}
			}
			
			if(robot instanceof AquaticDroneCI) {
				ArrayList<Waypoint> wps = Waypoint.getWaypoints(robot);
				
				if(!wps.isEmpty())
					((AquaticDroneCI) robot).setActiveWaypoint(wps.get(0));
			}
			return m;
		}
		return null;
	}
	
	private void log(String msg) {
		if(robot.getLogger() != null)
			robot.getLogger().logMessage(msg);
	}
}