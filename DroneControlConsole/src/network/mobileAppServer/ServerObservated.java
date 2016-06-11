package network.mobileAppServer;


public interface ServerObservated {
	public void startServer();

	public void startServer(int port);

	public void stopServer();

	public void setObserver(ServerObserver observer);
}
