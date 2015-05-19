package commoninterface.instincts;

import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.ObstacleLocation;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class AvoidObstaclesInstinct extends AvoidEntitiesInstinct{
	
	public AvoidObstaclesInstinct(CIArguments args, RobotCI robot) {
		super(args, robot);
	}
	
	@Override
	public void step(double timestep) {
		
		double left = robot.getLeftMotorSpeed();
		double right = robot.getRightMotorSpeed();
		
		double desiredLeft = left;
		double desiredRight = right;
		
		double closestDistance = Double.MAX_VALUE;
		
		Object[] entities = robot.getEntities().toArray();
		
		for(Object o : entities) {
			
			Entity e = (Entity)o;
			
			if(validEntity(e)) {
				
				ObstacleLocation ge = (ObstacleLocation)e;
				LatLon droneLatLon = drone.getGPSLatLon();
				LatLon geLatLon = ge.getLatLon();
				
				double distance = CoordinateUtilities.distanceInMeters(droneLatLon, geLatLon) - ge.getRadius();
				
				if(distance < safetyDistance) {
					
					double angle = getAngle(droneLatLon, geLatLon);
					
					if(Math.abs(angle) < 90) {//interesting objects are "in front"
												
						if(distance < closestDistance) {
							closestDistance = distance;
							
							if(angle < 0) {
								desiredLeft = 0;
							} else {
								desiredRight = 0;
							}
						}
					}
				}
			}
		}
		
		if(desiredLeft != left || desiredRight != right)
			drone.setMotorSpeeds(desiredLeft, desiredRight);
		
	}

	protected boolean validEntity(Entity e) {
		return e instanceof ObstacleLocation;
	}

}
