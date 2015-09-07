package evaluation;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class EnemyEvaluationFunction extends AvoidCollisionsFunction{
	
	@ArgumentsAnnotation(name="targetdistance", defaultValue="10")
	private double targetDistance = 10;
	@ArgumentsAnnotation(name="minimumdistance", defaultValue="4")
	private double minimumDistance = 3;
	
	private Robot enemy = null;
	private double time = 0;
	private double numberDetecting = 0;
	private double numberOutside = 0;

	public EnemyEvaluationFunction(Arguments args) {
		super(args);
		targetDistance = args.getArgumentAsDoubleOrSetDefault("targetdistance", targetDistance);
		minimumDistance = args.getArgumentAsDoubleOrSetDefault("minimumdistance", minimumDistance);
		fitness = 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(enemy == null) {
			enemy = simulator.getRobots().get(simulator.getRobots().size()-1);
			fitness = 0;
		}
		
		double numberDetecting = 0;
		double numberOutside = 0;
		
		for(Robot r : simulator.getRobots()) {
			if(r.getId() != enemy.getId()) {
				
				if(!insideLines(r.getPosition(), simulator))
					numberOutside++;
				else {
					double dist = r.getPosition().distanceTo(enemy.getPosition());
					if(dist < targetDistance && dist > minimumDistance) {
						numberDetecting++;
					}
				}
			} else {
				if(insideLines(r.getPosition(), simulator))
					time++;
			}
		}
		
		if(numberOutside == 0) {
//			fitness+=numberDetecting;
//			fitness-=numberOutside*10;
			this.numberDetecting+= numberDetecting;
			this.numberOutside+= numberOutside;
		}
		
		super.update(simulator);
		
		fitness = this.numberDetecting / time;
		
	}
	
	@Override
	public double getFitness() {
		return Math.max(10+fitness,0);
	}
	
	public boolean insideLines(Vector2d v, Simulator sim) {
        //http://en.wikipedia.org/wiki/Point_in_polygon
        int count = 0;

        for (PhysicalObject p : sim.getEnvironment().getAllObjects()) {
            if (p.getType() == PhysicalObjectType.LINE) {
                Line l = (Line) p;
                if (l.intersectsWithLineSegment(v, new Vector2d(0, -Integer.MAX_VALUE)) != null) {
                    count++;
                }
            }
        }
        return count % 2 != 0;
    }
	
}
