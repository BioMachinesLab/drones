
package network;

import java.util.ArrayList;

import simulation.Network;
import simulation.robot.AquaticDrone;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import commoninterface.utils.CoordinateUtilities;

public class SimulatedBroadcastHandler extends BroadcastHandler {

	private SimulatedBroadcastMessageSender sender;

	public SimulatedBroadcastHandler(RobotCI drone, ArrayList<BroadcastMessage> broadcastMessages) {
		super(drone, broadcastMessages);
		sender = new SimulatedBroadcastMessageSender(this, this.broadcastMessages);
	}
	
	@Override
	public void messageReceived(String address, String message) {

		if(address.equals(robot.getNetworkAddress()))
			return;
		
		if(message.startsWith(PositionBroadcastMessage.IDENTIFIER)) {
				
			RobotLocation dl = PositionBroadcastMessage.decode(message);
			
			if(robot.getNetworkAddress().equals(dl.getName()))
				return;
			
			if(robot instanceof AquaticDrone) {
				AquaticDrone drone = (AquaticDrone)robot;
				if(drone.getGPSLatLon() == null || dl.getLatLon() == null)
					return;
				if(CoordinateUtilities.distanceInMeters(drone.getGPSLatLon(),dl.getLatLon()) > drone.getCommRange())
					robot.getEntities().remove(dl);
				else {
					replaceEntity(dl);
					if(DEBUG)
						System.out.println("Added DroneLocation "+dl);
				}
					
			} else {
				replaceEntity(dl);
				if(DEBUG)
					System.out.println("Added DroneLocation "+dl);
			}
				
		} else {
			super.messageReceived(address, message);
		}
	}
	
	private void replaceEntity(Entity e) {
		e.setTimestepReceived((long)(robot.getTimeSinceStart()*10));
		robot.replaceEntity(e);
	}

	@Override
	public void sendMessage(String message) {
		AquaticDrone r = (AquaticDrone)robot;
		Network net = r.getSimulator().getNetwork();
		if(net != null)
			net.send(robot.getNetworkAddress(), message);
	}

	@Override
	public void update(double timestep) {
		super.update(timestep);
		sender.update(timestep);
	}
	

}
