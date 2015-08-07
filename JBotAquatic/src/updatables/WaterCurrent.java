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
	private boolean fixedSpeed = false;
	
	public WaterCurrent(Arguments args) {
		maxSpeed = args.getArgumentAsDoubleOrSetDefault("maxspeed", maxSpeed);
		fixedSpeed = args.getFlagIsTrue("fixedspeed");
		angle = Math.toRadians(args.getArgumentAsDouble("angle"));
	}
	
	@Override
	public void update(Simulator simulator) {
		
		if(!configured) {
			setup(simulator);
			configured = true;
		}
		
		for(Robot r : simulator.getRobots()) {
			r.setPosition(r.getPosition().getX()+increment.getX(),r.getPosition().getY()+increment.getY());
		}
	}
	
	private void setup(Simulator simulator) {
		if(fixedSpeed) {
			speed = maxSpeed;
		} else {
			speed = simulator.getRandom().nextDouble()*maxSpeed;
		}
		if(angle == 0)
			angle = simulator.getRandom().nextDouble()*Math.PI*2;
		
		increment = new Vector2d(speed*Math.cos(angle)*simulator.getTimeDelta(),speed*Math.sin(angle)*simulator.getTimeDelta());
	}
	
	public void disable() {
		increment = new Vector2d();
		configured = true;
	}

}
