package environment;

import objects.Waypoint;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import commoninterface.AquaticDroneCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class WaypointEnvironment extends Environment{
	
	@ArgumentsAnnotation(name="numberwaypoints", defaultValue="1")
	private int nWaypoints = 1;
	@ArgumentsAnnotation(name="distance", defaultValue="0")
	private double distance = 0;
	@ArgumentsAnnotation(name="random", defaultValue="0")
	private double rand = 0;

	public WaypointEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		nWaypoints = args.getArgumentAsIntOrSetDefault("numberwaypoints", nWaypoints);
		distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
		rand = args.getArgumentAsDoubleOrSetDefault("random", rand);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		for(Robot r : simulator.getRobots())
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		
		distance+= distance*rand*simulator.getRandom().nextDouble()*2-rand;
		
		for(int i = 0 ; i < nWaypoints ; i++) {
			double x = distance > 0 ? distance : width/2/3;
			double y = 0;
			LatLon latLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x,y));
			Waypoint wp = new Waypoint("wp"+i, latLon);
			for(Robot r : simulator.getRobots())
				((AquaticDroneCI)r).getEntities().add(wp);
			LightPole lp = new LightPole(simulator, "wp"+i, x, y, 1.5);
			addObject(lp);
		}
	}

	@Override
	public void update(double time) {
		
	}
}