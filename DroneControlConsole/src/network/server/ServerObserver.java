package network.server;

public interface ServerObserver {
	public void setOfflineServer();

	public void setOnlineServer();
	
	public void setMessage(String message);
	
	public void updateStatus();
}
