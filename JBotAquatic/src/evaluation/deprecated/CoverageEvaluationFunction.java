package evaluation.deprecated;

import java.util.ArrayList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoverageEvaluationFunction extends EvaluationFunction{
	
	private boolean isSetup = false;
	private double[][] coverage;
	private double resolution = 1;
	private double width = 5,height = 5;
	private double decrease = 1.0/(10*100);//1000 steps to go from 1.0 to 0.0
	
	@ArgumentsAnnotation(name="avoiddistance", defaultValue="0")
	private double avoidDistance = 0;
	
	private double v = 0;
	private double max = 0;
	private double penalty = 0;
	private double steps = 0;
	private double distance = 10;
	private double stopPenaltyMult = 0.5;
	private double foundMult = 1.0;
	private boolean kill = false;
	
	public CoverageEvaluationFunction(Arguments args) {
		super(args);
		resolution = args.getArgumentAsDoubleOrSetDefault("resolution", resolution);
		distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
		avoidDistance = args.getArgumentAsDoubleOrSetDefault("avoiddistance", avoidDistance);
		stopPenaltyMult = args.getArgumentAsDoubleOrSetDefault("stoppenaltymult", stopPenaltyMult);
		foundMult = args.getArgumentAsDoubleOrSetDefault("foundmult", foundMult);
		kill = args.getFlagIsTrue("kill");
	}
	
	public void setup(Simulator simulator) {
		width = simulator.getEnvironment().getWidth();
		height = simulator.getEnvironment().getHeight();
		coverage = new double[(int)(height/resolution)][(int)(width/resolution)];
		for(int y = 0 ; y < coverage.length ; y++) {
			for(int x = 0 ; x < coverage[y].length ; x++) {
				double coordX = (x - coverage[y].length/2)*resolution;
				double coordY = (y - coverage.length/2)*resolution;
				if(!insideLines(new Vector2d(coordX,coordY), simulator)) {
					coverage[y][x] = -1;
				} else {
					max++;
				}
			}
		}
	}
	
	public boolean insideLines(Vector2d v, Simulator sim) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		
		for(PhysicalObject p : sim.getEnvironment().getAllObjects()) {
			if(p.getType() == PhysicalObjectType.LINE) {
				Line l = (Line)p;
				if(l.intersectsWithLineSegment(v, new Vector2d(0,-Integer.MAX_VALUE)) != null)
					count++;
			}
		}
		return count % 2 != 0;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(!isSetup) {
			setup(simulator);
			isSetup = true;
			steps = simulator.getEnvironment().getSteps();
		}
		
		ArrayList<Robot> robots = simulator.getRobots();
		
		double sum = 0;
		
		for(int y = 0 ; y < coverage.length ; y++) {
			for(int x = 0 ; x < coverage[y].length ; x++) {
				
				if(coverage[y][x] == -1)
					continue;
				
				if(coverage[y][x] > 0) {
					 if(coverage[y][x] <= 1) {
						coverage[y][x]-=decrease;
						if(coverage[y][x] < 0)
							coverage[y][x] = 0;
					 }
				}
				
				if(coverage[y][x] > 0)
					 sum+=coverage[y][x];
			}
		}
		
		for(Robot r : robots) {
			if(r.isEnabled()) {
				AquaticDrone ad = (AquaticDrone)r;
				double speed = (Math.abs(ad.getLeftMotorSpeed()) + Math.abs(ad.getRightMotorSpeed())) / 2;
				penalty+=((speed)/robots.size()/steps)*stopPenaltyMult;
				
				if(insideLines(r.getPosition(), simulator)) {
					
					double rX = ad.getPosition().getX();
					double rY = ad.getPosition().getY();
					
					double minX = rX - distance;
					double minY = rY - distance;
					
					double maxX = rX + distance;
					double maxY = rY + distance;
					
					int pMinX = (int)((minX/resolution) + coverage.length/2);
					int pMinY = (int)((minY/resolution) + coverage[0].length/2);
					
					double pMaxX = (maxX/resolution) + coverage.length/2;
					double pMaxY = (maxY/resolution) + coverage[0].length/2;
				
					for(int y = pMinY ; y < pMaxY ; y++) {
						
						if(y >= coverage.length || y < 0)
							continue;
						
						for(int x = pMinX ; x < pMaxX ; x++) {
							
							if(x >= coverage[y].length || x < 0)
								continue;
							
							if(coverage[y][x] == -1)
								continue;
					
//							double px = (x - coverage[y].length/2)*resolution;
//							double py = (y - coverage.length/2)*resolution;
							
//							Vector2d p = new Vector2d(x,y);
							
//							if(p.distanceTo(r.getPosition()) < distance) {
								coverage[y][x] = 1.0;
//							}
						}
					}
				} else if(kill) {
					r.setEnabled(false);
				}
			}
		}
		
//		fitness = (v/max - penalty)/robots.size();
	
		fitness+= ((sum / max) / steps / robots.size()) * foundMult;
//		printGrid();
	}
	
	@Override
	public double getFitness() {
		return fitness - penalty;
	}
	
	public double[][] getCoverage() {
		return coverage;
	}
	
	private void printGrid() {
		System.out.println();
		System.out.println();
		for(int y = coverage.length-1 ; y >= 0 ; y--) {
			for(int x = 0 ; x < coverage[y].length ; x++) {
				if(coverage[y][x] == -1) {
					System.out.print(" ");
				} else if(coverage[y][x] == 0) {
					System.out.print("_");
				} else if(coverage[y][x] == 1) {
					System.out.print("#");
				} else {
					System.out.print("<");
				}
			}
			System.out.println();
		}
	}
}