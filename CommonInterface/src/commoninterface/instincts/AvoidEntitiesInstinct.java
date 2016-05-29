package commoninterface.instincts;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class AvoidEntitiesInstinct extends CIBehavior{
	
	protected double safetyDistance = 5;
	protected AquaticDroneCI drone;
	
	public AvoidEntitiesInstinct(CIArguments args, RobotCI robot) {
		super(args, robot);
		drone = (AquaticDroneCI)robot;
		safetyDistance = args.getArgumentAsDoubleOrSetDefault("safetydistance",safetyDistance);
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
				
				GeoEntity ge = (GeoEntity)e;
				LatLon droneLatLon = drone.getGPSLatLon();
				LatLon geLatLon = ge.getLatLon();
				
				double distance = CoordinateUtilities.distanceInMeters(droneLatLon, geLatLon);
				
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
	
	protected double getAngle(LatLon d, LatLon e) {

		double absoluteAngle = Math.toRadians(CoordinateUtilities.angleInDegrees(d, e)); 
		double sensorAngle = Math.toRadians(this.drone.getCompassOrientationInDegrees());
		
		double relativeAngle = sensorAngle - absoluteAngle;
		
		while(relativeAngle > Math.PI)
			relativeAngle-=2*Math.PI;
		while(relativeAngle < -Math.PI)
			relativeAngle+=2*Math.PI;
		
		return Math.toDegrees(relativeAngle);
	}
	
	protected boolean validEntity(Entity e) {
		return e instanceof GeoEntity;
	}

}
