package evaluation;

import java.util.ArrayList;

import environment.GridBoundaryEnvironment;
import environment.utils.EnvironmentGrid;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

public class GridCoverageFitness extends EvaluationFunction {
	private static final long serialVersionUID = 7814847897935336605L;

	private boolean isSetup = false;

    private ArrayList<EnvironmentGrid> grids;
    protected double max = 0;
    protected Simulator sim;

    public GridCoverageFitness(Arguments args) {
        super(args);
    }

    public void setup(Simulator simulator) {
        
    	this.sim = simulator;
    	
        GridBoundaryEnvironment env = (GridBoundaryEnvironment)simulator.getEnvironment();
        
        grids = env.getGrids();
        
        double[][] firstGrid = env.getGrids().get(0).getGrid(); 
        max = firstGrid.length*firstGrid[0].length;
    }

    @Override
    public void update(Simulator simulator) {
        if (!isSetup) {
            setup(simulator);
            isSetup = true;
        }
        
//        if(simulator.getTime() % 10000 == 0)
//        System.out.println(simulator.getTime()+" "+calculateGrid());
//      printGrid();
    }
    
    protected double calculateGrid() {
    	double sum = 0;
    	
    	if(grids == null)
    		return 0;
        
        EnvironmentGrid firstGrid = grids.get(0);
        
        double[][] result = new double[firstGrid.getGrid().length][firstGrid.getGrid()[0].length];
        
        for(EnvironmentGrid grid : grids) {
        	
        	double[][] g = grid.getGrid();

	        for (int y = 0; y < g.length; y++) {
	            for (int x = 0; x < g[y].length; x++) {
	            	result[y][x] = Math.max(result[y][x], g[y][x]);
	            }
	        }
        }
        
        for (int y = 0; y < result.length; y++) {
            for (int x = 0; x < result[y].length; x++) {
		        if(firstGrid.getDecay() == 0) {
		    		sum+= result[y][x] > 0 ? 1 : 0;
		    	} else {
		    		
		    		if(result[y][x] != 0) {
		    			sum+= sim.getTime() - firstGrid.getDecay() > result[y][x] ? 0 : 1;
		    		}
		    	}
            }
        }
        return sum/max;
    }

    @Override
    public double getFitness() {
    	
    	this.fitness = calculateGrid();
    	
        return 10 + fitness;
    }
    
    @SuppressWarnings("unused")
	private void printGrid() {
 		System.out.println();
 		System.out.println();
 		
 		double[][] firstGrid = grids.get(0).getGrid();
 		
 		for(int y = firstGrid.length-1 ; y >= 0 ; y--) {
 			for(int x = 0 ; x < firstGrid[y].length ; x++) {
 				if(firstGrid[y][x] == -1) {
 					System.out.print(" ");
 				} else if(firstGrid[y][x] == 0) {
 					System.out.print("_");
 				} else if(grids.get(0).getDecay() == 0 && firstGrid[y][x] > 0) {
 					System.out.print("#");
 				} else if(grids.get(0).getDecay() > 0 && sim.getTime() - grids.get(0).getDecay() <= firstGrid[y][x]) {
 					System.out.print("#");
 				} else {
 					System.out.print("<");
 				}
 			}
 			System.out.println();
 		}
 	}
}
