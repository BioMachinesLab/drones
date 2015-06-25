package commoninterface.sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;

/**
 * Based on the WallRaySensor from JBotEvolver
 * 
 * @author md
 */
public class GeoFenceCISensor extends ConeTypeCISensor{
	
	protected int numberOfRays = 7;
	protected double[][] rayReadings;
	public Vector2d[][] cones;
	private ArrayList<Line> lines = new ArrayList<Line>();
	private Vector2d robotPosition;
	private GeoFence fence;

	public GeoFenceCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		
		numberOfRays = args.getArgumentAsIntOrSetDefault("numberofrays", numberOfRays);
		
		if(numberOfRays%2 == 0)
			numberOfRays++;
		
		rayReadings = new double[numberSensors][numberOfRays];
		
		cones = new Vector2d[numberSensors][numberOfRays];
		for(int i = 0 ; i < numberSensors ; i++) {
			for(int j = 0 ; j < numberOfRays ; j++)
				cones[i][j] = new Vector2d();
		}
		
	}
	
	@Override
	public void update(double time, Object[] entities) {
		updateCones();
		
		updateLines(time, entities);
		
		try { 
			for(int i = 0; i < numberSensors; i++){
				for(int j = 0; j < numberOfRays; j++){
					rayReadings[i][j] = 0.0;
				}
				readings[i] = 0.0;
			}
			
			for(Line l : lines) {
				for(int j=0; j<numberSensors; j++){
					calculateContributionToSensor(j, l);
				}
			}
			
			for(int i = 0; i < numberSensors; i++){
				double avg = 0;
				for(int ray = 0 ; ray < numberOfRays ; ray++) {
					avg+= rayReadings[i][ray]/numberOfRays;
				}
				readings[i]=avg;
			}
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	private void updateCones() {
		
		try {
			
			for(int sensorNumber = 0 ; sensorNumber < numberSensors ; sensorNumber++) {
				
				//We have to subtract 90 degrees and negate because the math functions were made
				//for JBotEvolver, in which the reference orientation is to the east, not the north,
				//and the orientation increases counter-clockwise instead of clockwise
				double orientation = - (angles[sensorNumber] + Math.toRadians(drone.getCompassOrientationInDegrees()) - Math.PI/2);
				
				robotPosition = CoordinateUtilities.GPSToCartesian(drone.getGPSLatLon());
				
				double alpha = (this.openingAngle)/(numberOfRays-1);
				
				double halfOpening = openingAngle/2.0;
				
				for(int i = 0 ; i < numberOfRays ; i++) {
					cones[sensorNumber][i].set(
							Math.cos(orientation - halfOpening + alpha*i)* range + robotPosition.getX(),
							Math.sin(orientation - halfOpening + alpha*i)* range + robotPosition.getY()
						 );
				}
			}
		}catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	protected double calculateContributionToSensor(int sensorNumber, Line l) {
			
		double inputValue = 0;
		
		for(int i = 0 ; i < numberOfRays ; i++) {
			Vector2d cone = cones[sensorNumber][i];
			
			Vector2d intersection = null;
			intersection = l.intersectsWithLineSegment(robotPosition, cone);
			
			if(intersection != null) {
				
				double distance = intersection.distanceTo(robotPosition);
				cone.angle(intersection);
				
				if(distance < range) {
					inputValue = (range-distance)/range;
					
					if(inputValue > rayReadings[sensorNumber][i]) {
						rayReadings[sensorNumber][i] = Math.max(inputValue, rayReadings[sensorNumber][i]);
					}
				}
			}
		}
		return inputValue;
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

	@Override
	public boolean validEntity(Entity e) {
		if(e instanceof GeoFence)
			return true;
		return false;
	}	
}