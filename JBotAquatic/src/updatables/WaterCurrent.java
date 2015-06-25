package updatables;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class WaterCurrent implements Updatable{
	
	private double maxSpeed = 0;
	private double speed = 0;
	private double angle = 0;
	private Vector2d increment;
	private boolean configured = false;
	
	public WaterCurrent(Arguments args) {
		maxSpeed = args.getArgumentAsDoubleOrSetDefault("maxspeed", maxSpeed);
	}
	
	@Override
	public void update(Simulator simulator) {
		
		if(!configured) {
			speed = simulator.getRandom().nextDouble()*maxSpeed;
			angle = simulator.getRandom().nextDouble()*Math.PI*2;
			increment = new Vector2d(speed*Math.cos(angle),speed*Math.sin(angle));
			configured = true;
		}
		
		for(Robot r : simulator.getRobots()) {
			r.setPosition(r.getPosition().getX()+increment.getX(),r.getPosition().getY()+increment.getY());
		}
	}

}
