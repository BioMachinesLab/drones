package fieldtests.updatables;

import commoninterface.AquaticDroneCI.DroneType;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FaultInjection implements Updatable {
	
	//falha (minutos): 30 20 10 5 3 2 1 0.5
	//tirar falha (minutos): 0.5
	
	private double faultEveryMinutes = 0;
	private double unfaultEveryMinutes = 0.5;
	
	private double faultVal = 0;
	private double unFaultVal = 0;
	
	public FaultInjection(Arguments args) {
		// TODO Auto-generated constructor stub
		
		faultEveryMinutes = args.getArgumentAsDoubleOrSetDefault("faulteveryminutes", faultEveryMinutes);
		unfaultEveryMinutes = args.getArgumentAsDoubleOrSetDefault("unfaulteveryminutes", unfaultEveryMinutes);
		
		faultVal = 1/(faultEveryMinutes*10*60);
		unFaultVal = 1/(unfaultEveryMinutes*10*60);
		
	}
	
	@Override
	public void update(Simulator simulator) {
		
		for(Robot r : simulator.getRobots()) {
			AquaticDrone ad = (AquaticDrone)r;
			if(ad.getDroneType() == DroneType.DRONE) {
				if(!ad.hasFault()) {
					if(simulator.getRandom().nextDouble() < faultVal) {
						ad.setEnabled(false);
						ad.setFault(true);
					}
				} else {
					if(simulator.getRandom().nextDouble() < unFaultVal) {
						ad.setEnabled(true);
						ad.setFault(false);
					}
				}
			}
		}
		
	}

}
