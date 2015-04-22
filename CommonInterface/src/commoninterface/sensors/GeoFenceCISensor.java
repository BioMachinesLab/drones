package commoninterface.sensors;

import java.util.ArrayList;
import java.util.LinkedList;
import commoninterface.RobotCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.objects.Entity;
import commoninterface.objects.GeoFence;
import commoninterface.objects.Waypoint;
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
	protected Vector2d[][] cones;
	protected Vector2d[] sensorPositions;
	private ArrayList<Line> lines = new ArrayList<Line>();

	public GeoFenceCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		
		
		numberOfRays = args.getArgumentAsIntOrSetDefault("numberofrays", numberOfRays);
		
		if(numberOfRays%2 == 0)
			numberOfRays++;
		
		rayReadings = new double[numberSensors][numberOfRays];
		
		sensorPositions = new Vector2d[numberSensors];
		cones = new Vector2d[numberSensors][numberOfRays];
		for(int i = 0 ; i < numberSensors ; i++) {
			sensorPositions[i] = new Vector2d();
			for(int j = 0 ; j < numberOfRays ; j++)
				cones[i][j] = new Vector2d();
		}
	}
	
	@Override
	public void update(double time, ArrayList<Entity> entities) {
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
				calculateSourceContributions(l);
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
				double orientation = angles[sensorNumber] + Math.toRadians(drone.getCompassOrientationInDegrees());
				
				Vector2d robotPosition = CoordinateUtilities.GPSToCartesian(drone.getGPSLatLon());
				
				sensorPositions[sensorNumber].set(
						Math.cos(orientation) + robotPosition.getX(),
						Math.sin(orientation) + robotPosition.getY()
					);
				
				double alpha = (this.openingAngle)/(numberOfRays-1);
				
				double halfOpening = openingAngle/2.0;
				
				for(int i = 0 ; i < numberOfRays ; i++) {
					//the multiplication by 5 is necessary because of the close/far objects estimation
					//the number 5 is arbitrary
					cones[sensorNumber][i].set(
							Math.cos(orientation - halfOpening + alpha*i)* range*5 + sensorPositions[sensorNumber].getX(),
							Math.sin(orientation - halfOpening + alpha*i)* range*5 + sensorPositions[sensorNumber].getY()
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
			intersection = l.intersectsWithLineSegment(sensorPositions[sensorNumber], cone);
			
			if(intersection != null) {
				double distance = intersection.distanceTo(sensorPositions[sensorNumber]);
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
	
	protected void calculateSourceContributions(Line source) {
		for(int j=0; j<numberSensors; j++){
			if(openingAngle > 0.018){ //1degree
				calculateContributionToSensor(j, source);
			}
		}
	}
	
	private void updateLines(double time, ArrayList<Entity> entities) {
		
		GeoFence fence = null;
		
		for(Entity e : entities) {
			if(e instanceof GeoFence) {
				fence = (GeoFence)e;
				break;
			}
		}
		
		if(fence == null) {
			lines.clear();
		} else {
			LinkedList<Waypoint> waypoints = fence.getWaypoints();
			
			//force this every 100 seconds just to be on the safe side
			if(waypoints.size() != lines.size() || (time % 1000) == 0) {
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
