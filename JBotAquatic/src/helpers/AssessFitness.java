package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.WaterCurrent;
import commoninterface.CIBehavior;
import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.entities.RobotLocation;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.logger.LogData;
import controllers.DroneNeuralNetworkController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import gui.renderer.TwoDRenderer;

public class AssessFitness {
	
	static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
	
	public static double getRealFitness(Experiment exp, long randomSeed) {
		return getRealFitness(exp, randomSeed, false);
	}
	
	public static double getSimulatedFitness(Experiment exp, long randomSeed) {
		
		return getSimulatedFitness(exp, randomSeed, false);
	}
	
	public static double getRealFitness(Experiment exp, long randomSeed, boolean gui) {
		Setup setup = new Setup(exp, randomSeed, gui);
		
		DateTime stepTime = DateTime.parse(exp.logs.get(0).GPSdate,formatter);
		DateTime currentTime;
		int step = 0;
		
		int i = 0;
		for(LogData d : exp.logs) {
			if(d.ip != null && d.ip.endsWith(".2") && d.inputNeuronStates != null) {
				System.out.print((i++)+" ");
				for(double dd : d.inputNeuronStates)
					System.out.print(dd+" ");
				System.out.println();
			}
		}
		
		for(LogData d : exp.logs) {
			
			if(d.comment != null)
				continue;
			
			currentTime = DateTime.parse(d.GPSdate,formatter);
			
			while(Math.abs(stepTime.getMillis()-currentTime.getMillis()) > 100) {
				stepTime = stepTime.plus(100);
				step++;
				setup.sim.performOneSimulationStep((double)step);
				
				if(gui) {
					setup.renderer.drawFrame();
					setup.renderer.repaint();
//					System.out.println(step);
//					try {
//						Thread.sleep(20);
//					} catch (InterruptedException e) {}
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
		
		return setup.eval.getFitness();
	}
	
	public static double getSimulatedFitness(Experiment exp, long randomSeed, boolean gui) {
		
		Setup setup = new Setup(exp, randomSeed, gui);
		
		DateTime stepTime = DateTime.parse(exp.logs.get(0).GPSdate,formatter);
		DateTime currentTime;
		int step = 0;
		
		startControllers(exp, setup);
		
		for(LogData d : exp.logs) {
			
			if(d.comment != null || d.ip == null)
				continue;
			
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
		
		return setup.eval.getFitness();
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
			
			BehaviorMessage bm = new BehaviorMessage("ControllerCIBehavior", f.replaceAll("\\s+", ""), true, "dude");
			
			for(Robot r : setup.robots) {
				AquaticDrone drone = (AquaticDrone)r;
				drone.processInformationRequest(bm, null);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	

}
