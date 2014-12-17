package network.broadcast;

public abstract class BroadcastStatusThread extends Thread{
	
	public static final String MESSAGE_SEPARATOR = ";";
	
	private BroadcastHandler broadcastHandler;
	private String identifier;
	private long waitTime = 10*1000; //10 sec by default
	
	public BroadcastStatusThread(BroadcastHandler broadcastHandler, String identifier, long waitTime) {
		this.broadcastHandler = broadcastHandler;
		this.identifier = identifier;
		this.waitTime = waitTime;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				broadcastHandler.sendMessage(identifier+MESSAGE_SEPARATOR+createMessage());
				Thread.sleep(waitTime);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public abstract String createMessage();
	
}
