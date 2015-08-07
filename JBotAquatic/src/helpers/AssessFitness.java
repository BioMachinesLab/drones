package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import simulation.Updatable;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.CoverageTracer;
import updatables.PathTracer;
import commoninterface.entities.RobotLocation;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.LogData;
import evaluation.CoverageFitnessTest;
import evaluation.deprecated.CoverageEvaluationFunction;

public class AssessFitness {
	
	static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
	
	public static double getRealFitness(Experiment exp, long randomSeed) {
		return getRealFitness(exp, randomSeed, false);
	}
	
	public static double getSimulatedFitness(Experiment exp, long randomSeed) {
		return getSimulatedFitness(exp, randomSeed, false);
	}
	
	public static double getRealFitness(Experiment exp, long randomSeed, boolean gui) {
		Setup setup = new Setup(exp, randomSeed, gui, false);
		
		DateTime stepTime = DateTime.parse(exp.logs.get(0).GPSdate,formatter);
		DateTime currentTime;
		int step = 0;
		
		for(LogData d : exp.logs) {
			
			if(d.comment != null)
				continue;
			
			setup.updateRobotEntities(d);
			
			currentTime = DateTime.parse(d.GPSdate,formatter);
			
			while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) >= 100) {
				stepTime = stepTime.plus(100);
				setup.sim.performOneSimulationStep((double)step);
				step++;
				
				if(gui) {
					setup.renderer.drawFrame();
					setup.renderer.repaint();
//					System.out.println(step);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {}
				}
				
			}
			RobotLocation rl = Setup.getRobotLocation(d);
			
			if(rl == null || rl.getLatLon() == null)
				continue;
			
			commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
			
			int id = Integer.parseInt(rl.getName());
			int position = setup.robotList.get(id);
			
			setup.robots.get(position).setPosition(pos.x+setup.start.x-setup.firstPos.x, pos.y+setup.start.y-setup.firstPos.y);
			double orientation = 360 - (rl.getOrientation() - 90);
			setup.robots.get(position).setOrientation(Math.toRadians(orientation));
			
		}
		
		setup.sim.terminate();
		
		return setup.eval.getFitness();
	}
	
	public static double getSimulatedFitness(Experiment exp, long randomSeed, boolean gui) {
		
		Setup setup = new Setup(exp, randomSeed, gui, true);
		
		DateTime stepTime = DateTime.parse(exp.logs.get(0).GPSdate,formatter);
		DateTime currentTime;
		int step = 0;
		
		startControllers(exp, setup);
		
		for(LogData d : exp.logs) {
			
			if(d.comment != null || d.ip == null)
				continue;
			
			setup.updateRobotEntities(d);
			
			currentTime = DateTime.parse(d.GPSdate,formatter);
			
//			Robot r = setup.getRobot(d.ip);
//			RobotLocation rl = Setup.getRobotLocation(d);
//			
//			commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
//			
//			r.setPosition(pos.x+setup.start.x-setup.firstPos.x, pos.y+setup.start.y-setup.firstPos.y);
//			double orientation = 360 - (rl.getOrientation() - 90);
//			r.setOrientation(Math.toRadians(orientation));
			
			while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) > 100) {
				stepTime = stepTime.plus(100);
				step++;
				setup.sim.performOneSimulationStep((double)step);
				
//				AquaticDrone aq = (AquaticDrone) setup.getRobot("192.168.3.2");
//				ControllerCIBehavior b = (ControllerCIBehavior)aq.getActiveBehavior();
//				System.out.print(step+" ");
//				for(double dd : b.getNeuralNetwork().getInputNeuronStates())
//					System.out.print(dd+" ");
//				System.out.println();
				
				if(gui) {
					setup.renderer.drawFrame();
					setup.renderer.repaint();
//					System.out.println(step);
//					try {
//						Thread.sleep(20);
//					} catch (InterruptedException e) {}
				}
			}
		}
		
		setup.sim.terminate();
		
		return setup.eval.getFitness();
	}
	
	public static void compareFitness(Experiment exp, long randomSeed, boolean gui) {
		
		DoubleFitnessViewer viewer = null;
		
		if(gui) {
			viewer = new DoubleFitnessViewer();
		}
		
//		while(true) {
			
			Setup setupReal = new Setup(exp, randomSeed, false, false);
			Setup setupSim = new Setup(exp, randomSeed, false, true);
			
			randomSeed++;
			
			DateTime stepTime = DateTime.parse(exp.logs.get(0).GPSdate,formatter);
			DateTime currentTime;
			int step = 0;
			
			startControllers(exp, setupSim);
			
			if(gui) {
				viewer.setRenderer1(setupReal.getRenderer());
				viewer.setRenderer2(setupSim.getRenderer());
				viewer.validate();
			}
			
			Coverage temp = new Coverage(new Arguments("resolution="+setupReal.resolution+",distance=5,decrease=0,min=20,max=25"));
			CoverageTracer tempTracer = new CoverageTracer(new Arguments("bgcolor=0-0-0-0,min=20,max=25,color=1,folder=temp/"+exp.toString()));
			
			boolean coverage = false;
			
			if(coverage && setupSim.eval instanceof CoverageFitnessTest) {
				
				//TEMP
				tempTracer.setCoverage(temp.getCoverage(),setupReal.resolution);
				setupReal.sim.addCallback(temp);
				setupReal.sim.addCallback(tempTracer);
			
				//COVERAGE
//				CoverageTracer simCoverageTracer = new CoverageTracer(new Arguments("bgcolor=0-0-0-0,folder=coverage/"+exp.toString()+"_sim"));
//				CoverageTracer realCoverageTracer = new CoverageTracer(new Arguments("bgcolor=0-0-0-0,folder=coverage/"+exp.toString()+"_real"));
//				
//				CoverageFitnessTest simEval = (CoverageFitnessTest)setupSim.eval;
//				CoverageFitnessTest realEval = (CoverageFitnessTest)setupReal.eval;
//				
//				simCoverageTracer.setCoverage(simEval.getCoverage(),setupSim.resolution);
//				realCoverageTracer.setCoverage(realEval.getCoverage(),setupReal.resolution);
//				
//				setupReal.sim.addCallback(realCoverageTracer);
//				setupSim.sim.addCallback(simCoverageTracer);
				
				
				for(Integer id : setupReal.robotList.keySet()) {
					for(LogData d : exp.logs) {
						if(d.ip.endsWith("."+id) && d.temperatures != null) {
							temp.addPoint(setupReal.sim,setupReal.getRobot(d.ip).getPosition(), d.temperatures[1]);
							break;
						}
					}
				}
				temp.update(setupReal.sim);
			}
			
//			,timestart=0,timeend=1200
//			,timestart=1200,timeend=1800
//			,timestart=1800,timeend=3600
//			,timestart=3600,timeend=5400
//			,timestart=5400,timeend=6600
			PathTracer simPathTracer = new PathTracer(new Arguments("folder=path,bgcolor=0-0-0-0,fade=1,name="+exp.toString()+"_sim"));
			PathTracer realPathTracer = new PathTracer(new Arguments("folder=path,bgcolor=0-0-0-0,fade=1,name="+exp.toString()+"_real"));
			
			setupSim.sim.addCallback(simPathTracer);
			setupReal.sim.addCallback(realPathTracer);
			
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			
			boolean stopRun = false;
			boolean ignoreReal = false;
			
			for(LogData d : exp.logs) {
				
				if(stopRun)
					break;
				
				if(d.comment != null)
					continue;
				
				currentTime = DateTime.parse(d.GPSdate,formatter);
				
				while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) >= 100) {
					stepTime = stepTime.plus(100);
					
					if(!ignoreReal)
						setupReal.sim.performOneSimulationStep((double)step);
					
					setupSim.sim.performOneSimulationStep((double)step);
					step++;
					
					if(gui) {
						setupSim.renderer.drawFrame();
						setupReal.renderer.drawFrame();
						setupSim.renderer.repaint();
						setupReal.renderer.repaint();
					}
					
				}
				RobotLocation rl = Setup.getRobotLocation(d);
				
				if(rl == null || rl.getLatLon() == null)
					continue;
				
				if(!ignoreReal) {
					setupSim.updateRobotEntities(d);
					setupReal.updateRobotEntities(d);
				}
				
				 //THIS IS FOR WAYPOINT BEHAVIORS
				if(exp.activeRobot != -1 && d.ip.endsWith("."+exp.activeRobot)) {
					if(d.inputNeuronStates == null)
						ignoreReal = true;
					
					double distanceToTarget = 2;
					
					//Real
					if(getDistanceToTarget(setupReal.getRobot("."+exp.activeRobot)) < distanceToTarget) 
						ignoreReal = true;

					//Sim
					if(getDistanceToTarget(setupSim.getRobot("."+exp.activeRobot)) < distanceToTarget) 
						stopRun = true;
					
				}
				
				if(exp.activeRobot != -1 && !d.ip.endsWith("."+exp.activeRobot)) {
					if(d.inputNeuronStates != null)
						continue;
				}
				
				if(ignoreReal)
					continue;
				
				commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
				
				int id = Integer.parseInt(rl.getName());
				int position = setupReal.robotList.get(id);
				
				setupReal.robots.get(position).setPosition(pos.x+setupReal.start.x-setupReal.firstPos.x, pos.y+setupReal.start.y-setupReal.firstPos.y);
				
				temp.addPoint(setupReal.sim,setupReal.robots.get(position).getPosition(), d.temperatures[1]);
				tempTracer.setCoverage(temp.getCoverage(), setupReal.resolution);
				
				if(min > d.temperatures[1])
					min = d.temperatures[1];
				if(max < d.temperatures[1])
					max = d.temperatures[1];
				
				double orientation = 360 - (rl.getOrientation() - 90);
				setupReal.robots.get(position).setOrientation(Math.toRadians(orientation));
				
			}
			
			setupReal.sim.terminate();
			setupSim.sim.terminate();
			
//			System.out.println(min+" min max "+max);
			
			System.out.println(setupReal.eval.getFitness()+" "+setupSim.eval.getFitness());
//		}
	}
	
	private static double getDistanceToTarget(Robot r) {
		AquaticDrone drone = (AquaticDrone)r;
		LatLon wp = drone.getActiveWaypoint().getLatLon();
		Vector2d v = CoordinateUtilities.GPSToCartesian(wp);
		return drone.getPosition().distanceTo(new mathutils.Vector2d(v.x,v.y));
	}
	
	private static void startControllers(Experiment exp, Setup setup) {
		String f = "compare/controllers/preset_"+exp.controllerName+exp.controllerNumber+".conf";
		
		try {
			Scanner s = new Scanner(new File(f));
			f="";
			while(s.hasNextLine()) {
				f+=s.nextLine()+"\n";
			}
			s.close();
			
			String full = f.replaceAll("\\s+", "");
			
			CIArguments readArguments = new CIArguments(full);
			
			BehaviorMessage bm = new BehaviorMessage(readArguments.getArgumentAsString("type"), full, true, "dude");
			
			for(Robot r : setup.robots) {
				if(exp.activeRobot != -1) {
					if(r.getId() == setup.getRobot(""+exp.activeRobot).getId()) {
						AquaticDrone drone = (AquaticDrone)r;
						drone.processInformationRequest(bm, null);
					}
				} else {
					AquaticDrone drone = (AquaticDrone)r;
					drone.processInformationRequest(bm, null);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	

}
