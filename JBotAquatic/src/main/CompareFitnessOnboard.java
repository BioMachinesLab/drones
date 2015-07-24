package main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.entities.RobotLocation;
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
					
					ArrayList<LogData> data = getData(experiment, controller, robots, sample);
				
					try {
						JBotEvolver jbot2 = new JBotEvolver(new String[]{"compare/config/aggregate.conf"});
						hash = jbot2.getArguments();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=320,height=320,steps="+(int)maxSteps,true));
					
					Simulator sim = new Simulator(new Random(1), hash);
					
					ArrayList<Robot> robots = new ArrayList<Robot>();
					
					HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
					for(LogData d : data) {
						RobotLocation rl = getRobotLocation(d);
						int id = Integer.parseInt(rl.getName());
						robotList.put(id, 0);
					}
					
					for(int i = 0 ; i < robotList.keySet().size() ; i++) {
						AquaticDrone drone = new AquaticDrone(sim, hash.get("--robots"));
						drone.setPosition(start.x,start.y);
						robots.add(drone);
						robotList.put((Integer)robotList.keySet().toArray()[i], robots.size()-1);
						System.out.println(robotList.keySet().toArray()[i]+" = "+(robots.size()-1));
					}
					
					if(robots.size() != this.robots) {
						System.err.println("NUMBER OF ROBOTS DIFFERENT! "+robots.size()+" != "+this.robots);
//						System.exit(0);
					}
					
					boolean gui = true;
					
					TwoDRenderer renderer = new TwoDRenderer(new Arguments("bigrobots=1,drawframes=1"));
					
					if(gui) {
						renderer.setSimulator(sim);
						renderer.drawFrame();
						frame.setRenderer(renderer);
						frame.validate();
					}
					
					sim.addRobots(robots);
					
					RobotLocation firstRL = getRobotLocation(data.get(0));
					commoninterface.mathutils.Vector2d firstPos = CoordinateUtilities.GPSToCartesian(firstRL.getLatLon());
					
					for(Integer id : robotList.keySet()) {
						for(LogData d : data) {
							
							RobotLocation rl = getRobotLocation(d);
							int logId = Integer.parseInt(rl.getName());
							
							if(logId == id) {
								int position = robotList.get(id);
								commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
								robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
								robots.get(position).setOrientation(Math.toRadians(rl.getOrientation()));
								break;
							}
							
						}
					}
					
					AggregateFitness ff = new AggregateFitness(new Arguments("classname=evaluation.AggregateFitness,dontuse=1"));
					sim.addCallback(ff);
					
					String dateStr = data.get(0).systemTime;
					DateTime stepTime = DateTime.parse(dateStr,formatter);
					DateTime currentTime;
					int step = 0;

					//REAL ROBOT
					for(LogData d : data) {
						
						RobotLocation rl = getRobotLocation(d);
							
						currentTime = DateTime.parse(d.systemTime,formatter);
						System.out.println(step+" "+currentTime+" "+stepTime);
						
						while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) > 100) {
							stepTime = stepTime.plus(100);
							step++;
							sim.performOneSimulationStep((double)step);
						}
						
						commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
						
						int id = Integer.parseInt(rl.getName());
						int position = robotList.get(id);
						
						robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
						robots.get(position).setOrientation(Math.toRadians(rl.getOrientation()));
						
						if(gui) {
							renderer.drawFrame();
							renderer.repaint();
						}
						
					}
					
					System.out.println("Fitness: "+ff.getFitness());
				
				}
				
			}
			
		}
		
			
//			sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1,fixedspeed=1,angle=-45")));
//			
//			String f = "compare/controllers/"+FILE+".conf";
//			
//			try {
//				Scanner s = new Scanner(new File(f));
//				f="";
//				while(s.hasNextLine()) {
//					f+=s.nextLine()+"\n";
//				}
//				s.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			BehaviorMessage bm = new BehaviorMessage("ControllerCIBehavior", f.replaceAll("\\s+", ""), true, "dude");
//			
//			for(Robot r : robots) {
//				AquaticDrone drone = (AquaticDrone)r;
//				drone.processInformationRequest(bm, null);
//			}
//			
//			for(int i = 0 ; i < maxSteps ; i++) {
//				sim.performOneSimulationStep((double)i);
//				renderer.drawFrame();
//				renderer.repaint();
//			}
//			
		
		
		
	}
	
	public void pause() {
		this.pause = !pause;
	}
	
	public static void main(String[] args) {
		new JFrameViewerFitnessOnboard();
	}
	
}

class JFrameViewerFitnessOnboard extends JFrame{
	
	private Renderer renderer;
	private CompareFitnessOnboard plot;
	
	public JFrameViewerFitnessOnboard() {
		super("Position Plot");
		setLayout(new BorderLayout());
		
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
		
		add(south, BorderLayout.SOUTH);
		
		setSize(1000, 1000);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		button.doClick();
	}
	
	public void setRenderer(Renderer renderer) {
		if(this.renderer != null)
			remove(this.renderer);
		add(renderer,BorderLayout.CENTER);
		this.renderer = renderer;
	}
	
}
