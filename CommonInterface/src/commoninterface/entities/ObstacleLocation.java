package commoninterface.entities;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class ObstacleLocation extends GeoEntity{
	
	private double radius = 2;

	public ObstacleLocation(String name, LatLon latLon, double radius) {
		super(name, latLon);
		this.radius = radius;
	}
	public ObstacleLocation(String name, LatLon latLon) {
		super(name, latLon);
	}
	
	public double getRadius() {
		return radius;
	}
	
	public static LinkedHashSet<ObstacleLocation> getObstacleLocations(RobotCI robot) {
		LinkedHashSet<ObstacleLocation> obstacleLocations = new LinkedHashSet<ObstacleLocation>();
		
		for(Entity e : robot.getEntities()) {
			if(e instanceof ObstacleLocation)
				obstacleLocations.add((ObstacleLocation)e);
		}
		
		return obstacleLocations;
	}
	
	public String getLogMessage() {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		entities.add(this);
		
		return LogCodex.encodeLog(LogType.ENTITIES,
				new EntityManipulation(
						EntityManipulation.Operation.ADD, entities,
						this.getClass().getSimpleName()));
	}
	
}
