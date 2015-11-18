package fieldtests.data;

import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
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
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import commoninterface.controllers.Figure8CIBehavior;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogData;

public class PositionPlot extends Thread{
	
	private String file;
	private JFrameViewer frame;
	private ArrayList<String> lines = new ArrayList<String>();
	private boolean pause = false;
	private boolean useFile = true;
	
	public PositionPlot(String file, JFrameViewer frame) {
		this.file = file;
		this.frame = frame;
		
		if(!useFile)
			return;
		
		try {
		
			Scanner s = new Scanner(new File(file));
		
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(!line.startsWith("#") && !line.isEmpty())
					lines.add(line);
			}
			
			System.out.println(lines.size());
			
			s.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void alternativeRun() {
		Vector2d start = new Vector2d(0,0);
		
		HashMap<String, Arguments> hash = new HashMap<String, Arguments>();
		hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=20,height=20",true));
		
		ArrayList<Robot> robots = new ArrayList<Robot>();
		
		Simulator sim = new Simulator(new Random(1), hash);
		AquaticDrone drone = new AquaticDrone(sim, new Arguments("commrange=10,rudder=1"));
		drone.setPosition(start);
		robots.add(drone);
		
		TwoDRenderer renderer = new TwoDRenderer(new Arguments("bigrobots=1,drawframes=1"));
		renderer.setSimulator(sim);
		renderer.drawFrame();
		
		frame.setRenderer(renderer);
		frame.validate();
		
		sim.addRobots(robots);
		
		Environment env = sim.getEnvironment();
		drone.updateSensors(0, new ArrayList<PhysicalObject>());
		
		double h = 1; // 0 to 1!
		double s = 0.5;
		
		int i = 0;
		
		double minX = 0, maxX = 0;
		
		while(true) {
			
			if(pause) {
				i--;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			
			drone.setRudder(h, s);
			if(i > 60) {
				drone.setMotorSpeeds(0, 0);
			}
			sim.performOneSimulationStep((double)i);
			renderer.drawFrame();
			renderer.repaint();
			
			LightPole simPole = new LightPole(sim, "lp"+i++, drone.getPosition().x, drone.getPosition().y, 0.05);
			minX = Math.min(minX,drone.getPosition().x);
			maxX = Math.max(maxX,drone.getPosition().x);
			simPole.setColor(Color.red);
			
			env.addStaticObject(simPole);
			
//			System.out.println(i+" "+drone.getPosition());
			if(i > 100) {
				System.out.println(i+" "+(maxX-minX));
				break;
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		
		if(!useFile) {
			alternativeRun();
			return;
		}
		
		Vector2d start = new Vector2d(0,0);
		
		HashMap<String, Arguments> hash = new HashMap<String, Arguments>();
		hash.put("--environment", new Arguments("classname=EmptyEnvironment,width=20,height=20",true));
		
		ArrayList<Robot> robots = new ArrayList<Robot>();
		
		Simulator sim = new Simulator(new Random(1), hash);
		AquaticDrone drone = new AquaticDrone(sim, new Arguments("commrange=10,rudder=1"));
		drone.setPosition(start);
		robots.add(drone);
		
		TwoDRenderer renderer = new TwoDRenderer(new Arguments("bigrobots=1,drawframes=5"));
		renderer.setSimulator(sim);
		renderer.drawFrame();
		
		frame.setRenderer(renderer);
		frame.validate();
		
		sim.addRobots(robots);
		
		Environment env = sim.getEnvironment();
		
		commoninterface.utils.logger.DecodedLog dld = LogCodex.decodeLog(lines.get(0));
		LogData l = (LogData)dld.getPayload();
		
		double lat = l.latLon.getLat();
		double lon = l.latLon.getLon();
		
		double originalOrientation = l.GPSorientation + 50;//l.compassOrientation; 
		double orientation = 360 - (originalOrientation - 90);
		
		drone.setOrientation(Math.toRadians(orientation));
		drone.updateSensors(0, new ArrayList<PhysicalObject>());
		
		LatLon latLon = new LatLon(lat,lon);
		commoninterface.mathutils.Vector2d firstPos = CoordinateUtilities.GPSToCartesian(latLon);
		
		double mm = 50000000; double mM = -5000000;
		
		drone.startBehavior(new Figure8CIBehavior(new CIArguments(""), drone));
		
		for(int i = 0 ; i < lines.size() ; i++) {
			
			dld = LogCodex.decodeLog(lines.get(i));
			l = (LogData)dld.getPayload();
			
			if(pause) {
				i--;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			
			orientation = 360 - l.compassOrientation - 90;
//			drone.setOrientation(Math.toRadians(orientation));
			
			double left = l.motorSpeeds[0];
			double right = l.motorSpeeds[1];
			
//			System.out.println(left+" "+right);
			
			lat = l.latLon.getLat();
			lon = l.latLon.getLon();
			
			latLon = new LatLon(lat,lon);
			
//			drone.setMotorSpeeds(left, right);
			
			sim.performOneSimulationStep((double)i);
			renderer.drawFrame();
			renderer.repaint();
			
			commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(latLon);
			
			LightPole simPole = new LightPole(sim, "lp"+i, drone.getPosition().x, drone.getPosition().y, 0.05);
			LightPole realPole = new LightPole(sim, "lp"+i, pos.x+start.x-firstPos.x, pos.y+start.y-firstPos.y, 0.05);
			
			mm = Math.min(pos.x,mm);
			mM = Math.max(pos.x,mM);
			
			System.out.println(mM-mm);
			
			simPole.setColor(Color.red);
			realPole.setColor(Color.green);
			
			env.addStaticObject(simPole);
			env.addStaticObject(realPole);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void pause() {
		this.pause = !pause;
	}
	
	public static void main(String[] args) {
		String file = "logs/figure8.log";
		
		new JFrameViewer(file);
	}
	
}

class JFrameViewer extends JFrame{
	
	private String file;
	private Renderer renderer;
	private PositionPlot plot;
	
	public JFrameViewer(String file) {
		super("Position Plot");
		this.file = file;
		setLayout(new BorderLayout());
		
		final JFrameViewer t = this;
		
		JButton button = new JButton("Replay");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(plot != null)
					plot.interrupt();
				plot = new PositionPlot(t.file,t);
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
