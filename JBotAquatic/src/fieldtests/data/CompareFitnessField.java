package fieldtests.data;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import mathutils.Vector2d;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.WaterCurrent;
import commoninterface.entities.RobotLocation;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogData;
import evaluation.AggregateFitness;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;
import gui.util.Graph;

public class CompareFitnessField extends Thread{
	
	static double maxSteps = 1800;
	
	String experiment = "dispersion";
	int sample = 1;
	int controller = 0;
	int robots = 8;

	static DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	private JFrameViewerFitnessField frame;
	private boolean pause = false;
	
	private String[] ignoreNames = new String[]{"6","8"};
	
	public CompareFitnessField(JFrameViewerFitnessField frame) {
		this.frame = frame;
	}
	
	private ArrayList<LogData> getData(String experiment, int controller, int robots, int sample) {
		
		ArrayList<LogData> data = new ArrayList<LogData>();
		
		try {
			
			Scanner sBcast = new Scanner(new File("compare/bcast/preset_"+experiment+controller+"_"+sample+".txt"));
		
			while(sBcast.hasNextLine()) {
				String line = sBcast.nextLine();
				if(!line.startsWith("#") && !line.isEmpty()) {
					LogData d = new LogData();
					
					String[] split = line.split(" ");
					String msg = split[1];

					RobotLocation rl = PositionBroadcastMessage.decode(msg);
					
					if(rl == null)
						continue;
					
					boolean ignore = false;
					
					for(String ig : ignoreNames) { 
						if(rl == null)
							continue;
						if(rl.getName().endsWith("."+ig)) {
							ignore = true;
						}
					}
					
					if(ignore)
						continue;
							
					
					d.latLon = rl.getLatLon();
					d.systemTime = split[0];
					d.ip = rl.getName();
					d.compassOrientation = rl.getOrientation();
					d.droneType = rl.getDroneType();
					data.add(d);
				}
			}
			
			sBcast.close();
			
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return data;
	}
	
	private RobotLocation getRobotLocation(LogData d) {
		String[] split = d.ip.split("\\.");
		return new RobotLocation(split[split.length-1], d.latLon, d.compassOrientation, d.droneType);
	}
	
	@Override
	public void run() {
		
		Vector2d start = new Vector2d(0,0);
		
		HashMap<String, Arguments> hash = new HashMap<String, Arguments>();
		
		try {
			JBotEvolver jbot = new JBotEvolver(new String[]{"compare/config/"+experiment+".conf"});
			hash = jbot.getArguments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		hash.get("--evaluation").setArgument("dontuse",1);
		
		EvaluationFunction ff_real = EvaluationFunction.getEvaluationFunction(hash.get("--evaluation"));
		EvaluationFunction ff_sim = EvaluationFunction.getEvaluationFunction(hash.get("--evaluation"));
				
		Setup real = new Setup();
		Setup sim = new Setup();
		
		ArrayList<LogData> data = getData(experiment, controller, robots, sample);
		
		hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=150,height=150,steps="+(int)maxSteps,true));
		
		real.sim = new Simulator(new Random(1), hash);
		sim.sim = new Simulator(new Random(1113), hash);
		
		HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
		
		for(LogData d : data) {
			RobotLocation rl = getRobotLocation(d);
			int id = Integer.parseInt(rl.getName());
			robotList.put(id, 0);
		}
		
		real.setupDrones(robotList,hash.get("--robots"),start);
		sim.setupDrones(robotList,hash.get("--robots"),start);
		
		if(this.robots != real.robots.size()) {
			System.err.println("NUMBER OF ROBOTS DIFFERENT! "+this.robots+" != "+real.robots.size());
//			System.exit(0);
		}
		
		boolean gui = true;
		
		real.renderer = new TwoDRenderer(new Arguments("bigrobots=1,drawframes=5"));
		sim.renderer = new TwoDRenderer(new Arguments("bigrobots=1,drawframes=5"));
		
		if(gui) {
			real.renderer.setSimulator(real.sim);
			sim.renderer.setSimulator(sim.sim);
			real.renderer.drawFrame();
			sim.renderer.drawFrame();
			frame.setRenderer1(real.renderer);
			frame.setRenderer2(sim.renderer);
			frame.validate();
		}
		
		real.sim.addRobots(real.robots);
		sim.sim.addRobots(sim.robots);
		
		RobotLocation firstRL = getRobotLocation(data.get(0));
		commoninterface.mathutils.Vector2d firstPos = CoordinateUtilities.GPSToCartesian(firstRL.getLatLon());
		
		for(Integer id : robotList.keySet()) {
			for(LogData d : data) {
				
				RobotLocation rl = getRobotLocation(d);
				int logId = Integer.parseInt(rl.getName());
				
				if(logId == id) {
					int position = robotList.get(id);
					commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
					real.robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
					sim.robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
					
					double orientation = 360 - (rl.getOrientation() - 90);
					
					real.robots.get(position).setOrientation(Math.toRadians(orientation));
					sim.robots.get(position).setOrientation(Math.toRadians(orientation));
					break;
				}
				
			}
		}
		
		real.sim.addCallback(ff_real);
		sim.sim.addCallback(ff_sim);
		
		String dateStr = data.get(0).systemTime;
		DateTime stepTime = DateTime.parse(dateStr,formatter);
		DateTime currentTime;
		int step = 0;
		
		String f = "compare/controllers/preset_"+experiment+controller+".conf";
		
		try {
			Scanner s = new Scanner(new File(f));
			f="";
			while(s.hasNextLine()) {
				f+=s.nextLine()+"\n";
			}
			s.close();
			
			BehaviorMessage bm = new BehaviorMessage("ControllerCIBehavior", f.replaceAll("\\s+", ""), true, "dude");
			
			for(Robot r : sim.robots) {
				AquaticDrone drone = (AquaticDrone)r;
				drone.processInformationRequest(bm, null);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		sim.sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1,fixedspeed=1,angle=-45")));
		
		for(LogData d : data) {
			
			RobotLocation rl = getRobotLocation(d);
				
			currentTime = DateTime.parse(d.systemTime,formatter);
			
			while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) > 100) {
				stepTime = stepTime.plus(100);
				step++;
				real.sim.performOneSimulationStep((double)step);
				sim.sim.performOneSimulationStep((double)step);
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
			commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
			
			int id = Integer.parseInt(rl.getName());
			int position = robotList.get(id);
			
			real.robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
			double orientation = 360 - (rl.getOrientation() - 90);
			real.robots.get(position).setOrientation(Math.toRadians(orientation));
			
			if(gui) {
				sim.renderer.drawFrame();
				real.renderer.drawFrame();
				sim.renderer.repaint();
				real.renderer.repaint();
			}
			
		}
		
		System.out.println("Fitness: "+ff_real.getFitness()+" "+ff_sim.getFitness());
		
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void pause() {
		this.pause = !pause;
	}
	
	public static void main(String[] args) {
		new JFrameViewerFitnessField();
	}
	
}

class JFrameViewerFitnessField extends JFrame{
	
	private Renderer renderer1;
	private Renderer renderer2;
	private CompareFitnessField plot;
	
	public JFrameViewerFitnessField() {
		super("Position Plot");
		setLayout(new GridLayout(1,2));
		
		final JFrameViewerFitnessField t = this;
		
		JButton button = new JButton("Replay");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(plot != null)
					plot.interrupt();
				plot = new CompareFitnessField(t);
				plot.start();
			}
		});
		
		JButton pause = new JButton("Pause/Play");
		pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(plot != null)
					plot.pause();
			}
		});
		
		JPanel south = new JPanel();
		south.add(button);
		south.add(pause);
		
//		add(south, BorderLayout.SOUTH);
		
		setSize(400, 400);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		button.doClick();
	}
	
	public void setRenderer1(Renderer renderer) {
		if(this.renderer1 != null)
			remove(this.renderer1);
		add(renderer);
		this.renderer1 = renderer;
	}
	
	public void setRenderer2(Renderer renderer) {
		if(this.renderer2 != null)
			remove(this.renderer2);
		add(renderer);
		this.renderer2 = renderer;
	}
	
}
