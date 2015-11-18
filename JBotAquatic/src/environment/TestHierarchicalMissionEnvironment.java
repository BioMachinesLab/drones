package environment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import simpletestbehaviors.GoToMultiWaypointCIBehavior;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import comm.FileProvider;
import commoninterface.AquaticDroneCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.RobotCI;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import commoninterface.utils.jcoord.LatLon;
import drone.MissionController;
import fieldtests.updatables.HierarchicalTestStatistics;

public class TestHierarchicalMissionEnvironment extends BoundaryEnvironment{
	
	public double enemyDistance = 30;
	public GeoFence fence = null;
	public double seenSteps = 0;
	
	public boolean lampedusa = false;
	
	private ArrayList<Line> lines = null;
	
	private double fenceW = 100;
	private double fenceH = 100;
	private Simulator sim;
	private boolean simple=false;
	
	private String controllerFile = "compare/controllers/preset_hierarchical0.conf";
	
	private ArrayList<Waypoint> bases = new ArrayList<Waypoint>();
	
	public TestHierarchicalMissionEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.enemyDistance = args.getArgumentAsDoubleOrSetDefault("enemydistance", enemyDistance);
		if(args.getFlagIsTrue("statistics")) {
			simulator.addCallback(new HierarchicalTestStatistics(20));
		}
		this.sim = simulator;
		fenceW = args.getArgumentAsDoubleOrSetDefault("fencew", fenceW);
		fenceH = args.getArgumentAsDoubleOrSetDefault("fenceh", fenceH);
		controllerFile = args.getArgumentAsStringOrSetDefault("controllerfile", controllerFile);
		simple = args.getFlagIsTrue("simple");
	}
	
	@Override
	public void setup(Simulator simulator) {
		
		this.setup = true;
		if(!simple) {
			AquaticDrone enemy = new AquaticDrone(simulator, new Arguments("diameter=2,gpserror=1.8,commrange=40,rudder=0"));
			enemy.setDroneType(DroneType.ENEMY);
			addRobot(enemy);
			
			fence = new GeoFence("fence");
			
			createFence(simulator);
			
			wallsDistance = enemyDistance;
		}
	}
	
	private void createFence(Simulator simulator) {
		
		double sizeW = fenceW;
		double sizeH = fenceH;
		
		commoninterface.mathutils.Vector2d vec = CoordinateUtilities.GPSToCartesian(new LatLon(38.766524638824215, -9.094010382164727));
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x+=sizeW;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.y-=sizeH;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		vec.x-=sizeW;
		fence.addWaypoint(CoordinateUtilities.cartesianToGPS(vec));
		
		//base
		vec.y+= (sizeH+100/5);
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
			
			FileProvider fp = sim.getFileProvider();
			StringBuffer str = new StringBuffer();
				
			//DRONES
			try {
				
				Scanner s = new Scanner(fp.getFile(controllerFile));
				s.useDelimiter("\n");
				str = new StringBuffer();
			
			
				while(s.hasNextLine())
					str.append(s.nextLine()+"\n");
				
				s.close();
				
			
				CIArguments args = new CIArguments(str.toString().replaceAll("\\s+", ""),true);
				
				for(int i = 0 ; i < getRobots().size()-1 ; i++) {
					AquaticDrone r = (AquaticDrone)getRobots().get(i);
					r.startBehavior(new MissionController(args, r));
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//ENEMY
			try {
				Scanner s = new Scanner(fp.getFile("compare/controllers/preprog_waypoint.conf"));
				s.useDelimiter("\n");
				str = new StringBuffer();
				
				while(s.hasNextLine())
					str.append(s.nextLine()+"\n");
				
				s.close();
				
				CIArguments args = new CIArguments(str.toString().replaceAll("\\s+", ""),true);
					
				enemy.startBehavior(new GoToMultiWaypointCIBehavior(args, enemy));
			
			} catch (Exception e) {
				e.printStackTrace();
			}
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
	
	private ArrayList<Line> getGeoFenceLines() {
		
		if(lines == null) {
			
			if(fence == null)
				fence = GeoFence.getGeoFences((AquaticDroneCI) sim.getRobots().get(0)).get(0);
		
			if(fence != null) {
				lines = new ArrayList<Line>();
				
				LinkedList<Waypoint> waypoints = fence.getWaypoints();
				
				for(int i = 1 ; i < waypoints.size() ; i++) {
					
					Waypoint wa = waypoints.get(i-1);
					Waypoint wb = waypoints.get(i);
					
					lines.add(getLine(wa,wb));
				}
				
				//loop around
				Waypoint wa = waypoints.get(waypoints.size()-1);
				Waypoint wb = waypoints.get(0);
				
				lines.add(getLine(wa,wb));
				return lines;
			}
		} else {
			return lines;
		}
		
		return null;
	}
	
	public boolean insideBoundary(LatLon latLon) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		
		Vector2d vector = CoordinateUtilities.GPSToCartesian(latLon);

		for (Line l : getGeoFenceLines()) {
			if (l.intersectsWithLineSegment(vector, new Vector2d(0,
					-Integer.MAX_VALUE)) != null)
				count++;
		}
		
		return count % 2 != 0;
	}
	
	private Line getLine(Waypoint wa, Waypoint wb) {
		Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());
		
		return new Line(va.getX(), va.getY(), vb.getX(), vb.getY());
	}
	
}