package fieldtests.data;

import evaluation.AggregateFitness;
import evolutionaryrobotics.JBotEvolver;
import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;
import java.awt.BorderLayout;
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
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.WaterCurrent;
import commoninterface.entities.RobotLocation;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.utils.CoordinateUtilities;

public class CompareFitnessBroadcast extends Thread{
	
	static String FILE = "preset_aggregate0";
	static double maxSteps = 1800;

	static DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	private JFrameViewerFitness frame;
	private ArrayList<String> broadcastLines = new ArrayList<String>();
	private boolean pause = false;
	
	int sample = 1;
	boolean file = false;
	int nRobots = 8;
	
	public CompareFitnessBroadcast(JFrameViewerFitness frame) {
		this.frame = frame;
		
		try {
		
			Scanner sBcast = new Scanner(new File("compare/bcast/"+FILE+"_"+sample+".txt"));
		
			while(sBcast.hasNextLine()) {
				String line = sBcast.nextLine();
				if(!line.startsWith("#") && !line.isEmpty())
					broadcastLines.add(line);
			}
			
			System.out.println(broadcastLines.size());
			
			sBcast.close();
			
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	@Override
	public void run() {
		
		Vector2d start = new Vector2d(0,0);
		
		HashMap<String, Arguments> hash = new HashMap<String, Arguments>();
		
		try {
			JBotEvolver jbot2 = new JBotEvolver(new String[]{"compare/config/aggregate.conf"});
			hash = jbot2.getArguments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=320,height=320,steps=1800",true));
		
		ArrayList<Robot> robots = new ArrayList<Robot>();
		
		HashMap<Integer,Integer> robotList = new HashMap<Integer,Integer>();
		
		//EXCLUDED
		ArrayList<Integer> excluded = new ArrayList<Integer>();

//		AGG 8R C1 S1 S2
		excluded.add(7);
		
//		AGG 8R C1 S3
//		excluded.add(10);
		
//		AGG 8R C2 S1 S2
//		excluded.add(10);

//		AGG 8R C2 S3
//		excluded.add(1);
		
//		AGG 8R C3 S1 S2 S3
//		excluded.add(1);
		
		for(int i = 0 ; i < broadcastLines.size() ; i++) {
			RobotLocation rl = PositionBroadcastMessage.decode(broadcastLines.get(i).split(" ")[1]);
			String[] split = rl.getName().split("\\.");
			int id = Integer.parseInt(split[split.length-1]);
			if(!excluded.contains(id))
				robotList.put(id, 0);
		}
		
		Simulator sim = new Simulator(new Random(1), hash);
		
		for(int i = 0 ; i < robotList.keySet().size() ; i++) {
			AquaticDrone drone = new AquaticDrone(sim, hash.get("--robots"));
			drone.setPosition(start.x+i*10,start.y);
			robots.add(drone);
			robotList.put((Integer)robotList.keySet().toArray()[i], robots.size()-1);
			System.out.println(robotList.keySet().toArray()[i]+" = "+(robots.size()-1));
		}
		
		if(robots.size() != nRobots) {
			System.out.println("NUMBER OF ROBOTS DIFFERENT");
//			System.exit(0);
		}
		
		TwoDRenderer renderer = new TwoDRenderer(new Arguments("bigrobots=1,drawframes=1"));
		renderer.setSimulator(sim);
		renderer.drawFrame();
		
		frame.setRenderer(renderer);
		frame.validate();
		
		sim.addRobots(robots);
		
		String dateStr = broadcastLines.get(0).split(" ")[0];
		DateTime stepTime = DateTime.parse(dateStr,formatter);
		DateTime currentTime;
		int step = 0;
		
		RobotLocation firstRL = PositionBroadcastMessage.decode(broadcastLines.get(0).split(" ")[1]);
		commoninterface.mathutils.Vector2d firstPos = CoordinateUtilities.GPSToCartesian(firstRL.getLatLon());
		
		for(Integer id : robotList.keySet()) {
			for(int i = 0 ; i < broadcastLines.size() ; i++) {
				
				RobotLocation rl = PositionBroadcastMessage.decode(broadcastLines.get(i).split(" ")[1]);
				String[] split = rl.getName().split("\\.");
				int logId = Integer.parseInt(split[split.length-1]);
				
				if(logId == id) {
					int position = robotList.get(id);
					commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
					robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
					robots.get(position).setOrientation(Math.toRadians(rl.getOrientation()));
					break;
				}
				
			}
		}
		
		int currentMessage = 0;
		
		AggregateFitness ff = new AggregateFitness(new Arguments("classname=evaluation.AggregateFitness,dontuse=1"));
		sim.addCallback(ff);
		
		if(file) {
			do{
				
				String line = broadcastLines.get(currentMessage++);
				
				String[] split = line.split(" ");
				String msg = split[1];
				currentTime = DateTime.parse(split[0],formatter);
				
				while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) > 100) {
					stepTime = stepTime.plus(100);
					step++;
					sim.performOneSimulationStep((double)step);
					System.out.println(step);
				}
				
				RobotLocation rl = PositionBroadcastMessage.decode(msg);
				
				commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
				
				split = rl.getName().split("\\.");
				int id = Integer.parseInt(split[split.length-1]);
				
				if(!excluded.contains(id)) {
				
					int position = robotList.get(id);
					
					robots.get(position).setPosition(pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y);
					robots.get(position).setOrientation(Math.toRadians(rl.getOrientation()));
				
				}
				
				renderer.drawFrame();
				renderer.repaint();
				
				
			} while(step < maxSteps && currentMessage < broadcastLines.size());
		} else {
			
			sim.addCallback(new WaterCurrent(new Arguments("maxspeed=0.1,fixedspeed=1,angle=-45")));
			
			String f = "compare/controllers/"+FILE+".conf";
			
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
			
			for(Robot r : robots) {
				AquaticDrone drone = (AquaticDrone)r;
				drone.processInformationRequest(bm, null);
			}
			
			for(int i = 0 ; i < maxSteps ; i++) {
				sim.performOneSimulationStep((double)i);
				renderer.drawFrame();
				renderer.repaint();
			}
			
		}
		
		System.out.println("Fitness: "+ff.getFitness());
		
	}
	
	public void pause() {
		this.pause = !pause;
	}
	
	public static void main(String[] args) {
		new JFrameViewerFitness();
	}
	
}

class JFrameViewerFitness extends JFrame{
	
	private Renderer renderer;
	private CompareFitnessBroadcast plot;
	
	public JFrameViewerFitness() {
		super("Position Plot");
		setLayout(new BorderLayout());
		
		final JFrameViewerFitness t = this;
		
		JButton button = new JButton("Replay");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(plot != null)
					plot.interrupt();
				plot = new CompareFitnessBroadcast(t);
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
