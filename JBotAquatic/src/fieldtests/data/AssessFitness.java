package fieldtests.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import simulation.robot.AquaticDrone;
import simulation.robot.CISensorWrapper;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import updatables.CoverageTracer;
import updatables.PathTracer;
import commoninterface.AquaticDroneCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.sensors.GeoFenceCISensor;
import commoninterface.sensors.InsideBoundaryCISensor;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.LogData;
import fieldtests.evaluation.CoverageFitnessTest;
import fieldtests.updatables.Coverage;

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
		
		boolean useSim = false;
		boolean useReal = true;
		
		DoubleFitnessViewer viewer = null;
		
		if(gui) {
			viewer = new DoubleFitnessViewer();
		}
		
//		while(true) {
		
		int simSamples = 1;
		
		double[] samples = new double[simSamples];
		
		for(int i = 0 ; i < simSamples ; i++) {
			
			Setup setupReal = new Setup(exp, randomSeed, false, false);
			Setup setupSim = new Setup(exp, randomSeed, false, true);
			
			randomSeed++;
			
			DateTime stepTime = DateTime.parse(exp.logs.get(0).GPSdate,formatter);
			DateTime currentTime;
			int step = 0;
			
			if(useSim)
				startControllers(exp, setupSim);
			if(useReal)
				startControllers(exp, setupReal);
			
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
				PathTracer realPathTracer = new PathTracer(new Arguments("drawgeofence=0,linewidth=3,folder=path,bgcolor=0-0-0-0,altcolor=0-0-0-255,maincolor=255-000-000-255,fade=1,name="+exp.toString()+"_real"));
				setupReal.sim.addCallback(realPathTracer);
//				PathTracer simPathTracer = new PathTracer(new Arguments("drawgeofence=1,linewidth=3,folder=path,bgcolor=0-0-0-0,altcolor=0-0-0-255,maincolor=255-000-000-255,fade=0,name="+exp.toString()+"_sim"));
//				setupSim.sim.addCallback(simPathTracer);
			}
			
//			DISPERSION ADAPTIVE
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=600,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_1")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=600,timeend=1800,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_2")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=1800,timeend=2400,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_3")));
			
//			PATROL ADAPTIVE
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=1200,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_1")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=1200,timeend=1800,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_2")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=1,png=0,snapshotfrequency=5,timestart=1800,timeend=3600,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_3")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=3600,timeend=5400,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_4")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=5400,timeend=6600,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_5")));
			
//			AGG WP
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=600,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_1")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=1200,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_2")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=1800,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_3")));
//			setupReal.sim.addCallback(new PathTracer(new Arguments("drawgeofence=0,png=0,snapshotfrequency=5,timestart=0,timeend=2400,linewidth=3,bgcolor=0-0-0-0,altcolor=255-0-0-255,maincolor=255-0-000-255,fade=1,folder=path/"+exp.toString()+"_4")));
			
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			
			boolean stopRun = false;
			
			
			HashMap<String,LatLon> startLatLon = new HashMap<String,LatLon>();
			HashMap<String,LatLon> endLatLon = new HashMap<String,LatLon>();
			HashMap<String,Integer> time = new HashMap<String,Integer>();
			
			double distanceToTarget = 2;
			
			boolean measureSpeed = false;
			
			HashMap<Integer,LatLon> currentLatLonSim = new HashMap<Integer,LatLon>();
			HashMap<Integer,LatLon> currentLatLonReal = new HashMap<Integer,LatLon>();
			double[] totalDistanceSim = new double[8];
			double[] totalDistanceReal = new double[8];
			
			for(LogData d : exp.logs) {
				
				if(stopRun)
					break;
				
				
				if(d.comment != null) {
//					System.out.println(step+" "+d.comment);
					continue;
				}
				
				currentTime = DateTime.parse(d.GPSdate,formatter);
				
				while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) >= 100) {
					
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
					stepTime = stepTime.plus(100);
					if(useReal) {
						setupReal.sim.performOneSimulationStep((double)step);
					}
					if(measureSpeed) {
						if(step % 10 == 0) {
							for(Robot r : setupSim.sim.getRobots()) {
								mathutils.Vector2d vec = ((AquaticDrone)r).getPosition();
								LatLon current = CoordinateUtilities.cartesianToGPS(new Vector2d(vec.x,vec.y));
								if(currentLatLonSim.get(r.getId()) == null) {
									currentLatLonSim.put(r.getId(), current);
								}else {
									totalDistanceSim[r.getId()]+=current.distanceInKM(currentLatLonSim.get(r.getId()))*1000;
									currentLatLonSim.put(r.getId(), current);
								}
								
							}
							for(Robot r : setupReal.sim.getRobots()) {
								mathutils.Vector2d vec = ((AquaticDrone)r).getPosition();
								LatLon current = CoordinateUtilities.cartesianToGPS(new Vector2d(vec.x,vec.y));
								if(currentLatLonReal.get(r.getId()) == null) {
									currentLatLonReal.put(r.getId(), current);
								}else {
									totalDistanceReal[r.getId()]+=current.distanceInKM(currentLatLonReal.get(r.getId()))*1000;
									currentLatLonReal.put(r.getId(), current);
								}
							}
						}
					}
					
					if(useSim)
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
				
//				if(!ignoreReal) {
//					setupSim.updateRobotEntities(d);
//					setupReal.updateRobotEntities(d);
//				}
				
				 //THIS IS FOR WAYPOINT BEHAVIORS
				if(exp.controllerName.startsWith("waypoint")) {
					if(exp.activeRobot != -1 && d.ip.endsWith("."+exp.activeRobot)) {
						if(d.inputNeuronStates == null)
							useReal = false;
						
						//Real
						if(getDistanceToTarget(setupReal.getRobot("."+exp.activeRobot)) < distanceToTarget)
							useReal = false;
	
						//Sim
						if(getDistanceToTarget(setupSim.getRobot("."+exp.activeRobot)) < distanceToTarget) {
							stopRun = true;
						}
						
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
					
					if(startLatLon.get(d.ip) == null) {
						startLatLon.put(d.ip, setupSim.getShiftedLatLon(rlStopped.getLatLon()));
					}
					
					endLatLon.put(d.ip, setupSim.getShiftedLatLon(rlStopped.getLatLon()));
					time.put(d.ip, step);
					
				}
				
				if(!useReal)
					continue;
				
				commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
				
				int id = Integer.parseInt(rl.getName());
				int position = setupReal.robotList.get(id);
				
				setupReal.robots.get(position).setPosition(pos.x+setupReal.start.x-setupReal.firstPos.x, pos.y+setupReal.start.y-setupReal.firstPos.y);
				
//				temp.addPoint(setupReal.sim,setupReal.robots.get(position).getPosition(), d.temperatures[1]);
//				tempTracer.setCoverage(temp.getCoverage(), setupReal.resolution);
				
//				System.out.println(step+"\t"+d.ip+"\t"+(pos.x+setupReal.start.x-setupReal.firstPos.x)+"\t"+(pos.y+setupReal.start.y-setupReal.firstPos.y)+"\t"+d.temperatures[1]);
				
//				if(min > d.temperatures[1])
//					min = d.temperatures[1];
//				if(max < d.temperatures[1])
//					max = d.temperatures[1];
				
				double orientation = 360 - (rl.getOrientation() - 90);
				setupReal.robots.get(position).setOrientation(Math.toRadians(orientation));
				
//				System.out.println(GeoFence.getGeoFences((AquaticDroneCI)setupSim.sim.getRobots().get(0)));
//				for(Waypoint wp : GeoFence.getGeoFences((AquaticDroneCI)setupSim.sim.getRobots().get(0)).get(0).getWaypoints()) {
//					pos = CoordinateUtilities.GPSToCartesian(wp.getLatLon());
//					System.out.println(pos);
//				}
//				System.out.println();
				
				
			}
			
//			while(!stopRun) {
//				setupSim.sim.performOneSimulationStep((double)step);
//				step++;
//				
//				if(gui) {
//					setupSim.renderer.drawFrame();
//					setupSim.renderer.repaint();
//				}
//				
//				if(getDistanceToTarget(setupSim.getRobot("."+exp.activeRobot)) < distanceToTarget) {
//					stopRun = true;
//				}
//			}
			
//			double avg = 0;
//			double count = 0;
//			
//			for(String ss : time.keySet()) {
//				count++;
//				double meters = endLatLon.get(ss).distance(startLatLon.get(ss))*1000.0;
//				double metersPerSecond = meters/ (time.get(ss)/10.0);
//				avg+= metersPerSecond;
//			}
//			
//			avg/=count;
//			
//			System.out.println(exp+" "+avg+" m/s");
			
			setupReal.sim.terminate();
			setupSim.sim.terminate();
			
			if(measureSpeed) {
				double totalReal = 0;
				double totalSim = 0;
				for(int j = 0 ; j < totalDistanceSim.length ; j++) {
					totalReal+= totalDistanceReal[j]/8.0/step;
					System.out.println("Speed real: "+j+" "+totalDistanceReal[j]/step);
					totalSim+=totalDistanceSim[j]/8.0/step;
				}
				for(int j = 0 ; j < totalDistanceSim.length ; j++) {
					System.out.println("Speed sim: "+j+" "+totalDistanceSim[j]/step);
				}
				System.out.println("AVG SPEEDS "+totalReal+" "+totalSim);
			}
			
//			System.out.println(min+" min max "+max);
			
			samples[i] = setupSim.eval.getFitness();
			
//			System.out.println();
			
			if(simSamples == 1)
				System.out.println(setupReal.eval.getFitness()+" "+setupSim.eval.getFitness());
		}
		
		
		if(simSamples > 1) {
//			System.out.println(exp);
			for(int i = 0 ; i < samples.length ; i++) {
				System.out.println(samples[i]);
			}
			System.out.println();
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
			
			CIArguments readArguments = getConfigurationFile(f); 
			
			BehaviorMessage bm = new BehaviorMessage(readArguments.getArgumentAsString("type"), readArguments.getCompleteArgumentString(), true, "dude");
			
			for(int i = 0 ; i < setup.robots.size() ; i++) {
				Robot r = setup.robots.get(i); 
				if(exp.activeRobot != -1) {
					if(r.getId() == setup.getRobot(""+exp.activeRobot).getId()) {
						AquaticDrone drone = (AquaticDrone)r;
						drone.processInformationRequest(bm, null);
					}
				} else {
					
					AquaticDrone drone = (AquaticDrone)r;
					
					if(exp.controllerName.contains("hierarchical") && i == setup.robots.size() - 1) {
						CIArguments preprogArgs = getConfigurationFile("compare/controllers/preprog_waypoint.conf"); 
						BehaviorMessage preprogbm = new BehaviorMessage(preprogArgs.getArgumentAsString("type"), preprogArgs.getCompleteArgumentString(), true, "dude");
						drone.processInformationRequest(preprogbm, null);
						drone.setDroneType(DroneType.ENEMY);
						drone.setGpsError(0);
					} else {
						CIArguments readArguments2 = new CIArguments(readArguments.getCompleteArgumentString());
						readArguments2.setArgument("ip"+drone.getNetworkAddress(), (0.5+0.5/(setup.robots.size()-1-1)*i));
						bm = new BehaviorMessage(readArguments2.getArgumentAsString("type"), readArguments2.getCompleteArgumentString(), true, "dude");
						drone.processInformationRequest(bm, null);
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static CIArguments getConfigurationFile(String filename) throws FileNotFoundException{
		Scanner s = new Scanner(new File(filename));
		filename="";
		while(s.hasNextLine()) {
			filename+=s.nextLine()+"\n";
		}
		s.close();
		
		String full = filename.replaceAll("\\s+", "");
		
		return new CIArguments(full);
	}
	

}
