package evaluation;

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
		
		updateAll();
		
		ArrayList<Robot> robots = simulator.getRobots();
		
		for(int y = 0 ; y < coverage.length ; y++) {
			for(int x = 0 ; x < coverage[y].length ; x++) {
				
				if(coverage[y][x] == -1)
					continue;
				
				double px = (x - coverage[y].length/2)*resolution;
				double py = (y - coverage.length/2)*resolution;
				
				Vector2d p = new Vector2d(px,py);
				
				for(Robot r : robots) {
					if(r.isEnabled() && insideLines(r.getPosition(), simulator)) {
						if(p.distanceTo(r.getPosition()) < distance) {
							coverage[y][x] = 1.0;
						}
					} else if(kill) {
						r.setEnabled(false);
					}
				}
			}
		}
		
		for(Robot r : robots) {
			if(r.isEnabled()) {
				AquaticDrone ad = (AquaticDrone)r;
				double speed = (Math.abs(ad.getLeftMotorSpeed()) + Math.abs(ad.getRightMotorSpeed())) / 2;
				penalty+=((speed)/robots.size()/steps)*stopPenaltyMult;
			}
		}
		
//		fitness = (v/max - penalty)/robots.size();
	
		fitness+= ((countAll() / max) / steps / robots.size()) * foundMult;
//		printGrid();
	}
	
	@Override
	public double getFitness() {
		return fitness - penalty;
	}
	
	private void updateAll() {
		for(int i = 0 ; i < coverage.length ; i++) {
			for(int j = 0 ; j < coverage[i].length ; j++) {
				if(coverage[i][j] > 0) {
					 if(coverage[i][j] <= 1) {
						coverage[i][j]-=decrease;
						if(coverage[i][j] < 0)
							coverage[i][j] = 0;
					 }
				}
			}
		}
	}
	
	private double countAll() {
		
		double sum = 0;
		
		for(int i = 0 ; i < coverage.length ; i++) {
			for(int j = 0 ; j < coverage[i].length ; j++) {
				if(coverage[i][j] > 0) {
					 sum+=coverage[i][j];
				}
			}
		}
		
		return sum;
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