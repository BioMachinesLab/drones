package commoninterface.messageproviders;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.Waypoint;
import commoninterface.network.messages.EntityMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class EntityMessageProvider implements MessageProvider{
	
	private RobotCI robot;
	
	public EntityMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {
		
		if(m instanceof EntityMessage) {
			EntityMessage wm = (EntityMessage)m;
			Entity e = wm.getEntity();
			
			if(robot.getEntities().contains(e)) {				
				ArrayList<Entity> entities = new ArrayList<Entity>();
				entities.add(e);
				log(new EntityManipulation(EntityManipulation.Operation.REMOVE, entities,	null));
			}
			
			robot.replaceEntity(e);
			
			if(e instanceof GeoEntity) {
				GeoEntity ge = (GeoEntity)e;
				ArrayList<Entity> entities = new ArrayList<Entity>();
				entities.add(ge);
				log(new EntityManipulation(EntityManipulation.Operation.ADD, entities, e.getClass().getSimpleName()));
			}
			
			if(e instanceof Waypoint && robot instanceof AquaticDroneCI) {
				
				ArrayList<Waypoint> wps = Waypoint.getWaypoints(robot);
				
				if(wps.isEmpty())
					((AquaticDroneCI) robot).setActiveWaypoint((Waypoint)e);
			}
			return m;
		}
		return null;
	}
	
	private void log(EntityManipulation msg) {
		if(robot.getLogger() != null){
			String str = LogCodex.encodeLog(LogType.ENTITIES, msg);
			robot.getLogger().logMessage(str);
		}
	}
}