package environment;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.GeoFence;
import controllers.GoToWayPointController;

public class EnemyEnvironment extends BoundaryEnvironment{
	
	public double enemyDistance = 30;
	
	public EnemyEnvironment(Simulator simulator, Arguments args) {
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
		
		double wd = wallsDistance;
		wallsDistance = enemyDistance;
		
		GeoFence fenceEnemy = getFence(simulator,true);
		
		wallsDistance = wd;
		
		this.fence = getFence(simulator,false);
		
		addLines(fence.getWaypoints(), simulator);
		
		for(Robot r : simulator.getRobots())
			((AquaticDrone)r).replaceEntity(fence);
		
//		addLines(fence.getWaypoints(), simulator);
		
		AquaticDrone drone = new AquaticDrone(simulator, new Arguments("diameter=2,gpserror=1.8,commrange=200,avoiddrones=0,rudder=0,changewaypoint=1"));
		drone.setDroneType(DroneType.ENEMY);
		GoToWayPointController controller = new GoToWayPointController(simulator, drone, new Arguments(""));
		drone.setController(controller);
		drone.getEntities().addAll(fenceEnemy.getWaypoints());
		addRobot(drone);
		drone.setActiveWaypoint(fenceEnemy.getWaypoints().get(0));
		
	}
	
	@Override
	public void update(double time) {
		
	}
	
	public GeoFence getFence(Simulator simulator, boolean middle) {
		GeoFence fence = new GeoFence("fence");
		
		addNode(fence,-1,-1,simulator.getRandom());
		addNode(fence,-1,0,simulator.getRandom());
		addNode(fence,-1,1,simulator.getRandom());
		addNode(fence,0,1,simulator.getRandom());
		if(middle)
			addNode(fence,0,0,simulator.getRandom());
		addNode(fence,1,1,simulator.getRandom());
		addNode(fence,1,0,simulator.getRandom());
		addNode(fence,1,-1,simulator.getRandom());
		addNode(fence,0,-1,simulator.getRandom());
		
		return fence;
	
	}
}