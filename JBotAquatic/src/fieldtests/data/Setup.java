package fieldtests.data;

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
	public double resolution = 0.6;
	
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
		
		hash.get("--evaluation").setArgument("resolution", resolution);
//		hash.get("--evaluation").setArgument("decrease", 2000);
		
		hash.get("--evaluation").setArgument("min",1);
//		hash.get("--evaluation").setArgument("targetwp",3);
		
		if(simulation) { 
//			hash.get("--evaluation").setArgument("usegps",1);
//			hash.get("--robots").setArgument("badgps", 1);
		} else {
//			hash.get("--evaluation").setArgument("clusterdistance",7+1.8*2);
//			hash.get("--evaluation").setArgument("clusterdistance",7+1.8);
//			hash.get("--evaluation").setArgument("instant", 1);
			hash.get("--evaluation").setArgument("targetdistance", 2+1.8);
			//TODO REMOVE
			hash.get("--robots").setArgument("kalmanfilter",0);
			hash.get("--robots").setArgument("gpserror",0);
			hash.get("--evaluation").setArgument("usegps",1);
			
		}
		
		hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=150,height=150,steps="+exp.timeSteps,true));
		
//		hash.put("--environment", new Arguments("classname=environment.TestHierarchicalMissionEnvironment,enemydistance=30,width=200,height=200,steps=5001,deploytime=0,onboardrange=40,simple=1"));
//		//percentageseeing,averagerobotsseeing coverage
//		hash.put("--evaluation",new Arguments("classname=evaluation.IntruderStatisticsFitness,type=coverage"));
		
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
		
		if(exp.controllerName.contains("hierarchical")) {
			for(Robot r : robots) {
				int c = 0;
				for(LogData d : exp.logs) {
					if(d.entities.size()==2)
						break;
					c++;
				}
				updateRobotEntities(exp.logs.get(c), (AquaticDrone)r);
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
//		sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1")));
	
/*		
		for(Updatable u : sim.getCallbacks())
			if(u instanceof WaterCurrent)
				((WaterCurrent)u).disable();
		
		double val = 0;
		
		if(exp.toString().equals("waypoint0_1_8")) val = 0.1855567202347092;
		if(exp.toString().equals("waypoint0_2_8")) val = 0.1974116191050818;
		if(exp.toString().equals("waypoint0_3_8")) val = 0.16910446848365188;
		
		if(exp.toString().equals("waypoint1_1_8")) val = 0.11666613512650208;
		if(exp.toString().equals("waypoint1_2_8")) val = 0.08472035916661572;
		if(exp.toString().equals("waypoint1_3_8")) val = 0.14570037325057994;
		
		if(exp.toString().equals("waypoint2_1_8")) val = 0.12181122407714738;
		if(exp.toString().equals("waypoint2_2_8")) val = 0.14696682730072963;
		if(exp.toString().equals("waypoint2_3_8")) val = 0.07183482937207943;
		
		sim.addCallback(new WaterCurrent(new Arguments("fixedspeed=1,angle=-45,maxspeed="+val)));
*/
		if(gui) {
			renderer = getRenderer();
			viewer = new FitnessViewer(renderer);
		}
		
	}
	
	public void updateRobotEntities(LogData d, AquaticDrone aq) {
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
	
	
	public void updateRobotEntities(LogData d, String ip) {
		AquaticDrone aq = (AquaticDrone)getRobot(ip);
		updateRobotEntities(d,aq);
	}
	
	public void updateRobotEntities(LogData d) {
		updateRobotEntities(d,d.ip);
	}
	
	public LatLon getShiftedLatLon(LatLon latLon) {
		commoninterface.mathutils.Vector2d shifted = CoordinateUtilities.GPSToCartesian(latLon);
		shifted.x = shifted.x+start.x-firstPos.x;
		shifted.y = shifted.y+start.y-firstPos.y;
		return CoordinateUtilities.cartesianToGPS(shifted);
	}
	
	private Waypoint getShiftedWaypoint(Waypoint wp) {
		LatLon latLon = wp.getLatLon();
		return new Waypoint(wp.getName(), getShiftedLatLon(latLon));
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