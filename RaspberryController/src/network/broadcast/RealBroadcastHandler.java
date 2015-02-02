package network.broadcast;

import commoninterface.AquaticDroneCI;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;

/**
 * This class handles everything related to receiving and sending broadcast messages.
 * New types of messages can be periodically sent through BroadcastStatusThreads.
 * 
 * @author miguelduarte42
 */
public class RealBroadcastHandler extends BroadcastHandler {
	
	private BroadcastMessageThread broadcastStatusThread = null;
	
	private BroadcastReceiver receiver;
	private BroadcastSender sender;
	
	public RealBroadcastHandler(AquaticDroneCI drone) {
		super(drone);
		receiver = new BroadcastReceiver(this, drone.getNetworkAddress(), PORT);
		sender = new BroadcastSender(drone.getNetworkAddress(), PORT+1);
		receiver.start();
		
		initBroadcastThread();
	}
	
	private void initBroadcastThread() {
		
		broadcastStatusThread = new BroadcastMessageThread(this, broadcastMessages);
		broadcastStatusThread.start();
	}
	
	@Override
	public void sendMessage(String message) {
		sender.sendMessage(message);
	}
	
	@Override
	public void messageReceived(String address, String message) {
		//TODO possibly retransmit. We also have to react to certain messages,
		//such as starting behaviors, for instance.
		
		//System.out.println("Received "+message+" from "+address);
	}
	
	public AquaticDroneCI getDrone() {
		return drone;
	}	
}