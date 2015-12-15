package simulation.robot.actuator;

import java.util.Random;

import mathutils.MathUtils;
import mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class PropellersActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	protected double leftSpeed = 0;
	protected double rightSpeed = 0;
	protected Random random;
	protected double backwardSpeed = 0.5;
	
	private double frictionConstant = 0.21;//REX 0.21//OLD 0.05
	private double accelarationConstant = 0.20;//REX 0.2//0.1
	private Vector2d velocity = new Vector2d();
	
	public PropellersActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		this.random = simulator.getRandom();
	}

	public void setLeftPercentage(double percentage) {
		leftSpeed = calculateSpeed(percentage);
	}

	public void setRightPercentage(double percentage) {
		rightSpeed = calculateSpeed(percentage);
	}
	
	private double calculateSpeed(double percentage) {
		
		if(percentage == 0) {
			return 0;
		} else {
			double speed = getSpeedInMs(Math.abs(percentage));
			return percentage >= 0 ? speed : -speed*backwardSpeed;
		}
	}

	@Override
	public void apply(Robot robot, double timeDelta) {
		
		double ls = leftSpeed * (1 + random.nextGaussian() * NOISESTDEV);
		double rs = rightSpeed * (1 + random.nextGaussian() * NOISESTDEV);
		
		((AquaticDrone) robot).setWheelSpeed(ls, rs);
	}

	@Override
	public String toString() {
		return "PropellersActuator [leftSpeed=" + leftSpeed + ", rightSpeed="
				+ rightSpeed + "]";
	}
	
	/**
	 * Returns the speed of the drone in kilometers/h.
	 * @param percentage value between 0.0 and 1.0
	 * @return the speed in KM/h
	 */
	private double getSpeedInKMh(double percentage) {
		percentage*=100;
		return (-0.0001*Math.pow(percentage,2) + 0.0382*percentage + 2.5101) / 2;
	}
	
	/**
	 * Returns the speed of the drone in meters/second.
	 * @param percentage value between 0.0 and 1.0
	 * @return the speed in m/s
	 */
	public double getSpeedInMs(double percentage) {
		return getSpeedInKMh(percentage)*1000.0/3600.0;
	}
	
	public double[] getSpeed(){
		double[] velocities = {leftSpeed, rightSpeed};
		return velocities;
	}
	
	private double motorModel(double d) {
		return 0.0048*Math.exp(2.4912*Math.abs(d*2)) - 0.0048;
	}
	
	public void move(AquaticDrone drone, double leftWheelSpeed, double rightWheelSpeed, double timeDelta) {
		double lw = Math.signum(rightWheelSpeed - leftWheelSpeed);
		
		drone.setOrientation(MathUtils.modPI2(drone.getOrientation() + motorModel(rightWheelSpeed-leftWheelSpeed)*lw));
		
		double accelDirection = (rightWheelSpeed+leftWheelSpeed) < 0 ? -1 : 1;
		
		double lengthOfAcc = accelarationConstant * (leftWheelSpeed + rightWheelSpeed);
		
		//Backwards motion should be slower. This value here is just an
		//estimate, and should be improved by taking real world samples
		if(accelDirection < 0)
			lengthOfAcc*=0.2;
		
		Vector2d accelaration = new Vector2d(lengthOfAcc * Math.cos(drone.getOrientation()), lengthOfAcc * Math.sin(drone.getOrientation()));
		
		velocity.setX(velocity.getX() * (1 - frictionConstant));
		velocity.setY(velocity.getY() * (1 - frictionConstant));    
		
		velocity.add(accelaration);
		
		drone.getPosition().set(
				drone.getPosition().getX() + timeDelta * velocity.getX(), 
				drone.getPosition().getY() + timeDelta * velocity.getY());
	}
}