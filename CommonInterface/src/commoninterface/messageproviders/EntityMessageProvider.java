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
				log("entity removed "+e.getName());
			}
			
			robot.replaceEntity(e);
			
			if(e instanceof GeoEntity) {
				GeoEntity ge = (GeoEntity)e;
				String str = ge.getLatLon().getLat()+" "+ge.getLatLon().getLon();
				log("entity added"+e.getClass().getSimpleName()+" "+e.getName()+" "+str);
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
	
	private void log(String msg) {
		if(robot.getLogger() != null)
			robot.getLogger().logMessage(msg);
	}
}