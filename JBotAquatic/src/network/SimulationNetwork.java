package network;

import commoninterface.AquaticDroneCI;
import simulation.Network;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimulationNetwork extends Network {
	
	public SimulationNetwork(Arguments args, Simulator sim) {
		super(args, sim);
	}

	@Override
	public void send(String senderAddress, String msg) {
		for(Robot r : sim.getRobots()) {
			if(r instanceof AquaticDroneCI) {
				AquaticDroneCI aq = (AquaticDroneCI)r;
				aq.getBroadcastHandler().messageReceived(senderAddress, msg);
			}
		}
	}

	@Override
	public void shutdown() {
		//nothing to do in this one
	}
}