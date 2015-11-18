package environment;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import simpletestbehaviors.GoToMultiWaypointCIBehavior;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.RobotCI;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import drone.MissionController;

public class MissionEnvironment extends BoundaryEnvironment{
	
	public double enemyDistance = 30;
	public GeoFence fence = null;
	public double seenSteps = 0;
	
	public boolean lampedusa = false;
	
	private ArrayList<Waypoint> bases = new ArrayList<Waypoint>();
	
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
		
		createFence(simulator);
		
		wallsDistance = enemyDistance;
		
	}
	
	private void createFence(Simulator simulator) {
		
		
		int sizeW = 100;
		int sizeH = 100;
		
		commoninterface.mathutils.Vector2d vec = CoordinateUtilities.GPSToCartesian(new LatLon(38.766524638824215, -9.094010382164727));
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x+=sizeW;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.y-=sizeH;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x-=sizeW;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		
		//base
		vec.y+= (sizeH+sizeH/5);
		vec.x+= sizeW/2;
		
		for(Robot r : simulator.getRobots()) {
			((RobotCI)r).replaceEntity(fence);
			((RobotCI)r).replaceEntity(new Waypoint("base", CoordinateUtilities.cartesianToGPS(vec)));
			
			double rand = simulator.getRandom().nextDouble()*5;//5 m
			
			double x = vec.x + rand*2 - rand;
			double y = vec.y + rand*2 - rand;
			
			r.setPosition(x, y);
			r.setOrientation(Math.PI*2*simulator.getRandom().nextDouble());
		}
		
	}
	
	@Override
	public void update(double time) {
		
		AquaticDrone enemy = (AquaticDrone)getRobots().get(getRobots().size()-1);

		if(time == 1) {
				
			//DRONES
			File f = new File("swarm/hierarchical.conf");
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
			f = new File("swarm/preprog_waypoint.conf");
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
				
			enemy.startBehavior(new GoToMultiWaypointCIBehavior(args, enemy));
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
	
}