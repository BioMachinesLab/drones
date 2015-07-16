package commoninterface;

import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;

/**
 * Aquatic Drone Common Interface: an interface that allow for controllers built 
 * on top of it to be run in simulation, on real aquatic drones, and in mixed 
 * environments without modification. 
 * 
 * @author alc
 *
 */
public interface AquaticDroneCI extends RobotCI{
	
	public enum DroneType {DRONE,ENEMY};
	
	/**
	 * Get the orientation read from the compass in degrees [0, 359].
	 * 
	 * @return the current orientation read from the compass in degrees.
	 */
	public double  getCompassOrientationInDegrees();

	/**
	 * Get the latitude and longitude (in decimal) from the GPS.
	 * 
	 * @return LatLon read from the GPS in decimal.
	 */
	public LatLon  getGPSLatLon();
	
	/**
	 * Get the orientation (in degrees) from the GPS.
	 * 
	 * @return orientation read from the GPS in degrees
	 */
	public double getGPSOrientationInDegrees();
	
	/**
	 * Set the state of a LED.
	 * 
	 * @param index index of LED (0,..)
	 * @param state the state of the led
	 */
	public void setLed (int index, LedState state);
	
	public void setActiveWaypoint(Waypoint wp);
	
	public Waypoint getActiveWaypoint();
	
	public DroneType getDroneType();
	
	public void setDroneType(DroneType droneType);
	
	public void setRudder(double heading, double speed);
	
	public double getMotorSpeedsInPercentage();
	
}
