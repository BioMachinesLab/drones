
package simulation.robot;

import java.util.ArrayList;

import simulation.Network;
import commoninterface.RobotCI;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;

public class SimulatedBroadcastHandler extends BroadcastHandler {

	private SimulatedBroadcastMessageSender sender;

	public SimulatedBroadcastHandler(RobotCI drone, ArrayList<BroadcastMessage> broadcastMessages) {
		super(drone, broadcastMessages);
		sender = new SimulatedBroadcastMessageSender(this, this.broadcastMessages);
	}

	@Override
	public void sendMessage(String message) {
		AquaticDrone r = (AquaticDrone)drone;
		Network net = r.getSimulator().getNetwork();
		if(net != null)
			net.send(drone.getNetworkAddress(), message);
	}

	@Override
	public void update(double timestep) {
		super.update(timestep);
		sender.update(timestep);
	}
	

}
