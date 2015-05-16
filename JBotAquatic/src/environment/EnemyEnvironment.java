package environment;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.objects.GeoFence;
import controllers.GoToWayPointController;

public class EnemyEnvironment extends BoundaryEnvironment{
	
	public EnemyEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
	}
	
	@Override
	public void setup(Simulator simulator) {
		simulator.getRandom().nextDouble();
		this.setup = true;
		
		double dist = distance + distance*rand;
		
		for(Robot r : simulator.getRobots()) {
			double x = dist*2*simulator.getRandom().nextDouble() - dist;
			double y = dist*2*simulator.getRandom().nextDouble() - dist;
			r.setPosition(new Vector2d(x, y));
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}
		
		
		GeoFence fence = new GeoFence("fence");
		
		addNode(fence,-1,-1,simulator.getRandom());
		addNode(fence,-1,0,simulator.getRandom());
		addNode(fence,-1,1,simulator.getRandom());
		addNode(fence,0,1,simulator.getRandom());
		addNode(fence,1,1,simulator.getRandom());
		addNode(fence,1,0,simulator.getRandom());
		addNode(fence,1,-1,simulator.getRandom());
		addNode(fence,0,-1,simulator.getRandom());
		
//		addLines(fence.getWaypoints(), simulator);
		
		AquaticDrone drone = new AquaticDrone(simulator, new Arguments("diameter=3.2,gpserror=1.0,commrange=50,avoiddrones=0"));
		drone.setDroneType(DroneType.ENEMY);
		GoToWayPointController controller = new GoToWayPointController(simulator, drone, new Arguments(""));
		drone.setController(controller);
		drone.getEntities().addAll(fence.getWaypoints());
		addRobot(drone);
		drone.setActiveWaypoint(fence.getWaypoints().get(0));
	}
	
	@Override
	public void update(double time) {
		
	}
}