package main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.WaterCurrent;
import commoninterface.entities.RobotLocation;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogData;
import evaluation.AggregateFitness;
import evolutionaryrobotics.JBotEvolver;
import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;

public class CompareFitnessOnboard extends Thread{
	
	static double maxSteps = 1800;
	
	String[] experiments = new String[]{"aggregate"};
	int samples = 3;
	int controllers = 3;
	int robots = 8;

	static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
	private JFrameViewerFitnessOnboard frame;
	private boolean pause = false;
	
	public CompareFitnessOnboard(JFrameViewerFitnessOnboard frame) {
		this.frame = frame;
	}
	
	private ArrayList<LogData> getData(String experiment, int controller, int robots, int sample) {
		
		ArrayList<LogData> data = new ArrayList<LogData>();
		
		try {
			
			Scanner sBcast = new Scanner(new File("compare/onboard/"+experiment+""+controller+"_"+robots+"_"+sample+".txt"));
		
			while(sBcast.hasNextLine()) {
				String line = sBcast.nextLine();
				DecodedLog decodedData = LogCodex.decodeLog(line);
				Object o = decodedData.getPayload();
				
				if(o == null)
					continue;
				
				LogData d = (LogData)o;
				data.add(d);
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
		
		for(String experiment : experiments) {
			
			for(int controller = 0 ; controller < controllers ; controller++) {
				for(int sample = 1 ; sample <= samples ; sample++) {
					
					Setup real = new Setup();
					Setup sim = new Setup();
					
					ArrayList<LogData> data = getData(experiment, controller, robots, sample);
				
					try {
						JBotEvolver jbot = new JBotEvolver(new String[]{"compare/config/aggregate.conf"});
						hash = jbot.getArguments();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=150,height=150,steps="+(int)maxSteps,true));
					
					real.sim = new Simulator(new Random(1), hash);
					sim.sim = new Simulator(new Random(1), hash);
					
					HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
					for(LogData d : data) {
						RobotLocation rl = getRobotLocation(d);
						int id = Integer.parseInt(rl.getName());
						robotList.put(id, 0);
					}
					
					real.setupDrones(robotList,hash.get("--robots"),start);
					sim.setupDrones(robotList,hash.get("--robots"),start);
					
					if(this.robots != real.robots.size()) {
						System.err.println("NUMBER OF ROBOTS DIFFERENT! "+this.robots+" != "+this.robots);
						System.exit(0);
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
					
					AggregateFitness ff_real = new AggregateFitness(new Arguments("classname=evaluation.AggregateFitness,dontuse=1"));
					AggregateFitness ff_sim = new AggregateFitness(new Arguments("classname=evaluation.AggregateFitness,dontuse=1"));
					
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
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					BehaviorMessage bm = new BehaviorMessage("ControllerCIBehavior", f.replaceAll("\\s+", ""), true, "dude");
					
					for(Robot r : sim.robots) {
						AquaticDrone drone = (AquaticDrone)r;
						drone.processInformationRequest(bm, null);
					}
					
					sim.sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1,fixedspeed=1,angle=-45")));
					
					for(LogData d : data) {
						
						RobotLocation rl = getRobotLocation(d);
							
						currentTime = DateTime.parse(d.systemTime,formatter);
						
						while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) > 100) {
							stepTime = stepTime.plus(100);
							step++;
//							System.out.println(step);
							real.sim.performOneSimulationStep((double)step);
							sim.sim.performOneSimulationStep((double)step);
							
							for(int i = 0 ; i < sim.sim.getRobots().size() ; i++) {
								Vector2d v = new Vector2d(real.sim.getRobots().get(i).getPosition().x,real.sim.getRobots().get(i).getPosition().y);
								real.sim.getEnvironment().addStaticObject(new LightPole(real.sim, "lp", v.x, v.y, 0.2));
								
								v = new Vector2d(sim.sim.getRobots().get(i).getPosition().x,sim.sim.getRobots().get(i).getPosition().y);
								sim.sim.getEnvironment().addStaticObject(new LightPole(sim.sim, "lp", v.x, v.y, 0.2));
							}
							
							if(step % 100 == 0) {
								
								Iterator<PhysicalObject> it = real.sim.getEnvironment().getAllObjects().iterator();
						        while(it.hasNext()) {
						        	if(it.next() instanceof LightPole)
						        		it.remove();
						        }
						        it = sim.sim.getEnvironment().getAllObjects().iterator();
						        while(it.hasNext()) {
						        	if(it.next() instanceof LightPole)
						        		it.remove();
						        }
								
								for(int i = 0 ; i < sim.sim.getRobots().size() ; i++) {
//									sim.sim.getRobots().get(i).setPosition(real.sim.getRobots().get(i).getPosition());
//									sim.sim.getRobots().get(i).setOrientation(real.sim.getRobots().get(i).getOrientation());
								}
							}
						}
						
						commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
						
						int id = Integer.parseInt(rl.getName());
						int position = robotList.get(id);
						
						real.robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
						double orientation = 360 - (rl.getOrientation() - 90);
						real.robots.get(position).setOrientation(Math.toRadians(orientation));
						
						if(d.outputNeuronStates != null) {
//							double heading = d.outputNeuronStates[0]*2-1;
//							double speed = d.outputNeuronStates[1];
//							((AquaticDrone)sim.sim.getRobots().get(position)).setRudder(heading, speed);
						}
						
						if(gui) {
							sim.renderer.drawFrame();
							real.renderer.drawFrame();
							sim.renderer.repaint();
							real.renderer.repaint();
						}
						
					}
					
					System.out.println("Fitness: "+ff_real.getFitness()+" "+ff_sim.getFitness());
					System.exit(0);
				}
			}
		}
		
	}
	
	public void pause() {
		this.pause = !pause;
	}
	
	public static void main(String[] args) {
		new JFrameViewerFitnessOnboard();
	}
	
}

class Setup {
	
	Renderer renderer;
	Simulator sim;
	ArrayList<Robot> robots = new ArrayList<Robot>();
	
	public void setupDrones(HashMap<Integer,Integer> robotList, Arguments args, Vector2d start) {
		for(int i = 0 ; i < robotList.keySet().size() ; i++) {
			AquaticDrone drone = new AquaticDrone(sim, args);
			drone.setPosition(start.x,start.y);
			robots.add(drone);
			robotList.put((Integer)robotList.keySet().toArray()[i], robots.size()-1);
		}
	}
	
}

class JFrameViewerFitnessOnboard extends JFrame{
	
	private Renderer renderer1;
	private Renderer renderer2;
	private CompareFitnessOnboard plot;
	
	public JFrameViewerFitnessOnboard() {
		super("Position Plot");
		setLayout(new GridLayout(1,2));
		
		final JFrameViewerFitnessOnboard t = this;
		
		JButton button = new JButton("Replay");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(plot != null)
					plot.interrupt();
				plot = new CompareFitnessOnboard(t);
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
		
		setSize(1000, 1000);
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
