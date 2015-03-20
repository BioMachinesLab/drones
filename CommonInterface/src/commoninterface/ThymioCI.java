package commoninterface;

import java.util.List;


public interface ThymioCI extends RobotCI {
	
	/**
	 * Get the readings from the infrared sensors of the thymio robot.
	 * 
	 * @return the readings of the infrared sensors.
	 */
	public List<Short> getInfraredSensorsReadings();
	
}
