package commoninterface.network.broadcast;

import java.util.ArrayList;
import java.util.Iterator;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import commoninterface.objects.Entity;
import commoninterface.objects.RobotLocation;

public abstract class BroadcastHandler {
	
	protected static boolean DEBUG = false;
	protected static int PORT = 8888;
	protected static long CLEAN_ENTITIES_TIME = 100; //100 timesteps == 10 seconds
	protected ArrayList<BroadcastMessage> broadcastMessages;
	
	protected RobotCI robot;
	
	public BroadcastHandler(RobotCI drone, ArrayList<BroadcastMessage> broadcastMessages) {
		this.robot = drone;
		this.broadcastMessages = broadcastMessages;
	}
	
	public abstract void sendMessage(String message);
	
	public void messageReceived(String address, String message) {

		if(address.equals(robot.getNetworkAddress()))
			return;
		
		String identifier = message.split(BroadcastMessage.MESSAGE_SEPARATOR)[0];
		
		switch(identifier) {
			case HeartbeatBroadcastMessage.IDENTIFIER:
				//do nothing :D
				break;
			case PositionBroadcastMessage.IDENTIFIER:
				
				RobotLocation dl = PositionBroadcastMessage.decode(address, message);
				dl.setTimestepReceived((long)(robot.getTimeSinceStart()/10));
				
				synchronized(robot.getEntities()){
					robot.getEntities().remove(dl);
					robot.getEntities().add(dl);
				}
				
				if(DEBUG)
					System.out.println("Added DroneLocation "+dl);
				break;
			case VirtualPositionBroadcastMessage.IDENTIFIER:
				VirtualPositionBroadcastMessage.decode(address, message, (ThymioCI)robot);
				break;
		}
	}
	
	public void update(double timestep) {
		cleanupEntities(timestep);
	}
	
	protected void cleanupEntities(double timestep) {
		synchronized(robot.getEntities()) {
		
			Iterator<Entity> i = robot.getEntities().iterator();
			
			while(i.hasNext()) {
				Entity e = i.next();
				if(e instanceof RobotLocation) {
					if(timestep - e.getTimestepReceived() > CLEAN_ENTITIES_TIME) {
						i.remove();
						if(DEBUG)
							System.out.println("Removed Entity "+e);
					}
				}
			}
		}
	}
	
	public RobotCI getDrone() {
		return robot;
	}
}