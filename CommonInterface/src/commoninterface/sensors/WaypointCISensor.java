package commoninterface.sensors;

import java.util.ArrayList;

import objects.Entity;
import objects.Waypoint;
import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public class WaypointCISensor extends CISensor{
	
	private double[] readings = {0,0};
	private double range = 1;

	public WaypointCISensor(int id, AquaticDroneCI drone, CIArguments args) {
		super(id, drone, args);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}

	@Override
	public void update(double time, ArrayList<Entity> entities) {
		
		double closestDistance = Double.MAX_VALUE;
		
		Vector2d robotPos = new Vector2d(drone.getGPSLatitude(), drone.getGPSLongitude());
		
		for(Entity e : entities) {
			if(e instanceof Waypoint) {
				Vector2d latLon = new Vector2d(e.getLatitude(),e.getLongitude());
				
				double currentDistance = CoordinateUtilities.distanceInMeters(robotPos,latLon);
				
				if(currentDistance < closestDistance) {
				
					double currentOrientation = drone.getCompassOrientationInDegrees();
					double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotPos,latLon);
					
					double difference = currentOrientation - coordinatesAngle;
					
					difference%=360;
					
					if(difference > 180){
						difference = -((180 -difference) + 180);
					}
					readings[0] = difference;
					readings[1] = currentDistance;
					closestDistance = currentDistance;
				}
			}
		}
	}
	
	public double getRange() {
		return range;
	}
}
