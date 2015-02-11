package network.broadcast;

import commoninterface.AquaticDroneCI;
import commoninterface.network.broadcast.BroadcastHandler;

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
		sender = new BroadcastSender(drone.getNetworkAddress(), PORT);
		receiver.start();
		
		initBroadcastThread();
	}
	
	private void initBroadcastThread() {
		broadcastStatusThread = new BroadcastMessageThread(this, broadcastMessages);
		broadcastStatusThread.start();
	}
	
	@Override
	public void sendMessage(String message) {
		if(DEBUG) {
			System.out.println("RealBroadcastHandler sent ["+message+"]");
		}
		sender.sendMessage(message);
	}
	
	@Override
	public void messageReceived(String address, String message) {
		super.messageReceived(address, message);
		if(DEBUG)
			System.out.println("RealBroadcastHandler received ["+message+"]");
	}
	
	public AquaticDroneCI getDrone() {
		return drone;
	}	
}