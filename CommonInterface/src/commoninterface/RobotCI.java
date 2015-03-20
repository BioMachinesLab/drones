package commoninterface;

import java.util.ArrayList;

import objects.Entity;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.utils.CIArguments;

public interface RobotCI {

	/**
	 * Start the drone hardware
	 */
	public void    begin(CIArguments args, CILogger logger);
	
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
	 * Get time elapsed since the controller was started (in seconds).
	 * 
	 * @return get the time elapsed since the controller was started (in seconds).
	 */
	public double  getTimeSinceStart();
	
	/**
	 * Get the list of entities that have been detected/received by the drone (waypoints, other drones' locations, etc).
	 * 
	 * @return the list with all the current entities.
	 */
	public ArrayList<Entity> getEntities();
	
	/**
	 * Get the list of registered sensors.
	 * 
	 * @return the list with all the current sensors.
	 */
	public ArrayList<CISensor> getCISensors();
	
	/**
	 * The network address is the identifier of each drone.
	 * 
	 * @return IP address of the drone
	 */
	public String getNetworkAddress();
	
	/**
	 * The BroadcastHandler takes care of communication with nearby drone.
	 * It is possible to send and receive messages.
	 * 
	 * @return the BroadcastHandler
	 */
	public BroadcastHandler getBroadcastHandler();
	
}
