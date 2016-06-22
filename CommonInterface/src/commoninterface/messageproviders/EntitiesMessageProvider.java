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
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Target;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.logger.EntityManipulation.Operation;

public class EntitiesMessageProvider implements MessageProvider {

	private RobotCI robot;

	public EntitiesMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {

		if (m instanceof EntitiesMessage) {

			EntitiesMessage wm = (EntitiesMessage) m;
			ArrayList<Entity> entities = wm.getEntities();

			synchronized (robot.getEntities()) {

				Iterator<Entity> i = robot.getEntities().iterator();

				ArrayList<Entity> entitiesToRemove = new ArrayList<Entity>();
				while (i.hasNext()) {
					Entity e = i.next();
					System.out.println("REMOVING " + e.getClass().getSimpleName());
					if (e instanceof GeoFence || e instanceof Waypoint || e instanceof ObstacleLocation
							|| e instanceof Formation || e instanceof Target) {
						entitiesToRemove.add(e);
						i.remove();

						if (e instanceof Formation && robot instanceof AquaticDroneCI) {
							((AquaticDroneCI) robot).setUpdateEntitiesStep(0);
							((AquaticDroneCI) robot).setUpdateEntities(false);
						}
					}
				}

				if (!entitiesToRemove.isEmpty()) {
					for (Entity entity : entitiesToRemove) {
						log(entity.getLogMessage(Operation.REMOVE));
					}
				}

				robot.getEntities().addAll(entities);
			}

			for (Entity e : entities) {
				System.out.println("ADDED " + e.getClass().getSimpleName());
				if (e instanceof GeoEntity) {
					GeoEntity ge = (GeoEntity) e;
					log(ge.getLogMessage(Operation.ADD));
				} else if (e instanceof GeoFence) {
					GeoFence gf = (GeoFence) e;
					log(gf.getLogMessage(Operation.ADD));
				} else if (e instanceof Formation) {
					Formation gf = (Formation) e;
					log(gf.getLogMessage(Operation.ADD));
				} else if (e instanceof Target) {
					Target t = (Target) e;
					log(t.getLogMessage(Operation.ADD));
				}
			}

			if (robot instanceof AquaticDroneCI) {
				ArrayList<Waypoint> wps = Waypoint.getWaypoints(robot);

				if (!wps.isEmpty()) {
					((AquaticDroneCI) robot).setActiveWaypoint(wps.get(wm.getActiveId()));
					System.out.println("Active waypoint is " + wm.getActiveId());
				}
			}
			return m;
		}
		return null;
	}

	private void log(String msg) {
		if (robot.getEntityLogger() != null) {
			robot.getEntityLogger().logMessage(msg);
		}else if (robot.getLogger() != null) {
			robot.getLogger().logMessage(msg);
		}
	}
}