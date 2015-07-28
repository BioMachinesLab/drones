package helpers;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import commoninterface.entities.RobotLocation;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.logger.LogData;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.WaterCurrent;

class Setup {
	
	public Renderer renderer;
	public Simulator sim;
	public EvaluationFunction eval;
	public ArrayList<Robot> robots = new ArrayList<Robot>();
	public HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
	public Vector2d start = new Vector2d(0,0);
	public commoninterface.mathutils.Vector2d firstPos;
	
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
	
	public Setup(Experiment exp, long randomSeed, boolean gui) {
		
		HashMap<String, Arguments> hash = new HashMap<String, Arguments>();
		
		try {
			JBotEvolver jbot = new JBotEvolver(new String[]{"compare/config/"+exp.controllerName+".conf"});
			hash = jbot.getArguments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		hash.get("--evaluation").setArgument("dontuse",1);
		
		EvaluationFunction eval = EvaluationFunction.getEvaluationFunction(hash.get("--evaluation"));
		hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=150,height=150,steps="+exp.timeSteps,true));
		
		sim = new Simulator(new Random(randomSeed), hash);
		
		HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
		
		for(LogData d : exp.logs) {
			if(d.comment == null) {
				RobotLocation rl = getRobotLocation(d);
				int id = Integer.parseInt(rl.getName());
				robotList.put(id, 0);
			}
		}
		
		setupDrones(robotList,hash.get("--robots"),start);
		sim.addRobots(robots);
		this.robotList = robotList;
		
		RobotLocation firstRL = getRobotLocation(exp.logs.get(0));
		commoninterface.mathutils.Vector2d firstPos = CoordinateUtilities.GPSToCartesian(firstRL.getLatLon());
		this.firstPos = firstPos;
		
		for(Integer id : robotList.keySet()) {
			for(LogData d : exp.logs) {
				
				if(d.comment != null)
					continue;
				
				RobotLocation rl = getRobotLocation(d);
				int logId = Integer.parseInt(rl.getName());
				
				if(logId == id) {
					int position = robotList.get(id);
					commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
					robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
					
					double orientation = 360 - (rl.getOrientation() - 90);
					
					robots.get(position).setOrientation(Math.toRadians(orientation));
					break;
				}
				
			}
		}
		
		sim.addCallback(eval);
		this.eval = eval;
		sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1")));
		
		if(gui) {
			renderer = new TwoDRenderer(new Arguments("bigrobots=1,drawframes=5"));
			renderer.setSimulator(sim);
			
			FitnessViewer viewer = new FitnessViewer(renderer);
		}
		
	}
	
	public static RobotLocation getRobotLocation(LogData d) {
		String[] split = d.ip.split("\\.");
		return new RobotLocation(split[split.length-1], d.latLon, d.compassOrientation, d.droneType);
	}
	
}