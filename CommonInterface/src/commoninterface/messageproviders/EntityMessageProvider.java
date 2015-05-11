package commoninterface.messageproviders;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.network.messages.EntityMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.objects.Entity;
import commoninterface.objects.Waypoint;

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
				robot.getEntities().remove(e);
			}
			
			if(e instanceof Waypoint && robot instanceof AquaticDroneCI) {
				ArrayList<Waypoint> wps = Waypoint.getWaypoints(robot);
				
				if(wps.isEmpty())
					((AquaticDroneCI) robot).setActiveWaypoint((Waypoint)e);
			}
			
			robot.getEntities().add(e);
			return m;
		}
		
		return null;
	}

}
