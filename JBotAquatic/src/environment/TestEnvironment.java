package environment;

import java.util.LinkedList;
import mathutils.Vector2d;
import commoninterface.AquaticDroneCI;
import commoninterface.objects.GeoFence;
import commoninterface.objects.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.util.Arguments;

public class TestEnvironment extends Environment {
	
	public TestEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		AquaticDroneCI drone = (AquaticDroneCI)robots.get(0);
		
		robots.get(0).setPosition(new Vector2d(0,0));
		robots.get(0).setOrientation(Math.toRadians(-45));
		
		GeoFence fence = new GeoFence("fence");
		
		fence.clear();
		
		int x = 30;
		int y = 20;
		int size = 40;
		
		addNode(fence,x,y);
		addNode(fence,x+size,y);
		addNode(fence,x+size,y-size);
		addNode(fence,x-size,y-size);
		
		addLines(fence.getWaypoints(), simulator);
		
		drone.getEntities().add(fence);
	}
	
	private void addNode(GeoFence fence, double x, double y) {
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(new commoninterface.mathutils.Vector2d(x, y)));
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

	@Override
	public void update(double time) {}
}
