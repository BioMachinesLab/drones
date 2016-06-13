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
	 * Get the readings from the Raspberry Pi Camera
	 * 
	 * @return the readings of the picamera
	 */
	public double[] getCameraReadings();

	/**
	 * Get the virtual position of the thymio
	 * 
	 * @return virtual x and y of the thymio
	 */
	public Vector2d getVirtualPosition();

	/**
	 * Set the virtual position of the thymio
	 * 
	 * @param x
	 *            and y new position of the thymio
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
	 * @param orientation
	 *            of the thymio in virtual environment
	 */
	public void setVirtualOrientation(double orientation);

	/**
	 * Radius of the thymio
	 * 
	 * @return value of the radius of the thymio
	 */
	public double getThymioRadius();
}
