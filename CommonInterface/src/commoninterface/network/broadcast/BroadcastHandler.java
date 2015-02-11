package commoninterface.network.broadcast;

import java.util.ArrayList;
import java.util.Iterator;

import objects.DroneLocation;
import objects.Entity;
import commoninterface.AquaticDroneCI;

public abstract class BroadcastHandler {
	
	protected static boolean DEBUG = false;
	protected static int PORT = 8888;
	protected static long CLEAN_ENTITIES_TIME = 100; //100 timesteps == 10 seconds
	protected ArrayList<BroadcastMessage> broadcastMessages;
	
	protected AquaticDroneCI drone;
	
	public BroadcastHandler(AquaticDroneCI drone) {
		this.drone = drone;
		broadcastMessages = new ArrayList<BroadcastMessage>();
		broadcastMessages.add(new HeartbeatBroadcastMessage(drone));
		broadcastMessages.add(new PositionBroadcastMessage(drone));
	}
	
	public abstract void sendMessage(String message);
	
	public void messageReceived(String address, String message) {

		if(address.equals(drone.getNetworkAddress()))
			return;
		
		String identifier = message.split(BroadcastMessage.MESSAGE_SEPARATOR)[0];
		
		switch(identifier) {
			case HeartbeatBroadcastMessage.IDENTIFIER:
				//do nothing :D
				break;
			case PositionBroadcastMessage.IDENTIFIER:
				
				DroneLocation dl = PositionBroadcastMessage.decode(address, message);
				dl.setTimestepReceived((long)(drone.getTimeSinceStart()/10));
				drone.getEntities().remove(dl);
				drone.getEntities().add(dl);
				if(DEBUG)
					System.out.println("Added DroneLocation "+dl);
				break;
		}
	}
	
	public void update(double timestep) {
		cleanupEntities(timestep);
	}
	
	protected void cleanupEntities(double timestep) {
		Iterator<Entity> i = drone.getEntities().iterator();
		
		while(i.hasNext()) {
			Entity e = i.next();
			if(e instanceof DroneLocation) {
				if(timestep - e.getTimestepReceived() > CLEAN_ENTITIES_TIME) {
					i.remove();
					if(DEBUG)
						System.out.println("Removed Entity "+e);
				}
			}
		}
	}
	
	public AquaticDroneCI getDrone() {
		return drone;
	}
}