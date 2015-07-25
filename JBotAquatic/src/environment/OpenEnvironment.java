package environment;


import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class OpenEnvironment extends Environment {

    @ArgumentsAnnotation(name = "distance", defaultValue = "0")
    private double distance = 0;
    private double safetyDistance = 0;

    public OpenEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        safetyDistance = args.getArgumentAsDoubleOrSetDefault("safetydistance", safetyDistance);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);
        for (Robot r : simulator.getRobots()) {
            
        	do{
        		positionDrone((AquaticDrone) r, simulator);
        		simulator.updatePositions(0);
        	}while(!safe(r, simulator));
        	
        }
        this.setup = true;
    }
    
    protected boolean safe(Robot r, Simulator simulator) {
    	
    	if(r.isInvolvedInCollison())
    		return false;
    	
    	if(safetyDistance > 0) {
    		for(Robot robot : robots) {
    			
    			if(robot.getId() == r.getId()) {
    				//all robots with a lower ID are at safe distances, so we can exit
    				return true;
    			}
    			
   				if(robot.getPosition().distanceTo(r.getPosition()) < safetyDistance)
   					return false;
    		}
    	}
    	
    	return true;
    }
    
    protected void positionDrone(AquaticDrone drone, Simulator simulator) {
    	
    	double x = distance*2*simulator.getRandom().nextDouble() - distance;
    	double y = distance*2*simulator.getRandom().nextDouble() - distance;
        drone.setPosition(x, y);
        drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
    }

    @Override
    public void update(double time) {

    }
}
