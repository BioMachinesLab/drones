package environment;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.RobotCI;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import controllers.GoToWayPointController;

public class MissionEnvironment extends BoundaryEnvironment{
	
	public double enemyDistance = 30;
	
	public MissionEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.enemyDistance = args.getArgumentAsDoubleOrSetDefault("enemydistance", enemyDistance);
	}
	
	@Override
	public void setup(Simulator simulator) {
		
		this.setup = true;
		
		for (Robot r : simulator.getRobots()) {
        	do{
        		positionDrone((AquaticDrone) r, simulator);
        		simulator.updatePositions(0);
        	}while(!safe(r, simulator));
        	
        }
		
		GeoFence fenceDrones = new GeoFence("fence");
		
		int size = 100;
		
		commoninterface.mathutils.Vector2d vec = CoordinateUtilities.GPSToCartesian(new LatLon(38.766524638824215, -9.094010382164727));
		fenceDrones.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x+=size;
		fenceDrones.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.y-=size;
		fenceDrones.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x-=size;
		fenceDrones.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		
		//base
		vec.y+= (size+size/4);
		vec.x+= size/2;
		
		for(Robot r : simulator.getRobots()) {
			((RobotCI)r).replaceEntity(fenceDrones);
			((RobotCI)r).replaceEntity(new Waypoint("base", CoordinateUtilities.cartesianToGPS(vec)));
			
			double rand = simulator.getRandom().nextDouble()*5;//5 m
			
			double x = vec.x + rand*2 - rand;
			double y = vec.y + rand*2 - rand;
			
			r.setPosition(x, y);
			r.setOrientation(Math.PI*2*simulator.getRandom().nextDouble());
		}
		
		wallsDistance = enemyDistance;
		GeoFence fenceEnemy = getFence(simulator);
		
		AquaticDrone drone = new AquaticDrone(simulator, new Arguments("diameter=2,gpserror=1.8,commrange=40,avoiddrones=0"));
		drone.setDroneType(DroneType.ENEMY);
		GoToWayPointController controller = new GoToWayPointController(simulator, drone, new Arguments("wait=300"));
		drone.setController(controller);
		drone.getEntities().addAll(fenceEnemy.getWaypoints());
		drone.setPosition(new Vector2d(-2*wallsDistance, 1*wallsDistance));
		addRobot(drone);
		drone.setActiveWaypoint(fenceEnemy.getWaypoints().get(0));
	}
	
	@Override
	public void update(double time) {
		
	}
	
	public GeoFence getFence(Simulator simulator) {
		GeoFence fence = new GeoFence("fence");
		
		rand = 0;
		
		addNode(fence,-2,1,simulator.getRandom());
		addNode(fence,2,1,simulator.getRandom());
		addNode(fence,2,0.5,simulator.getRandom());
		addNode(fence,-2,0.5,simulator.getRandom());
		addNode(fence,-2,0,simulator.getRandom());
		addNode(fence,2,0,simulator.getRandom());
		addNode(fence,2,-0.5,simulator.getRandom());
		addNode(fence,-2,-0.5,simulator.getRandom());
		addNode(fence,-2,-1,simulator.getRandom());
		addNode(fence,2,-1,simulator.getRandom());
		addNode(fence,2,-1.5,simulator.getRandom());
		addNode(fence,-2,-1.5,simulator.getRandom());
		
		return fence;
	
	}
}