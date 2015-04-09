package commoninterface.utils;

public interface RobotLogger {
	
	public void stopLogging();

	public void logMessage(String string);

	public void logError(String string);

}
