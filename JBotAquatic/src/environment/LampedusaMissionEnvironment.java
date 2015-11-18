package environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import simpletestbehaviors.GoForwardCIBehavior;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import commoninterface.utils.jcoord.LatLon;
import drone.MissionController;
import fieldtests.updatables.IntruderStatistics;

public class LampedusaMissionEnvironment extends BoundaryEnvironment{
	
	public GeoFence fence = null;
	public double seenSteps = 0;
	
	public boolean lampedusa = false;
	private int deployTime = 0;
	private int deployIndex = 0;
	private CIArguments droneArguments;
	private Simulator simulator;
	private AquaticDrone intruder;
	private int intruderDeployTime = 30*60*10;//30min
	
	private ArrayList<Waypoint> bases = new ArrayList<Waypoint>();
	private ArrayList<Line> lines;
	private int onboardRange = 50;
	
	public LampedusaMissionEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.lampedusa = args.getFlagIsTrue("lampedusa");
		
		deployTime = args.getArgumentAsIntOrSetDefault("deploytime", deployTime);
		intruderDeployTime = args.getArgumentAsIntOrSetDefault("intruderdeploytime", intruderDeployTime);
		
		this.onboardRange = args.getArgumentAsIntOrSetDefault("onboardrange", onboardRange);
		
		if(args.getArgumentIsDefined("statistics")) {
			simulator.addCallback(new IntruderStatistics(onboardRange));
		}
		this.simulator = simulator;
		
	}
	
	@Override
	public void setup(Simulator simulator) {
		
		this.setup = true;
		
		droneArguments = getArguments("swarm/hierarchical"+onboardRange+".conf");
		
		createFence(simulator);
		
		for(Robot r : robots) {
			r.setEnabled(false);
			r.setPosition(new mathutils.Vector2d(20000,20000));
		}
		
		createIntruder();
	}
	
	private void createIntruder() {
		intruder = new AquaticDrone(simulator, new Arguments("diameter=2,gpserror=0.0,commrange="+onboardRange+",rudder=1"));
		intruder.setDroneType(DroneType.ENEMY);
		intruder.setOrientation(Math.PI/2);
		intruder.setEnabled(true);
		intruder.startBehavior(new GoForwardCIBehavior(new CIArguments(""), intruder));
		addRobot(intruder);
		
	}
	
	private void createFence(Simulator simulator) {
		
		fence = new GeoFence("fence");
		
		LinkedList<Waypoint> currentWPs = new LinkedList<Waypoint>();
		
		int baseNumber = 0;
		
		double intruderX = 0;
		double intruderY = 0;
		
		try {
			
			Scanner s = new Scanner(simulator.getFileProvider().getFile("swarm/lampedusa.txt"));
			
			while(s.hasNextLine()) {
				String line = s.nextLine();
				
				String[] split = line.split(" ");
				
				if(line.startsWith("B")) {
					Vector2d baseWP = new Vector2d(Double.parseDouble(split[1]), Double.parseDouble(split[2]));
					bases.add(new Waypoint("base"+(baseNumber++),CoordinateUtilities.cartesianToGPS(baseWP)));
				} else if (line.startsWith("L")) {
					Vector2d baseWP = new Vector2d(Double.parseDouble(split[1]), Double.parseDouble(split[2]));
					Waypoint wp = new Waypoint("base"+(baseNumber++),CoordinateUtilities.cartesianToGPS(baseWP));
					if(baseWP.getY() < intruderY)
						intruderY = baseWP.getY();
					if(Math.abs(baseWP.getX()) > intruderX)
						intruderX = Math.abs(baseWP.getX());
					currentWPs.add(wp);
				}
			}
			
			s.close();
			
			for(Waypoint wp : currentWPs) {
				fence.addWaypoint(wp);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	@Override
	public void update(double time) {
		
		super.update(time);
		if(time % deployTime == 0) {
			if(deployIndex < robots.size()) {
				deployDrone(deployIndex);
			}
			deployIndex++;
		}
		
		if(time > intruderDeployTime && time % intruderDeployTime == 0) {
			deployIntruder();
		}
	}
	
	private void deployIntruder() {
		LatLon pos = chooseLatLonInGeoFence();
		Vector2d v = CoordinateUtilities.GPSToCartesian(pos);
		intruder.teleportTo(new mathutils.Vector2d(v.getX(),v.getY()-1500));
	}
	
	private void deployDrone(int index) {
		AquaticDrone r = (AquaticDrone)robots.get(index);
		if(r.getDroneType() == DroneType.DRONE) {
			r.setEnabled(true);
			Vector2d v = CoordinateUtilities.GPSToCartesian(bases.get(r.getId()%this.bases.size()).getLatLon());
			
			r.teleportTo(new mathutils.Vector2d(v.x,v.y));
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
			
			r.replaceEntity(fence);
			r.replaceEntity(bases.get(index % bases.size()));
			
			r.startBehavior(new MissionController(droneArguments, r));
		}
	}
	
	private CIArguments getArguments(String filename) {
		File f = new File(filename);
		StringBuffer str = new StringBuffer();
		
		try {
			Scanner s = new Scanner(f);
			
			while(s.hasNextLine())
				str.append(s.nextLine()+"\n");
			
			s.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new CIArguments(str.toString().replaceAll("\\s+", ""),true);
	}
	
	private ArrayList<Line> getGeoFenceLines() {
		
		if(lines == null) {
		
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
	
	protected LatLon chooseLatLonInGeoFence() {

		LatLon result = null;
		if(fence != null) {
			
			LinkedList<Waypoint> wps = fence.getWaypoints();
			
			Vector2d coord = CoordinateUtilities.GPSToCartesian(wps.getFirst().getLatLon());
			
			double minX = coord.x;
			double maxX = coord.x;
			double minY = coord.y;
			double maxY = coord.y;
			
			for(int i = 1 ; i < wps.size() ; i++) {
				Vector2d wpCoord = CoordinateUtilities.GPSToCartesian(wps.get(i).getLatLon());
				
				minX = Math.min(minX, wpCoord.x);
				maxX = Math.max(maxX, wpCoord.x);
				
				minY = Math.min(minY, wpCoord.y);
				maxY = Math.max(maxY, wpCoord.y);
			}
			
			int tries = 0;
			boolean success = false;
			
			do {
				
				double x = minX+(maxX-minX)*simulator.getRandom().nextDouble();
				double y = minY+(maxY-minY)*simulator.getRandom().nextDouble();
				
				LatLon rLatLon = CoordinateUtilities.cartesianToGPS(new Vector2d(x,y));
				
				if(insideBoundary(rLatLon)) {
					success = true;
					result = rLatLon;
				}
				
			} while(!success && ++tries < 1000);
			
		}
		return result;
	}
	
}