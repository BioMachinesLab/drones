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
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class DroneGeoFenceEvaluationFunction extends EvaluationFunction{
	
	private boolean isSetup = false;
	private int[][] visited;
	private double resolution = 1;
	private double width = 5,height = 5;
	
	private double v = 0;
	private double max = 0;
	private double penalty = 0;
	
	public DroneGeoFenceEvaluationFunction(Arguments args) {
		super(args);
		resolution = args.getArgumentAsDoubleOrSetDefault("resolution", resolution);
	}
	
	public void setup(Simulator simulator) {
		width = simulator.getEnvironment().getWidth();
		height = simulator.getEnvironment().getHeight();
		visited = new int[(int)(height/resolution)][(int)(width/resolution)];
		for(int y = 0 ; y < visited.length ; y++) {
			for(int x = 0 ; x < visited[y].length ; x++) {
				double coordX = (x - visited[y].length/2)*resolution;
				double coordY = (y - visited.length/2)*resolution;
				if(!insideLines(new Vector2d(coordX,coordY), simulator)) {
					visited[y][x] = -1;
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
				if(l.intersectsWithLineSegment(v, new Vector2d(0,-100)) != null)
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
		}
		
		ArrayList<Robot> robots = simulator.getRobots();
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			AquaticDrone r = (AquaticDrone)robots.get(i);
			
			if(!insideLines(new Vector2d(r.getPosition().getX(),r.getPosition().getY()), simulator)) {
				penalty+=1.0/simulator.getEnvironment().getSteps();
			}
			
			double x = r.getPosition().getX();
			double y = r.getPosition().getY();
			x/=resolution;
			y/=resolution;
			int indexY = (int)(y + visited.length/2);
			if(indexY < visited.length && indexY >= 0) {
				int indexX = (int)(x + visited.length/2);
				if(indexX < visited[indexY].length && indexX >= 0 && visited[indexY][indexX] == 0) {
					visited[indexY][indexX] = 1;
					v++;
				}
			}
		}
		fitness = v/max - penalty;
//		printGrid();
	}
	
	private void printGrid() {
		System.out.println();
		System.out.println();
		for(int y = visited.length-1 ; y >= 0 ; y--) {
			for(int x = 0 ; x < visited[y].length ; x++) {
				if(visited[y][x] == -1) {
					System.out.print(" ");
				}
				if(visited[y][x] == 0) {
					System.out.print("_");
				}
				if(visited[y][x] == 1) {
					System.out.print("#");
				}
			}
			System.out.println();
		}
	}
}