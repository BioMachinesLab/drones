package commoninterface;

public interface CILogger {
	public void logMessage(String message);
	public void logError(String error);
	public void stopLogging();
}
