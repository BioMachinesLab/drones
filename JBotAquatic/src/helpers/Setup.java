package helpers;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import gui.renderer.CITwoDRenderer;
import gui.renderer.Renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.WaterCurrent;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.LogData;

class Setup {
	
	public Renderer renderer;
	public Simulator sim;
	public EvaluationFunction eval;
	public ArrayList<Robot> robots = new ArrayList<Robot>();
	public HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
	public Vector2d start = new Vector2d(0,0);
	public commoninterface.mathutils.Vector2d firstPos;
	public FitnessViewer viewer;
	public Experiment exp;
	public boolean simulation = false;
	
	public void setupDrones(HashMap<Integer,Integer> robotList, Arguments args, Vector2d start) {
		for(int i = 0 ; i < robotList.keySet().size() ; i++) {
			AquaticDrone drone = new AquaticDrone(sim, args);
			drone.setPosition(start.x,start.y);
			robots.add(drone);
			robotList.put((Integer)robotList.keySet().toArray()[i], robots.size()-1);
		}
	}
	
	public Robot getRobot(String ip) {
		String[] split = ip.split("\\.");
		int id= Integer.parseInt(split[split.length-1]);
		return robots.get(robotList.get(id));
	}
	
	public Setup() {}
	
	public Setup(Experiment exp, long randomSeed, boolean gui, boolean simulation) {
		
		this.exp = exp;
		this.simulation = simulation;
		
		HashMap<String, Arguments> hash = new HashMap<String, Arguments>();
		
		try {
			JBotEvolver jbot = new JBotEvolver(new String[]{"compare/config/"+exp.controllerName+".conf"});
			hash = jbot.getArguments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(simulation) { 
//			hash.get("--evaluation").setArgument("usegps",1);
//			hash.get("--robots").setArgument("badgps", 1);
		} else {
			hash.get("--evaluation").setArgument("clusterdistance",10.6);
			hash.get("--evaluation").setArgument("resolution", 1);
			hash.get("--evaluation").setArgument("trace", 1);
		}
		
		hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=150,height=150,steps="+exp.timeSteps,true));
		
		sim = new Simulator(new Random(randomSeed), hash);
		
		HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
		
		for(LogData d : exp.logs) {
			if(d.latLon != null) {
				RobotLocation rl = getRobotLocation(d);
				int id = Integer.parseInt(rl.getName());
				robotList.put(id, 0);
			}
		}
		
		setupDrones(robotList,hash.get("--robots"),start);
		sim.addRobots(robots);
		this.robotList = robotList;
		
		RobotLocation firstRL = getRobotLocation(exp.logs.get(0));
		this.firstPos = CoordinateUtilities.GPSToCartesian(firstRL.getLatLon());
		
		for(Integer id : robotList.keySet()) {
			for(LogData d : exp.logs) {
				
				if(d.latLon == null)
					continue;
				
				RobotLocation rl = getRobotLocation(d);
				int logId = Integer.parseInt(rl.getName());
				
				if(logId == id) {
					int position = robotList.get(id);
					commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
					robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
					
					double orientation = 360 - (rl.getOrientation() - 90);
					
					robots.get(position).setOrientation(Math.toRadians(orientation));
					
					updateRobotEntities(d);
					
					break;
				}
			}
		}
		
		String className = hash.get("--evaluation").getArgumentAsString("classname");
		hash.get("--evaluation").setArgument("classname", className+"Test");
		hash.get("--evaluation").setArgument("dontuse",1);
		hash.get("--evaluation").setArgument("laststeps",1);
		
		if(exp.activeRobot != -1) {
			hash.get("--evaluation").setArgument("activerobot",getRobot(""+exp.activeRobot).getId());
		}
		EvaluationFunction eval = EvaluationFunction.getEvaluationFunction(hash.get("--evaluation"));
		this.eval = eval;
		
		sim.addCallback(eval);
		sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1")));
		
		if(gui) {
			renderer = getRenderer();
			viewer = new FitnessViewer(renderer);
		}
		
	}
	
	public void updateRobotEntities(LogData d) {
		
		AquaticDrone aq = (AquaticDrone)getRobot(d.ip);
		Iterator<Entity> i = aq.getEntities().iterator();
		while(i.hasNext()) {
			Entity e = i.next();
			if(e instanceof Waypoint || e instanceof GeoFence)
				i.remove();
		}
		
		for(Entity e : d.entities) {
			Entity replacement = null;
			if(e instanceof Waypoint) {
				replacement = getShiftedWaypoint((Waypoint)e);
			} else if(e instanceof GeoFence) {
				
				GeoFence fence = (GeoFence)e;
				replacement = new GeoFence(fence.getName());
				
				for(Waypoint wp : fence.getWaypoints()) {
					((GeoFence)replacement).addWaypoint(getShiftedWaypoint(wp));
				}
				
			}
			aq.replaceEntity(replacement);
		}
		ArrayList<Waypoint> wps = Waypoint.getWaypoints(aq);
		
		if(!wps.isEmpty()) {
			aq.setActiveWaypoint(wps.get(0));
		}
	}
	
	private Waypoint getShiftedWaypoint(Waypoint wp) {
		LatLon latLon = wp.getLatLon();
		
		commoninterface.mathutils.Vector2d shifted = CoordinateUtilities.GPSToCartesian(latLon);
		shifted.x = shifted.x+start.x-firstPos.x;
		shifted.y = shifted.y+start.y-firstPos.y;
		LatLon latLonShifted = CoordinateUtilities.cartesianToGPS(shifted);
		
		return new Waypoint(wp.getName(), latLonShifted);
	}
	
	public Renderer getRenderer() {
		if(renderer == null) {
			int droneId = exp.activeRobot != -1 ? getRobot(""+exp.activeRobot).getId() : 0;
			renderer = new CITwoDRenderer(new Arguments("bigrobots=1,drawframes=5,droneid="+droneId));
			renderer.setSimulator(sim);
		}
		return renderer;
	}
	
	public static RobotLocation getRobotLocation(LogData d) {
		String[] split = d.ip.split("\\.");
		return new RobotLocation(split[split.length-1], d.latLon, d.compassOrientation, d.droneType);
	}
	
}