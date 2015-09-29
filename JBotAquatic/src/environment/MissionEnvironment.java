package environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import simpletestbehaviors.GoToWaypointCIBehavior;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.RobotCI;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import drone.MissionController;

public class MissionEnvironment extends BoundaryEnvironment{
	
	public double enemyDistance = 30;
	public GeoFence fence = null;
	public double seenSteps = 0;
	
	public MissionEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.enemyDistance = args.getArgumentAsDoubleOrSetDefault("enemydistance", enemyDistance);
	}
	
	@Override
	public void setup(Simulator simulator) {
		
		this.setup = true;
		
		AquaticDrone enemy = new AquaticDrone(simulator, new Arguments("diameter=2,gpserror=1.8,commrange=40,rudder=0"));
		enemy.setDroneType(DroneType.ENEMY);
		addRobot(enemy);
		
		fence = new GeoFence("fence");
		
		int size = 100;
		
		commoninterface.mathutils.Vector2d vec = CoordinateUtilities.GPSToCartesian(new LatLon(38.766524638824215, -9.094010382164727));
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x+=size;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.y-=size;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x-=size;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		
		//base
		vec.y+= (size+size/4);
		vec.x+= size/2;
		
		for(Robot r : simulator.getRobots()) {
			((RobotCI)r).replaceEntity(fence);
			((RobotCI)r).replaceEntity(new Waypoint("base", CoordinateUtilities.cartesianToGPS(vec)));
			
			double rand = simulator.getRandom().nextDouble()*5;//5 m
			
			double x = vec.x + rand*2 - rand;
			double y = vec.y + rand*2 - rand;
			
			r.setPosition(x, y);
			r.setOrientation(Math.PI*2*simulator.getRandom().nextDouble());
		}
		
		wallsDistance = enemyDistance;
		
	}
	
	@Override
	public void update(double time) {
		
		AquaticDrone enemy = (AquaticDrone)getRobots().get(getRobots().size()-1);
		
		if(time == 1) {
			
			//DRONES
			File f = new File("../DroneControlConsole/controllers/preset_hierarchical8r.conf");
			StringBuffer str = new StringBuffer();
			
			try {
				Scanner s = new Scanner(f);
				
				while(s.hasNextLine())
					str.append(s.nextLine()+"\n");
				
				s.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			CIArguments args = new CIArguments(str.toString().replaceAll("\\s+", ""),true);
			
			for(int i = 0 ; i < getRobots().size()-1 ; i++) {
				AquaticDrone r = (AquaticDrone)getRobots().get(i);
				r.startBehavior(new MissionController(args, r));
			}
			
			//ENEMY
			f = new File("../DroneControlConsole/controllers/preset_preprog_waypoint.conf");
			str = new StringBuffer();
			
			try {
				Scanner s = new Scanner(f);
				
				while(s.hasNextLine())
					str.append(s.nextLine()+"\n");
				
				s.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			args = new CIArguments(str.toString().replaceAll("\\s+", ""),true);
			
			enemy.startBehavior(new GoToWaypointCIBehavior(args, enemy));
		}
		
		if(time > 600*2) {
			
			double seenStepsNow = 0;
		
			for(int i = 0 ; i < getRobots().size()-1 ; i++) {
				AquaticDrone r = (AquaticDrone)getRobots().get(i);
				if(r.getPosition().distanceTo(enemy.getPosition()) < 20) {
					seenStepsNow++;
				}
			}
			
//			seenSteps+=Math.min(seenStepsNow, 1);
			seenSteps+=seenStepsNow;
		}
//		System.out.println(seenSteps);
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