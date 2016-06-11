package utils.GPSTimeProvider;

public interface GPSTimeProviderServerObserver {
	public void setOfflineServer();

	public void setOnlineServer();
	
	public void setOnlineGPSModule();
	
	public void setOfflineGPSModule();

	public void setMessage(String message);

	public void setErrorMessage(String message);

	public void updateStatus();
}
