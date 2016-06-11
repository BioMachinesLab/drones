package utils.GPSTimeProvider;

import commoninterface.dataobjects.GPSData;

public interface GPSTimeProviderServerObservated {
	public void startServer();

	public void startServer(int port);

	public void stopServer();

	public void startGPSModule();

	public void startGPSModule(String port);

	public void stopGPSModule();

	public String[] getSerialPortIdentifiers();

	public void setObserver(GPSTimeProviderServerObserver observer);

	public boolean isServerRunning();

	public boolean isGPSModuleRunning();

	public int getDefaultPort();

	public GPSData getGPSData();

	public int getConnectedClientsQuantity();
}
