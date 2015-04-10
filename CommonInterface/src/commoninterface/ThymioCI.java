package commoninterface;

import java.util.List;

import commoninterface.mathutils.Vector2d;


public interface ThymioCI extends RobotCI {
	
	/**
	 * Get the readings from the infrared sensors of the thymio robot.
	 * 
	 * @return the readings of the infrared sensors.
	 */
	public List<Short> getInfraredSensorsReadings();
	
	/**
	 * Get the virtual position of the thymio
	 * 
	 * @return virtual x and y of the thymio
	 */
	public Vector2d getVirtualPosition();
	
	/**
	 * Set the virtual position of the thymio
	 * 
	 * @param new virtual x and y of the thymio
	 */
	public void setVirtualPosition(double x, double y);
	
	/**
	 * Get the virtual orientation of the thymio
	 * 
	 * @return virtual orientation of the thmyio
	 */
	public Double getVirtualOrientation();
	
	/**
	 * Set the virtual orientation of the thymio
	 * 
	 * @param virtual orientation of the thymio
	 */
	public void setVirtualOrientation(double orientation);
}
