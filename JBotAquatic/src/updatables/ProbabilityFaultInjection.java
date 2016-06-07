package updatables;

import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ProbabilityFaultInjection implements Updatable{
	
	private int interval = 1000;
	private double probability;
	private double deadrobots = 10;
	
	public ProbabilityFaultInjection(Arguments args) {
		interval = args.getArgumentAsIntOrSetDefault("interval", interval);
		deadrobots = args.getArgumentAsDoubleOrSetDefault("deadrobots", deadrobots);
		double nRobots = args.getArgumentAsDoubleOrSetDefault("nrobots", 50);
		probability = 1.0/((144000.0/1000.0)*nRobots);
		probability*=deadrobots;
	}
	
	@Override
	public void update(Simulator simulator) {

		if(simulator.getTime() % interval == 0) {
			for(Robot r : simulator.getRobots()) {
				if(r.isEnabled()) {
					if(simulator.getRandom().nextDouble() < probability) {
						r.setEnabled(false);
					}
				}
			}
		}
	}
}