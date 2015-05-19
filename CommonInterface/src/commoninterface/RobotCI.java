package commoninterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commoninterface.entities.Entity;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.CIArguments;
import commoninterface.utils.RobotLogger;

public interface RobotCI {

	/**
	 * Start the drone hardware
	 */
	public void    begin(HashMap<String,CIArguments> args);
	
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
	public void setMotorSpeeds(double leftMotor, double rightMotor);
	
	/**
	 * Get the current left motor speed of the robot.
	 * 
	 * @return the left motor speed
	 */
	public double getLeftMotorSpeed();
	
	/**
	 * Get the current right motor speed of the robot.
	 * 
	 * @return the right motor speed
	 */
	public double getRightMotorSpeed();
	
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
	 * Replaces an old entity with a more recent one.
	 */
	public void replaceEntity(Entity e);
	
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
	
	/**
	 * Process a request that has been sent via the network
	 */
	public void processInformationRequest(Message request, ConnectionHandler conn);
	
	/**
	 * Gets the status message of the robot's initialization procedure.
	 * 
	 * @return the status message
	 */
	public String getInitMessages();
	
	/**
	 * Resets the status of the robot, for instance, stops the wheels, turns off any active behavior.
	 */
	public void reset();
	
	/**
	 * Gets the list of MessageProviders. The MessageProviders are responsible for 
	 * replying to information requests. 
	 * 
	 * @return the list of MessageProviders
	 */
	public List<MessageProvider> getMessageProviders();
	
	/**
	 * Gets the current status message of the robot.
	 * 
	 * @return the status message
	 */
	public String getStatus();
	
	/**
	 * Gets the behavior that is currently controlling the robot.
	 * 
	 * @return the current behavior, or null if no behavior is active
	 */
	public CIBehavior getActiveBehavior();
	
	/**
	 * Starts a specific behavior.
	 * 
	 * @param the behavior that will control the robot
	 */
	public void startBehavior(CIBehavior b);
	
	/**
	 * Stops the currently active behavior.
	 */
	public void stopActiveBehavior();
	
	/**
	 * Gets the robot's logger.
	 * 
	 * @return the logger
	 */
	public RobotLogger getLogger();
	
}
