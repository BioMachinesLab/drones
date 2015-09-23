/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.RobotLocation;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

/**
 *
 * @author jorge
 */
public class SingleEnemyCISensor extends CISensor {
    
    	private AquaticDroneCI drone;
	private double[] readings = {0,0};
	private double range = 1;

	public SingleEnemyCISensor(int id, RobotCI robot, CIArguments args) {
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
                RobotLocation enemy = null;
                for(Object e : entities) {
            		if(e instanceof RobotLocation) {
                            RobotLocation rl = (RobotLocation)e;
                            if(rl.getDroneType() == AquaticDroneCI.DroneType.ENEMY) {
                                enemy = rl;
                                break;
                            }
                        }
		}
		
		LatLon robotLatLon = drone.getGPSLatLon();
		if(enemy != null) {
			LatLon latLon = enemy.getLatLon();
			
			double currentDistance = CoordinateUtilities.distanceInMeters(robotLatLon,latLon);
			
			double currentOrientation = drone.getCompassOrientationInDegrees();
			double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotLatLon,latLon);
			
			double difference = currentOrientation - coordinatesAngle;
			
			difference%=360;
			
			if(difference > 180){
				difference = -((180 -difference) + 180);
			}
			
			readings[0] = difference / 360 + 0.5;
			readings[1] = currentDistance / range;
		}  else {
			//If there is no enemy
			//should act as if the robot is very far
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
