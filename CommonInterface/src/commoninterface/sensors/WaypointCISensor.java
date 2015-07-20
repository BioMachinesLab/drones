package commoninterface.sensors;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.controllers.StationKeepingCIBehavior;
import commoninterface.entities.Entity;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class WaypointCISensor extends CISensor{
	
	private AquaticDroneCI drone;
	private double[] readings = {0,0};
	private double range = 1;

	public WaypointCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
		
		drone = (AquaticDroneCI)robot;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}

	@Override
	public void update(double time, Object[] entities) {
		
		LatLon robotLatLon = drone.getGPSLatLon();
		
		Waypoint wp = drone.getActiveWaypoint();
		if(wp != null) {
				
			LatLon latLon = wp.getLatLon();
			
			double currentDistance = CoordinateUtilities.distanceInMeters(robotLatLon,latLon);
			
			double currentOrientation = drone.getCompassOrientationInDegrees();
			double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotLatLon,latLon);
			
			double difference = currentOrientation - coordinatesAngle;
			
			difference%=360;
			
			if(difference > 180){
				difference = -((180 -difference) + 180);
			}
			
			readings[0] = difference;
			readings[1] = currentDistance;
		}  else {
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
