package drone;

import java.util.ArrayList;
import java.util.LinkedList;

import commoninterface.AquaticDroneCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.SharedDroneLocation;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import commoninterface.utils.jcoord.LatLon;

public class CompositeController extends CIBehavior {
	
	public enum State {
		GO_TO_AREA,DISPERSE,PATROL,AGGREGATE, GO_BACK
	}
	
	public enum Controller {
		AGGREGATE_WAYPOINT,DISPERSE,PATROL,AGGREGATE
	}
	
	protected long time[] = {2*60*10,1*60*10,3*60*10, 3*60*10, 2*60*10};
	
	protected CIArguments args;
	protected AquaticDroneCI drone;

	protected State currentState = State.GO_TO_AREA;
	protected int currentSubController = -1;
	protected double startTime = 0;
	protected boolean configured = false;
	private String description = "";
	
	protected ArrayList<CIBehavior> subControllers = new ArrayList<CIBehavior>();

	public CompositeController(CIArguments args, RobotCI robot) {
		super(args, robot);
		this.args = args;
		this.drone = (AquaticDroneCI)robot;
		
		if(args.getArgumentAsString("description") != null) {
			description+=" "+args.getArgumentAsString("description");
		}
	}
	
	@Override
	public void start() {
		for(Controller c : Controller.values()) {
			CIArguments a = new CIArguments(args.getArgumentAsString(c.toString()));
			ControllerCIBehavior cont = new ControllerCIBehavior(a, drone);
			subControllers.add(cont);
		}
		
		drone.setActiveWaypoint(null);
	}
	
	@Override
	public void step(double timestep) {
		
		int subController = 0;

		double elapsedTime = timestep - startTime;
		
		
		switch(currentState) {
			case GO_TO_AREA:
				
				subController = Controller.AGGREGATE_WAYPOINT.ordinal();
				
				if(!configured) {

					if(Waypoint.getWaypoints(drone).size() != 2) {
						System.out.println("The behavior expects 2 waypoints!");
						return;
					}
					drone.setActiveWaypoint(Waypoint.getWaypoints(drone).get(1));
					startTime = timestep;
					configured = true;
					elapsedTime = timestep - startTime;
				} 
				//TODO remove
				drone.setActiveWaypoint(Waypoint.getWaypoints(drone).get(1));
				if(elapsedTime > time[State.GO_TO_AREA.ordinal()]) {
					currentState = State.DISPERSE;
					configured = false;
				} 
				
				break;
			case DISPERSE:
				
				subController = Controller.DISPERSE.ordinal();
				
				if(!configured) {
					startTime = timestep;
					configured = true;
					elapsedTime = timestep - startTime;
				}
				
				
				if(elapsedTime > time[State.DISPERSE.ordinal()]) {
					currentState = State.PATROL;
					configured = false;
				}
				break;
			case PATROL:
				
				subController = Controller.PATROL.ordinal();
				
				if(!configured) {
					startTime = timestep;
					configured = true;
					elapsedTime = timestep - startTime;
				}
				
				if(elapsedTime > time[State.PATROL.ordinal()]) {
					currentState = State.AGGREGATE;
					configured = false;
				}
				
				break;
			case AGGREGATE:
				
				subController = Controller.AGGREGATE.ordinal();
				
				if(!configured) {
					startTime = timestep;
					configured = true;
					elapsedTime = timestep - startTime;
				}
				
				if(elapsedTime > time[State.AGGREGATE.ordinal()]) {
					currentState = State.GO_BACK;
					configured = false;
				}
				
				break;
			case GO_BACK:
				
				subController = Controller.AGGREGATE_WAYPOINT.ordinal();
				
				if(!configured) {
					drone.setActiveWaypoint(Waypoint.getWaypoints(drone).get(0));
					startTime = timestep;
					configured = true;
					elapsedTime = timestep - startTime;
				}
				
				break;
		}
		
		chooseSubController(subController,timestep);
		
	}
	
	protected void chooseSubController(int output, double time) {
		if(!subControllers.isEmpty()) {
			
			if(output != currentSubController) {
				if(currentSubController != -1)
					subControllers.get(currentSubController).cleanUp();
				currentSubController = output;
				subControllers.get(currentSubController).start();
			}
			
			subControllers.get(currentSubController).step(time);
			
		}
	}
	
	@Override
	public void cleanUp() {
		subControllers.get(currentSubController).cleanUp();
		robot.setMotorSpeeds(0, 0);
		subControllers.clear();
	}
	
	@Override
	public String toString() {
		return super.toString() + description;
	}
}