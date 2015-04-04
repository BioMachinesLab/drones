package evaluation;

import java.util.ArrayList;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import objects.Entity;
import objects.Waypoint;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class DroneWaypointEvaluationFunction extends EvaluationFunction{
	
	private boolean configured = false;
	private double startingDistance = 0;
	@ArgumentsAnnotation(name="targetdistance", defaultValue="0.5")
	private double targetDistance = 0.5;
	private Waypoint wp;
	private int steps = 0;
	private double bonus = 0;

	public DroneWaypointEvaluationFunction(Arguments args) {
		super(args);
		targetDistance = args.getArgumentAsDoubleOrSetDefault("targetdistance", targetDistance);
	}

	@Override
	public void update(Simulator simulator) {
		AquaticDrone drone = (AquaticDrone)simulator.getRobots().get(0);
		
		if(!configured) {
			steps = simulator.getEnvironment().getSteps();
			ArrayList<Waypoint> waypoints = Waypoint.getWaypoints(drone);
			
			if(!waypoints.isEmpty()) {
				wp = getWaypoint(drone);
				startingDistance = calculateDistance(wp,drone);
			}
			
			configured = true;
		}
		
		if(wp != null) {
			double currentDistance = calculateDistance(wp, drone);
			
			if(currentDistance < targetDistance) {
				fitness = 1.0;
				
				bonus+=(0.5/steps);
				
				if(drone.getLeftWheelSpeed() == 0 && drone.getRightWheelSpeed() == 0) {
					bonus+=(1.0/steps);
				}
				
			} else {
				fitness = (startingDistance-currentDistance)/startingDistance;
			}
		}
		
	}
	
	@Override
	public double getFitness() {
		return fitness + bonus;
	}
	
	private Waypoint getWaypoint(AquaticDrone drone) {
		ArrayList<Entity> entities = drone.getEntities();
		for(Entity e : entities) {
			if(e instanceof Waypoint)
				return (Waypoint)e;
		}
		return null;
	}
	
	private double calculateDistance(Waypoint wp, AquaticDrone drone) {
		Vector2d pos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
		Vector2d robotPos = new Vector2d(drone.getPosition().getX(), drone.getPosition().getY());
		return pos.distanceTo(robotPos);
	}
}
