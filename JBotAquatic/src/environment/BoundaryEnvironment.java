package environment;

import java.util.LinkedList;
import java.util.Random;
import mathutils.Vector2d;
import commoninterface.objects.GeoFence;
import commoninterface.objects.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class BoundaryEnvironment extends Environment{
	
	@ArgumentsAnnotation(name="distance", defaultValue="5")
	protected double distance = 5;
	@ArgumentsAnnotation(name="random", defaultValue="0.1")
	protected double rand = 0.5;
	@ArgumentsAnnotation(name="multi", defaultValue="0")
	protected int multi = 0;
	
	public BoundaryEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		
		distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
		rand = args.getArgumentAsDoubleOrSetDefault("random", rand);
		multi = args.getArgumentAsIntOrSetDefault("multi", multi);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		double dist = distance + distance*rand;
		
		for(Robot r : simulator.getRobots()) {
			double x = dist*2*simulator.getRandom().nextDouble() - dist;
			double y = dist*2*simulator.getRandom().nextDouble() - dist;
			r.setPosition(new Vector2d(x, y));
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}
		
		
//		GeoFence fence = new GeoFence("fence");
//		
//		addNode(fence,-1,-1,simulator.getRandom());
//		addNode(fence,-1,0,simulator.getRandom());
//		addNode(fence,-1,1,simulator.getRandom());
//		addNode(fence,0,1,simulator.getRandom());
//		addNode(fence,1,1,simulator.getRandom());
//		addNode(fence,1,0,simulator.getRandom());
//		addNode(fence,1,-1,simulator.getRandom());
//		addNode(fence,0,-1,simulator.getRandom());
//		
//		addLines(fence.getWaypoints(), simulator);
//		
//		for(Robot r : robots) {
//			AquaticDroneCI drone = (AquaticDroneCI)r;
//			drone.getEntities().add(fence);
//		}
	}
	
	protected void addLines(LinkedList<Waypoint> waypoints, Simulator simulator) {
		
		for(int i = 1 ; i < waypoints.size() ; i++) {
			
			Waypoint wa = waypoints.get(i-1);
			Waypoint wb = waypoints.get(i);
			commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
			commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());
			
			simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator,"line"+i,va.getX(),va.getY(),vb.getX(),vb.getY());
			addObject(l);
		}
		
		Waypoint wa = waypoints.get(waypoints.size()-1);
		Waypoint wb = waypoints.get(0);
		commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());
		
		simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator,"line0",va.getX(),va.getY(),vb.getX(),vb.getY());
		addObject(l);
	}
	
	protected void addNode(GeoFence fence, double x, double y, Random r) {
		
		x*=distance;
		y*=distance;
		
		if(rand > 0) {
			x+= r.nextDouble() * rand * distance * 2 - rand*distance;
			y+= r.nextDouble() * rand * distance * 2 - rand*distance;
		}
		
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(new commoninterface.mathutils.Vector2d(x, y)));
	}

	@Override
	public void update(double time) {
		
	}

}
