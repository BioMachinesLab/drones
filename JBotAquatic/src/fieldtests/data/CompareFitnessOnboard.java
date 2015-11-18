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
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogData;

import evaluation.AggregateFitness;
import evolutionaryrobotics.JBotEvolver;
import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;
import gui.util.Graph;

public class CompareFitnessOnboard extends Thread{
	/*	
	static double maxSteps = 1800;
	
	String[] experiments = new String[]{"aggregate","dispersion"};
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
				
				if(decodedData == null)
					continue;
				
				Object o = decodedData.getPayload();
				
				if(o == null)
					continue;
				
				if(o instanceof String)
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
		
		AggregateFitness ff_real = new AggregateFitness(new Arguments("classname=evaluation.AggregateFitness,dontuse=1,startingdistance=50"));
		AggregateFitness ff_sim = new AggregateFitness(new Arguments("classname=evaluation.AggregateFitness,dontuse=1,startingdistance=50"));
		
//		EvaluationFunction ff_real = new DispersionFitness(new Arguments("classname=evaluation.DispersionFitness,dontuse=1,margin=2.5,range=25"));
//		EvaluationFunction ff_sim = new DispersionFitness(new Arguments("classname=evaluation.DispersionFitness,dontuse=1,margin=2.5,range=25"));
		
		try {
			JBotEvolver jbot = new JBotEvolver(new String[]{"compare/config/dispersion.conf"});
			hash = jbot.getArguments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		for(String experiment : experiments) {
			
			for(int controller = 0 ; controller < controllers ; controller++) {
				for(int sample = 1 ; sample <= samples ; ) {
					
					Setup real = new Setup();
					Setup sim = new Setup();
					
					ArrayList<LogData> data = getData(experiment, controller, robots, sample);
				
					hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=150,height=150,steps="+(int)maxSteps,true));
					
					real.sim = new Simulator(new Random(1), hash);
					sim.sim = new Simulator(new Random(3222), hash);
					
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
					
					real.sim.addCallback(ff_real);
					sim.sim.addCallback(ff_sim);
					
					String dateStr = data.get(0).systemTime;
					DateTime stepTime = DateTime.parse(dateStr,formatter);
					DateTime currentTime;
					int step = 0;
					
					String f = "compare/controllers/preset_"+experiment+controller+".conf";
					
//					try {
//						Scanner s = new Scanner(new File(f));
//						f="";
//						while(s.hasNextLine()) {
//							f+=s.nextLine()+"\n";
//						}
//						s.close();
//						
//						BehaviorMessage bm = new BehaviorMessage("ControllerCIBehavior", f.replaceAll("\\s+", ""), true, "dude");
//						
//						for(Robot r : sim.robots) {
//							AquaticDrone drone = (AquaticDrone)r;
//							drone.processInformationRequest(bm, null);
//						}
//						
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					}
					
					sim.sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1,fixedspeed=1,angle=-45")));
					
					Graph graph = new Graph();
					JFrame frame = new JFrame();
					frame.add(graph);
					graph.setShowLast(1800);
					frame.setSize(1800,800);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
					
					graph.addLegend("sim");
					graph.addLegend("real");
					
					Double[] headingD = new Double[1800];
					Double[] realO = new Double[1800];
					Double[] simO = new Double[1800];
					int index = 0;
					
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
//								Vector2d v = new Vector2d(real.sim.getRobots().get(i).getPosition().x,real.sim.getRobots().get(i).getPosition().y);
//								real.sim.getEnvironment().addStaticObject(new LightPole(real.sim, "lp", v.x, v.y, 0.2));
//								
//								v = new Vector2d(sim.sim.getRobots().get(i).getPosition().x,sim.sim.getRobots().get(i).getPosition().y);
//								sim.sim.getEnvironment().addStaticObject(new LightPole(sim.sim, "lp", v.x, v.y, 0.2));
							}
							
							if(step % 100 == 0) {
								
//								Iterator<PhysicalObject> it = real.sim.getEnvironment().getAllObjects().iterator();
//						        while(it.hasNext()) {
//						        	if(it.next() instanceof LightPole)
//						        		it.remove();
//						        }
//						        it = sim.sim.getEnvironment().getAllObjects().iterator();
//						        while(it.hasNext()) {
//						        	if(it.next() instanceof LightPole)
//						        		it.remove();
//						        }
								
								for(int i = 0 ; i < sim.sim.getRobots().size() ; i++) {
//									sim.sim.getRobots().get(i).setPosition(real.sim.getRobots().get(i).getPosition());
//									sim.sim.getRobots().get(i).setOrientation(real.sim.getRobots().get(i).getOrientation());
								}
							}
							
//							try {
//								Thread.sleep(10);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
						}
						
						commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
						
						int id = Integer.parseInt(rl.getName());
						int position = robotList.get(id);
						
						real.robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
						double orientation = 360 - (rl.getOrientation() - 90);
						real.robots.get(position).setOrientation(Math.toRadians(orientation));
						
						int chosen = 0;
						
						if(d.outputNeuronStates != null) {
							double heading = d.outputNeuronStates[0]*2-1;
							double speed = d.outputNeuronStates[1];
							((AquaticDrone)sim.sim.getRobots().get(position)).setRudder(heading, speed);
							
							double h = (heading+1)/2;
							
							if(((AquaticDrone)sim.sim.getRobots().get(position)).getId() == chosen) {
//								((AquaticDrone)sim.sim.getRobots().get(position)).setRudder(heading, speed);
								simO[index] = ((AquaticDrone)sim.sim.getRobots().get(position)).getCompassOrientationInDegrees();
								realO[index++] = rl.getOrientation();
								System.out.println(step+" "+speed+" "+heading+" "+((AquaticDrone)sim.sim.getRobots().get(position)).getCompassOrientationInDegrees());
							}
////							System.out.println(h*300+" "+rl.getOrientation()+" "+((AquaticDrone)sim.sim.getRobots().get(position)).getCompassOrientationInDegrees());
						} else {
							//TODO DEBUG
//							sim.sim.getRobots().get(position).setPosition(real.sim.getRobots().get(position).getPosition());
//							sim.sim.getRobots().get(position).setOrientation(real.sim.getRobots().get(position).getOrientation());
							
//							if(((AquaticDrone)sim.sim.getRobots().get(position)).getId() == chosen) {
//								simO[index] = ((AquaticDrone)sim.sim.getRobots().get(position)).getCompassOrientationInDegrees();
//								realO[index++] = rl.getOrientation();
//							}
						}
						
						if(gui) {
							sim.renderer.drawFrame();
							real.renderer.drawFrame();
							sim.renderer.repaint();
							real.renderer.repaint();
						}
						
					}
					
//					graph.addDataList(headingD);
					
//					double error = 0;
//					for(int i = 0 ; i < simO.length ;i++) {
//						if(simO[i] != null)
//							error+=Math.abs(simO[i]-realO[i]);
//					}
					
					graph.addDataList(simO);
					graph.addDataList(realO);
					
//					System.out.println(error);
					
					System.out.println("Fitness: "+ff_real.getFitness()+" "+ff_sim.getFitness());
					
					try {
						Thread.sleep(300000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
	*/	
}
