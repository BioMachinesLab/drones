package commoninterface.network.broadcast;

import java.util.ArrayList;
import objects.DroneLocation;
import commoninterface.AquaticDroneCI;

public abstract class BroadcastHandler {
	
	protected static int PORT = 8888;
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
		switch(message) {
			case HeartbeatBroadcastMessage.IDENTIFIER:
				//do nothing :D
				break;
			case PositionBroadcastMessage.IDENTIFIER:
				DroneLocation dl = PositionBroadcastMessage.decode(address, message);
				drone.getEntities().remove(dl);
				drone.getEntities().add(dl);
				break;
		}
	}
	
	public AquaticDroneCI getDrone() {
		return drone;
	}

}
