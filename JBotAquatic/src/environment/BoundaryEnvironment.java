package environment;

import java.util.LinkedList;
import java.util.Random;

import mathutils.Vector2d;
import commoninterface.AquaticDroneCI;
import commoninterface.objects.GeoFence;
import commoninterface.objects.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class BoundaryEnvironment extends Environment{
	
	@ArgumentsAnnotation(name="distance", defaultValue="5")
	private double distance = 5;
	@ArgumentsAnnotation(name="random", defaultValue="0.1")
	private double rand = 0.1;
	
	public BoundaryEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		
		distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
		rand = args.getArgumentAsDoubleOrSetDefault("random", rand);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		double dist = distance + distance*rand*simulator.getRandom().nextDouble()*2-rand;
		
		for(Robot r : simulator.getRobots()) {
			double x = dist*2*simulator.getRandom().nextDouble() - dist;
			double y = dist*2*simulator.getRandom().nextDouble() - dist;
			r.setPosition(new Vector2d(x, y));
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}
		
		AquaticDroneCI drone = (AquaticDroneCI)robots.get(0);		
		
		GeoFence fence = new GeoFence("fence");
		
		while(fence.getWaypoints().size() < 3) {
			
			fence.clear();
		
			addNode(fence,-1,-1,simulator.getRandom());
			addNode(fence,-1,0,simulator.getRandom());
			addNode(fence,-1,1,simulator.getRandom());
			addNode(fence,0,1,simulator.getRandom());
			addNode(fence,1,1,simulator.getRandom());
			addNode(fence,1,0,simulator.getRandom());
			addNode(fence,1,-1,simulator.getRandom());
			addNode(fence,0,-1,simulator.getRandom());
		
		}
		
		addLines(fence.getWaypoints(), simulator);
		
		drone.getEntities().add(fence);
	}
	
	private void addLines(LinkedList<Waypoint> waypoints, Simulator simulator) {
		
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
	
	private void addNode(GeoFence fence, double x, double y, Random r) {
		
		x*=distance;
		y*=distance;
		
		if(rand > 0) {
			x+= r.nextDouble() * distance;
			y+= r.nextDouble() * distance;
		}
		
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(new commoninterface.mathutils.Vector2d(x, y)));
	}

	@Override
	public void update(double time) {
		
	}

}
