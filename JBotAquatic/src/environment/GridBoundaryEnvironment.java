package environment;

import java.util.ArrayList;

import environment.utils.EnvironmentGrid;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GridBoundaryEnvironment extends BoundaryEnvironment {
	private static final long serialVersionUID = -7378304806835524935L;
	protected ArrayList<EnvironmentGrid> grids = new ArrayList<EnvironmentGrid>();
	protected double gridResolution = 1.0;
	protected double gridDistance = 5;
	protected double gridCommRange = 40;
	protected int gridUpdate = 50;
	protected double gridDecay = 0;

	public GridBoundaryEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.gridResolution = args.getArgumentAsDoubleOrSetDefault("gridresolution", gridResolution);
		this.gridDistance = args.getArgumentAsDoubleOrSetDefault("griddistance", gridDistance);
		this.gridUpdate = args.getArgumentAsIntOrSetDefault("gridupdate", gridUpdate);
		this.gridCommRange = args.getArgumentAsDoubleOrSetDefault("gridcommrange", gridCommRange);
		this.gridDecay = args.getArgumentAsDoubleOrSetDefault("griddecay", gridDecay);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		for(Robot r : robots) {
			EnvironmentGrid grid = new EnvironmentGrid(r, width, height, gridResolution, gridDistance, gridDecay);
			simulator.addCallback(grid);
			grids.add(grid);
		}
		
	}
	
	@Override
	public void update(double time) {
		super.update(time);
		
		if(gridUpdate != 0 && time % gridUpdate == 0) {
			for(int i = 0 ; i < grids.size() ; i++) {
				for(int j = i+1 ; j < grids.size() ; j++) {
					if(grids.get(i).getRobot().getPosition().distanceTo(grids.get(j).getRobot().getPosition()) < gridCommRange) {
						grids.get(i).mergeGrids(grids.get(j));
					}
				}
			}
		}
	}
	
	public EnvironmentGrid getGrid(Robot r) {
		return grids.get(r.getId());
	}
	
	public ArrayList<EnvironmentGrid> getGrids() {
		return grids;
	}
	
	public double getGridResolution() {
		return gridResolution;
	}
	
	public double getGridDistance() {
		return gridDistance;
	}

}
