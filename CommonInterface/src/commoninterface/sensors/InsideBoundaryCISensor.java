package commoninterface.sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;

public class InsideBoundaryCISensor extends CISensor{
	
	private AquaticDroneCI drone;
	private ArrayList<Line> lines = new ArrayList<Line>();
	private double reading = 0;
	private boolean reverse = false;
	private GeoFence fence;

	public InsideBoundaryCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		drone = (AquaticDroneCI)robot;
		reverse = args.getFlagIsTrue("reverse");
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return reverse ? 1 - reading : reading;
	}

	@Override
	public void update(double time, Object[] entities) {
		
		updateLines(time, entities);
		
		if(lines.isEmpty())
			reading = 0;
		else
			reading = insideBoundary() ? 1 : 0;
		
	}
	
	@Override
	public int getNumberOfSensors() {
		return 1;
	}
	
	private void updateLines(double time, Object[] entities) {
		
		GeoFence fence = null;
		
		for(Object e : entities) {
			if(e instanceof GeoFence) {
				fence = (GeoFence)e;
				break;
			}
		}
		
		if(fence == null) {
			lines.clear();
		} else {
			
			if(this.fence != null && fence.getTimestepReceived() == this.fence.getTimestepReceived()) {
				return;
			}
			
			lines.clear();
			
			this.fence = fence;
			
			LinkedList<Waypoint> waypoints = fence.getWaypoints();
			
			//force this every 100 seconds just to be on the safe side
			if(waypoints.size() != lines.size() || (time % 1000) == 0) {
				
				lines.clear();
				
				for(int i = 1 ; i < waypoints.size() ; i++) {
					
					Waypoint wa = waypoints.get(i-1);
					Waypoint wb = waypoints.get(i);
					
					addLine(wa,wb);
				}
				
				//loop around
				Waypoint wa = waypoints.get(waypoints.size()-1);
				Waypoint wb = waypoints.get(0);
				
				addLine(wa,wb);
			}
		}
	}
	
	private void addLine(Waypoint wa, Waypoint wb) {
		Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());
		
		Line l = new Line(va.getX(), va.getY(), vb.getX(), vb.getY());
		lines.add(l);
	}
	
	private boolean insideBoundary() {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		
		if(drone.getGPSLatLon() != null) {
			Vector2d dronePosition = CoordinateUtilities.GPSToCartesian(drone.getGPSLatLon());
			
			for(Line l : lines) {
				if(l.intersectsWithLineSegment(dronePosition, new Vector2d(0,-Integer.MAX_VALUE)) != null)
					count++;
			}
			return count % 2 != 0;
		}
		return false;
	}
}
