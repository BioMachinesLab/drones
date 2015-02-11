package commoninterface.sensors;

import java.util.ArrayList;
import objects.Entity;
import objects.Waypoint;
import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

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
		
		LatLon robotLatLon = drone.getGPSLatLon();
		
		boolean found = false;
		
		for(Entity e : entities) {
			if(e instanceof Waypoint) {
				LatLon latLon = new LatLon(e.getLatitude(),e.getLongitude());
				
				double currentDistance = CoordinateUtilities.distanceInMeters(robotLatLon,latLon);
				
				if(currentDistance < closestDistance) {
				
					double currentOrientation = drone.getCompassOrientationInDegrees();
					double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotLatLon,latLon);
					
					double difference = currentOrientation - coordinatesAngle;
					
					difference%=360;
					
					if(difference > 180){
						difference = -((180 -difference) + 180);
					}
					
					readings[0] = difference;
					readings[1] = currentDistance;
					found = true;
					closestDistance = currentDistance;
				}
			}
		}
		
		if(!found) {
			//If there is no waypoint, the sensor
			//should act as if the robot arrived at a waypoint
			readings[0] = 0.5;
			readings[1] = 1;
		}
	}
	
	public double getRange() {
		return range;
	}

	@Override
	public int getNumberOfSensors() {
		return readings.length;
	}
}
