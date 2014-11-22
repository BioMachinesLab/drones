package commoninterface;

public class CIStdOutLogger implements CILogger 
{	
	private AquaticDroneCI droneCI;
	private String prefix = "";
	
	public CIStdOutLogger(AquaticDroneCI droneCI) {
		this.droneCI = droneCI;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public void logMessage(String message) {
		System.out.printf("%s %6.2s: %s\n", prefix, droneCI.getTimeSinceStart(), message);
	}

	@Override
	public void logError(String error) {
		System.err.printf("%s %6.2s: %s\n", prefix, droneCI.getTimeSinceStart(), error);
	}
	
}
