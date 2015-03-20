package commoninterface;

public class CIStdOutLogger implements CILogger 
{	
	private RobotCI droneCI;
	
	public CIStdOutLogger(RobotCI droneCI) {
		this.droneCI = droneCI;
	}
	
	@Override
	public void logMessage(String message) {
		System.out.printf("%6.2s: %s\n", droneCI.getTimeSinceStart(), message);
	}

	@Override
	public void logError(String error) {
		System.err.printf("%6.2s: %s\n", droneCI.getTimeSinceStart(), error);
	}
	
	@Override
	public void stopLogging() {
		System.out.println("Logger stopping");
	}
	
}
