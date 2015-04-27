package commoninterface.messageproviders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.objects.Entity;
import commoninterface.objects.GeoFence;
import commoninterface.objects.Waypoint;

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
			
			Iterator<Entity> i = robot.getEntities().iterator();
			
			while(i.hasNext()) {
				Entity e = i.next();
				if(e instanceof GeoFence)
					i.remove();
				if(e instanceof Waypoint)
					i.remove();
			}
			
			robot.getEntities().addAll(entities);
			
			if(robot instanceof AquaticDroneCI) {
				ArrayList<Waypoint> wps = Waypoint.getWaypoints(robot);
				
				if(!wps.isEmpty())
					((AquaticDroneCI) robot).setActiveWaypoint(wps.get(0));
			}
			
			return m;
		}
		
		return null;
	}

}
