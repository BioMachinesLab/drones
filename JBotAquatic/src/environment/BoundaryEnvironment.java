package environment;

import java.util.LinkedList;
import java.util.Random;

import mathutils.Vector2d;
import commoninterface.AquaticDroneCI;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class BoundaryEnvironment extends Environment{
	
	@ArgumentsAnnotation(name="distance", defaultValue="5")
	protected double distance = 5;
	@ArgumentsAnnotation(name="random", defaultValue="0.1")
	protected double rand = 0.5;
	
	public BoundaryEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		
		distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
		rand = args.getArgumentAsDoubleOrSetDefault("random", rand);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		double dist = distance + distance*rand;
		
		GeoFence fence = new GeoFence("fence");
		
		addNode(fence,-1,-1,simulator.getRandom());
		addNode(fence,-1,0,simulator.getRandom());
		addNode(fence,-1,1,simulator.getRandom());
		addNode(fence,0,1,simulator.getRandom());
		addNode(fence,1,1,simulator.getRandom());
		addNode(fence,1,0,simulator.getRandom());
		addNode(fence,1,-1,simulator.getRandom());
		addNode(fence,0,-1,simulator.getRandom());
		
		addLines(fence.getWaypoints(), simulator);
		
		for(Robot r : simulator.getRobots()) {
			
			for(int i = 0 ; i < 100 ; i++) {
				double x = (dist*2*simulator.getRandom().nextDouble() - dist)*0.5;
				double y = (dist*2*simulator.getRandom().nextDouble() - dist)*0.5;
				
				if(insideLines(new Vector2d(x,y), simulator)){
					r.setPosition(new Vector2d(x, y));
					break;
				}
			}
			
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}
		
		for(Robot r : robots) {
			AquaticDroneCI drone = (AquaticDroneCI)r;
			drone.getEntities().add(fence);
		}
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
	
	public boolean insideLines(Vector2d v, Simulator sim) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		for(PhysicalObject p : sim.getEnvironment().getAllObjects()) {
			if(p.getType() == PhysicalObjectType.LINE) {
				Line l = (Line)p;
				if(l.intersectsWithLineSegment(v, new Vector2d(0,-Integer.MAX_VALUE)) != null)
					count++;
			}
		}
		return count % 2 != 0;
	}

	@Override
	public void update(double time) {
		
	}

}
