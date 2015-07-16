package evaluation.deprecated;

import java.util.ArrayList;

import commoninterface.entities.Entity;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
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
	@ArgumentsAnnotation(name="avoiddistance", defaultValue="0")
	private double avoidDistance = 0;
	@ArgumentsAnnotation(name="kill", defaultValue="0")
	private boolean kill = false;
	private double penalty = 0;
	private double sum = 0;

	public DroneWaypointEvaluationFunction(Arguments args) {
		super(args);
		targetDistance = args.getArgumentAsDoubleOrSetDefault("targetdistance", targetDistance);
		avoidDistance = args.getArgumentAsDoubleOrSetDefault("avoiddistance", avoidDistance);
		sum = args.getArgumentAsDoubleOrSetDefault("sum", sum);
		kill = args.getArgumentAsDoubleOrSetDefault("kill", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		AquaticDrone drone = (AquaticDrone)simulator.getRobots().get(0);
		boolean insideWP = false;
		
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
				insideWP = true;
				
				bonus+=(0.5/steps);
				
				if(drone.getLeftWheelSpeed() == 0 && drone.getRightWheelSpeed() == 0) {
					bonus+=(1.0/steps);
				}
				
				
			} else {
				fitness = (startingDistance-currentDistance)/startingDistance;
			}
		}
		
		if(!insideWP && avoidDistance > 0) {
			
			double highestPenalty = 0;
			
			for(int i = 1 ; i < simulator.getRobots().size() ; i++) {
				AquaticDrone other = (AquaticDrone)simulator.getRobots().get(i);
				
				double dist = CoordinateUtilities.distanceInMeters(drone.getGPSLatLon(), other.getGPSLatLon());
				
				if(dist < avoidDistance)
					highestPenalty= Math.max(1-(dist/avoidDistance),highestPenalty);
			}
			penalty-=highestPenalty/simulator.getEnvironment().getSteps()*5;
		}
		
		if(kill && !insideWP && drone.isInvolvedInCollison()) {
			simulator.stopSimulation();
			fitness = 0;
			bonus = 0;
			penalty = 0;
		}
	}
	
	@Override
	public double getFitness() {
		return fitness + bonus + (bonus > 0 ? penalty : 0) + sum;
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
