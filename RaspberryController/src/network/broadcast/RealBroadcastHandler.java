package network.broadcast;

import java.util.ArrayList;

import commoninterface.RobotCI;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;

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
	
	public RealBroadcastHandler(RobotCI robot, ArrayList<BroadcastMessage> broadcastMessages) {
		super(robot, broadcastMessages);
		receiver = new BroadcastReceiver(this, robot.getNetworkAddress(), PORT);
		sender = new BroadcastSender(robot.getNetworkAddress(), PORT);
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
	
	public RobotCI getDrone() {
		return robot;
	}	
}