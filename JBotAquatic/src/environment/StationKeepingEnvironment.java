package environment;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;

public class StationKeepingEnvironment extends Environment {
    
    public StationKeepingEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
    }
    
    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);
        
        AquaticDrone drone = (AquaticDrone) simulator.getRobots().get(0);
        double x = 0, y = 0;
        drone.setPosition(x, y);
        drone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
        Waypoint wp = new Waypoint("wp." + drone.getName(), CoordinateUtilities.cartesianToGPS(x, y));
        drone.getEntities().add(wp);
        drone.setActiveWaypoint(wp);
        LightPole lp = new LightPole(simulator, "wp." + drone.getName(), x, y, 5);
        addObject(lp);
    }
    
    @Override
    public void update(double time) {
        
    }
}
