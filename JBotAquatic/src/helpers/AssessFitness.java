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
import commoninterface.entities.Waypoint;
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
						Thread.sleep(1);
					} catch (InterruptedException e) {}
				}
				
			}
			
			if(setup.sim.simulationFinished())
				break;
			
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
			
			while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) >= 100) {
				stepTime = stepTime.plus(100);
				
				setup.sim.performOneSimulationStep((double)step);
				step++;
				
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
			
			if(setup.sim.simulationFinished())
				break;
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
		
		int s = 1;
		
		double[] samples = new double[s];
		
		for(int i = 0 ; i < s ; i++) {
			
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
			boolean tempEnabled = false;
			boolean pathTracer = false;
			
			//TEMP
			if(tempEnabled) {
				temp.setup(setupReal.sim);
				tempTracer.setCoverage(temp.getCoverage(),setupReal.resolution);
				setupReal.sim.addCallback(temp);
				setupReal.sim.addCallback(tempTracer);
			}
			
			if(coverage && setupSim.eval instanceof CoverageFitnessTest) {
				
				//COVERAGE
				CoverageTracer simCoverageTracer = new CoverageTracer(new Arguments("drawgeofence=0,gradient=0,altcolor=0-0-0-255,png=1,maincolor=46-203-206-255,bgcolor=0-0-0-0,folder=coverage/"+exp.toString()+"_sim"));
				CoverageFitnessTest simEval = (CoverageFitnessTest)setupSim.eval;
				simCoverageTracer.setCoverage(simEval.getCoverage(),setupSim.resolution);
				setupSim.sim.addCallback(simCoverageTracer);

				CoverageTracer realCoverageTracer = new CoverageTracer(new Arguments("drawgeofence=0,gradient=0,altcolor=255-0-0-255,png=1,maincolor=46-203-206-255,bgcolor=0-0-0-0,folder=coverage/"+exp.toString()+"_real"));
//				CoverageTracer realCoverageTracer = new CoverageTracer(new Arguments("png=1,sequence=1,linewidth=3,snapshotfrequency=5,scale=10,gradient=0,altcolor=255-0-0-255,png=1,maincolor=255-255-000-255,bgcolor=0-0-0-0,folder=coverage/"+exp.toString()+"_real"));
				CoverageFitnessTest realEval = (CoverageFitnessTest)setupReal.eval;
				realCoverageTracer.setCoverage(realEval.getCoverage(),setupReal.resolution);
				setupReal.sim.addCallback(realCoverageTracer);
				
			}
			
			if(pathTracer) {
				PathTracer realPathTracer = new PathTracer(new Arguments("drawgeofence=1,linewidth=3,folder=path,bgcolor=0-0-0-0,altcolor=0-0-0-255,maincolor=255-000-000-255,fade=0,name="+exp.toString()+"_real"));
				setupReal.sim.addCallback(realPathTracer);
				PathTracer simPathTracer = new PathTracer(new Arguments("drawgeofence=1,linewidth=3,folder=path,bgcolor=0-0-0-0,altcolor=0-0-0-255,maincolor=255-000-000-255,fade=0,name="+exp.toString()+"_sim"));
				setupSim.sim.addCallback(simPathTracer);
			}
			
//			DISPERSION ADAPTIVE
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=400,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_1")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=400,timeend=758,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_2")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=758,timeend=1138,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_3")));
			
//			PATROL ADAPTIVE
			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=1200,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_1")));
			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=1200,timeend=1800,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_2")));
			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=1,png=0,snapshotfrequency=5,timestart=1800,timeend=3600,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_3")));
			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=3600,timeend=5400,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_4")));
			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=5400,timeend=6600,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_5")));
			
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			
			boolean stopRun = false;
			boolean ignoreReal = false;
			
			for(LogData d : exp.logs) {
				
				if(stopRun)
					break;
				
				
				if(d.comment != null) {
//					System.out.println(step+" "+d.comment);
					continue;
				}
				
				currentTime = DateTime.parse(d.GPSdate,formatter);
				
				while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) >= 100) {
					stepTime = stepTime.plus(100);
					
					if(!ignoreReal) {
						setupReal.sim.performOneSimulationStep((double)step);
						
					}
					
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
				if(exp.controllerName.startsWith("waypoint")) {
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
				}
				
				if(exp.activeRobot != -1 && !d.ip.endsWith("."+exp.activeRobot)) {
					if(d.inputNeuronStates != null)
						continue;
					
					//force location of immobile robots
					RobotLocation rlStopped = Setup.getRobotLocation(d);
					Robot rStopped = setupSim.getRobot(d.ip);
					Vector2d  vecStopped = CoordinateUtilities.GPSToCartesian(setupSim.getShiftedLatLon(rlStopped.getLatLon()));
					rStopped.setPosition(vecStopped.x, vecStopped.y);
					
				}
				
				if(ignoreReal)
					continue;
				
				commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
				
				int id = Integer.parseInt(rl.getName());
				int position = setupReal.robotList.get(id);
				
				setupReal.robots.get(position).setPosition(pos.x+setupReal.start.x-setupReal.firstPos.x, pos.y+setupReal.start.y-setupReal.firstPos.y);
				
				temp.addPoint(setupReal.sim,setupReal.robots.get(position).getPosition(), d.temperatures[1]);
				tempTracer.setCoverage(temp.getCoverage(), setupReal.resolution);
				
//				System.out.println(pos.x+" "+pos.y+" "+d.temperatures[1]);
				
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
			
			samples[i] = setupSim.eval.getFitness();
			
//			System.out.println();
			
			if(s == 1)
				System.out.println(setupReal.eval.getFitness()+" "+setupSim.eval.getFitness());
		}
		
		if(s > 1) {
//			System.out.println(exp);
			for(int i = 0 ; i < samples.length ; i++) {
				System.out.println(samples[i]);
			}
		}
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
