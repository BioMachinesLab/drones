package network;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import simulation.Network;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimulationNetwork extends Network {
	
	private double error = 0.0;
	
	public SimulationNetwork(Arguments args, Simulator sim) {
		super(args, sim);
		error = args.getArgumentAsDoubleOrSetDefault("error", error);
	}

	@Override
	public synchronized void send(String senderAddress, String msg) {
		for(Robot r : sim.getRobots()) {
			if(r instanceof AquaticDroneCI) {
				AquaticDroneCI aq = (AquaticDroneCI)r;
				
				if(sim.getRandom().nextDouble() >= error)
					aq.getBroadcastHandler().messageReceived(senderAddress, msg);
				
			}else if(r instanceof ThymioCI){
				ThymioCI thymio = (ThymioCI)r;
				
				if(sim.getRandom().nextDouble() >= error)
					thymio.getBroadcastHandler().messageReceived(senderAddress, msg);
			}
		}
	}

	@Override
	public void shutdown() {
		//nothing to do in this one
	}
}