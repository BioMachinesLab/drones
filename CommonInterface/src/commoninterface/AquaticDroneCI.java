package commoninterface;

import java.util.LinkedList;

import objects.Waypoint;

/**
 * Aquatic Drone Common Interface: an interface that allow for controllers built 
 * on top of it to be run in simulation, on real aquatic drones, and in mixed 
 * environments without modification. 
 * 
 * @author alc
 *
 */
public interface AquaticDroneCI {
	public enum LedState {
		OFF,
		ON,
		BLINKING
	}
	
	/**
	 * Start the drone hardware
	 */
	public void    begin(String[] args, CILogger logger);
	
	/**
	 * Stop the drone hardware
	 */	
	public void    shutdown();
	
	/**
	 * Set the speeds of the motors: 
	 * <ul> 
	 * <li> 1 = full speed forward
	 * <li> 0 = stop
	 * <li> -1 = full speed reverse
	 * </ul>
	 * 
	 * @param leftMotor speed of the left motor [-1,1].
	 * @param rightMotor speed of the right motor [-1,1].
	 */
	public void    setMotorSpeeds(double leftMotor, double rightMotor);
	
	/**
	 * Get the orientation read from the compass in degrees [0, 359].
	 * 
	 * @return the current orientation read from the compass in degrees.
	 */
	public double  getCompassOrientationInDegrees();

	/**
	 * Get the latitude (in decimal) from the GPS.
	 * 
	 * @return latitude read from the GPS in decimal.
	 */
	public double  getGPSLatitude();
	
	/**
	 * Get the longitude (in decimal) from the GPS.
	 * 
	 * @return longitude read from the GPS in decimal.
	 */
	public double  getGPSLongitude();

	/**
	 * Get time elapsed since the controller was started (in seconds).
	 * 
	 * @return get the time elapsed since the controller was started (in seconds).
	 */
	public double  getTimeSinceStart();
	
	/**
	 * Set the state of a LED.
	 * 
	 * @param index index of LED (0,..)
	 * @param state the state of the led
	 */
	public void setLed (int index, LedState state);
	public LinkedList<Waypoint> getWaypoints();
	
	//TODO: Methods for communication and for getting nearby robots and messages from those are still missing
}
