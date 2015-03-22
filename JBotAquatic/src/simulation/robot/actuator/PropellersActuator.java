package simulation.robot.actuator;

import java.util.Random;

import simulation.Simulator;
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
	public void apply(Robot robot) {
		leftSpeed*= (1 + random.nextGaussian() * NOISESTDEV);
		rightSpeed*= (1 + random.nextGaussian() * NOISESTDEV);
		((DifferentialDriveRobot) robot).setWheelSpeed(leftSpeed, rightSpeed);
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
}