package commoninterface.messageproviders;

import java.util.ArrayList;
import java.util.Iterator;

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
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.EntityManipulation.Operation;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class EntitiesMessageProvider implements MessageProvider{
	
	private RobotCI robot;
	
	public EntitiesMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {
		
		if(m instanceof EntitiesMessage) {
			
			EntitiesMessage wm = (EntitiesMessage)m;
			ArrayList<Entity> entities = wm.getEntities();
			
			synchronized(robot.getEntities()) {
			
				Iterator<Entity> i = robot.getEntities().iterator();
				
				ArrayList<Entity> entitiesToRemove = new ArrayList<Entity>();
				while(i.hasNext()) {
					Entity e = i.next();
					System.out.println("REMOVING "+e.getClass().getSimpleName());
					if(e instanceof GeoFence || e instanceof Waypoint || e instanceof ObstacleLocation) {
						entitiesToRemove.add(e);
						i.remove();
					}
				}
				
				if (!entitiesToRemove.isEmpty()) {
					EntityManipulation msg = new EntityManipulation(
							Operation.REMOVE, entitiesToRemove, GeoEntity.class.getSimpleName());
					log(LogCodex.encodeLog(LogType.ENTITIES, msg));
				}
				
				robot.getEntities().addAll(entities);
			}
			
			for(Entity e : entities) {
				System.out.println("ADDED "+e.getClass().getSimpleName());
				if(e instanceof GeoEntity) {
					GeoEntity ge = (GeoEntity)e;
					log(ge.getLogMessage());
				} else if(e instanceof GeoFence) {
					GeoFence gf = (GeoFence)e;
					log(gf.getLogMessage());
				}
			}
			
			if(robot instanceof AquaticDroneCI) {
				ArrayList<Waypoint> wps = Waypoint.getWaypoints(robot);
				
				if(!wps.isEmpty()) {
					((AquaticDroneCI) robot).setActiveWaypoint(wps.get(wm.getActiveId()));
					System.out.println("Active waypoint is "+wm.getActiveId());
				}
			}
			return m;
		}
		return null;
	}
	
	private void log(String msg) {
		if(robot.getLogger() != null){
			robot.getLogger().logMessage(msg);
		}
	}
}