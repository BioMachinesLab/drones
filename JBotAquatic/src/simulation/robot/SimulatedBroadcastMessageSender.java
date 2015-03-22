package simulation.robot;

import java.util.ArrayList;

import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;

public class SimulatedBroadcastMessageSender {
	
	private ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
	private BroadcastHandler broadcastHandler;
	private long[] timeLastMsg;
	
	public SimulatedBroadcastMessageSender(BroadcastHandler broadcastHandler, ArrayList<BroadcastMessage> broadcastMessages) {
		this.broadcastMessages = broadcastMessages;
		this.broadcastHandler = broadcastHandler;
		timeLastMsg = new long[broadcastMessages.size()];
	}
	
	public void update(double step) {
		
		long time = (long)(step*10);//from simulator units to real time units
				
		for(int i = 0 ; i < broadcastMessages.size() ; i++) {
			BroadcastMessage msg = broadcastMessages.get(i);
			
			long timeUntilMessage = time - timeLastMsg[i] - msg.getUpdateTimeInMiliseconds();
			
			if(timeUntilMessage >= 0) {
				broadcastHandler.sendMessage(msg.encode());
				timeLastMsg[i] = time;
			}
		}
	}
}