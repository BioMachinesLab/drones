package environment.utils;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;

public class EnvironmentGrid implements Updatable{
	
	protected double[][] grid;
	protected double resolution;
	protected double distance;
	protected double decay;
	protected Robot r;
	
	public EnvironmentGrid(Robot r, double width, double height, double resolution, double distance, double decay) {
		grid = new double[(int)(height/resolution)][(int)(width/resolution)];
		this.resolution = resolution;
		this.distance = distance;
		this.decay = decay;
		this.r = r;
	}
	
	public double[][] getGrid() {
		return grid;
	}
	
	public Robot getRobot() {
		return r;
	}
	
	public double getResolution() {
		return resolution;
	}
	
	@Override
	public void update(Simulator simulator) {
		double rX = r.getPosition().x;
		double rY = r.getPosition().y;
		
		if(distance < resolution) {
			
			int x = (int)((rX / resolution) + grid[0].length / 2);
		    int y = (int)((rY / resolution) + grid.length / 2);
			
		    if(y >= 0 && y < grid.length && x >= 0 && x < grid[y].length)
		    	grid[y][x] = simulator.getTime();
			
		} else {
		
		
	        double minX = rX - distance;
	        double minY = rY - distance;
	
	        double maxX = rX + distance;
	        double maxY = rY + distance;
	
	        int pMinX = (int) ((minX / resolution) + grid[0].length / 2);
	        int pMinY = (int) ((minY / resolution) + grid.length / 2);
	
	        double pMaxX = (maxX / resolution) + grid[0].length / 2;
	        double pMaxY = (maxY / resolution) + grid.length / 2;
	
	        for (int y = pMinY; y < pMaxY; y++) {
	
	            if (y >= grid.length || y < 0) {
	                continue;
	            }
	
	            for (int x = pMinX; x < pMaxX; x++) {
	
	                if (x >= grid[y].length || x < 0) {
	                    continue;
	                }
	
	                grid[y][x] = simulator.getTime();
	            }
	        }
		}
	}
	
	public void mergeGrids(EnvironmentGrid other) {
		for(int y = 0 ; y < grid.length ; y++) {
			for(int x = 0 ; x < grid[y].length ; x++) {
				if(other.grid[y][x] > grid[y][x]) {
					grid[y][x] = other.grid[y][x];
				} else if(grid[y][x] > other.grid[y][x]) {
					other.grid[y][x] = grid[y][x];
				}
			}
		}
	}
	
	public double getGridValue(double rX, double rY) {
        int gY = (int) ((rY / resolution) + grid.length / 2);
        int gX = (int) ((rX / resolution) + grid[0].length / 2);
        
        if(gX >= 0 && gX < grid[0].length && gY >= 0 && gY < grid.length) {
        	return grid[gY][gX];
        }
        
        return -1;
	}
	
	public Vector2d getCartesianPosition(int gx, int gy) {
		double x = (gx - grid[0].length/2)*resolution;
		double y = (gy - grid.length/2)*resolution;
		return new Vector2d(x,y);
	}
	
	public double getDecay() {
		return decay;
	}
	
}