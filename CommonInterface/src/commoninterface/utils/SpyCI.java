package commoninterface.utils;

import java.util.Arrays;

import commoninterface.AquaticDroneCI;
import commoninterface.CILogger;

/**
 * 
 * Decorator pattern-based implementation of a call interceptor for the aquatic drone
 * common interface: all calls, arguments, and return values are logged.
 * 
 * 
 * @author alc
 */

public class SpyCI implements AquaticDroneCI {
	
	private AquaticDroneCI original; 
	private CILogger spyLogger;
	
	public SpyCI(AquaticDroneCI original, CILogger spyLogger) {
		this.original  = original;
		this.spyLogger = spyLogger;
	}
	
	@Override
	public void begin(String[] args, CILogger logger) {
		spyLogger.logMessage("start(" + Arrays.toString(args) + ", " + logger + ")");
		original.begin(args, logger);
	}

	@Override
	public void shutdown() {
		spyLogger.logMessage("shutdown()");
		original.shutdown();

	}

	@Override
	public void setMotorSpeeds(double leftMotor, double rightMotor) {
		spyLogger.logMessage("setMotorSpeeds(" + leftMotor + "," + rightMotor + ")");
		original.setMotorSpeeds(leftMotor, rightMotor);
	}

	@Override
	public double getCompassOrientationInDegrees() {
		double orientation = original.getCompassOrientationInDegrees();
		spyLogger.logMessage("getCompassOrientationInDegrees() returns " + orientation);
		return orientation;
	}

	@Override
	public double getGPSLatitude() {
		double latitude = original.getGPSLatitude();
		spyLogger.logMessage("getGPSLatitude() returns " + latitude);
		return latitude;
	}

	@Override
	public double getGPSLongitude() {
		double longitude = original.getGPSLongitude();
		spyLogger.logMessage("getGPSLongtitude() returns " + longitude);
		return longitude;
	}

	@Override
	public double getTimeSinceStart() {
		double time = original.getTimeSinceStart();
		spyLogger.logMessage("getTimeSinceStart() returns " + time);
		return time;
	}

	@Override
	public void setLed(int index, LedState state) {
		spyLogger.logMessage("setLed(" + index + "," + state + ")");
		original.setLed(index, state);
	}
}
