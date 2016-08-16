package simulation.robot.actuator;

import java.util.Random;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class RudderActuator extends Actuator {
	private static final long serialVersionUID = 382936688810273745L;

	//noise in the speeds
	public float NOISESTDEV = 0.05f;

	protected double heading = 0;
	protected double speed = 0;
	protected Random random;
	private double prevSpeed = 0;
	
	private int indexDelay = 0;
	private double[] delay;

	private double angle;
	
	private double currentOrientation = 0;
	private double inertia = 0;
	
	public RudderActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		this.random = simulator.getRandom();
		
		//We observed a delay of roughly 500ms between actuation and turning of the robot, which 
		//we tried to model in simulation by putting the heading values in a qeue
		int delay = arguments.getArgumentAsIntOrSetDefault("delay", 5);
		
		if(delay == 0) {
			 this.delay = new double[1];
		} else {
			this.delay = new double[delay];
		}
	}
	
	public void setHeading(double currentHeading) {
		delay[indexDelay] = currentHeading;
		indexDelay++;
		indexDelay%=delay.length;
		this.heading = delay[indexDelay];
		
	}

	public void setSpeed(double percentage) {
		this.speed = percentage;
	}

	@Override
	public void apply(Robot drone, double timeDelta) {
		
		//the robot's measured max turning speed is 9 degrees per second   
		double angleInDegrees = Math.max(-9, Math.min(9, angle + (getTurningAngleFromHeading(heading) * -1)));
		
		double motorDifference = 0;
		double forwardComponent = 0;
		double turningComponent = 0;
		double turningSpeed = 0;
		double forwardSpeed = 0;

		double maxIncrementUp = 1.0 / (1.0 * 10.0); // 1 second to accel to full speed
		double maxIncrementDown = 1.0 / (5.0 * 10.0); // 5 seconds to stop

		double desiredSpeed = speed;

		//limit how much the robot can accelerate at any given moment
		if (speed > prevSpeed + maxIncrementUp)
			speed = prevSpeed + maxIncrementUp;
		else if (speed < prevSpeed - maxIncrementDown)
			speed = prevSpeed - maxIncrementDown;
		
		//Heading varies between [-1,1]. If the value is very close to the extremes (<-0.9 or > 0.9),
		//we simplify by turning off one of the motors completely. if the value is very close to the center (~0),
		//we simplify by saying that the robot wants to go in a straight line.
		if (Math.abs(heading) >= 0.9) {
			
			//the robot is either turning abruptly (one motor stopped), or moving forward.

			if (Math.abs(heading) >= 0.9)
				heading = 1.0 * Math.signum(heading);

			if (Math.abs(heading) <= 0.1)
				heading = 0;

			angleInDegrees = Math.max(-9, Math.min(9, angle + (getTurningAngleFromHeading(heading) * -1)));
			
			//The motor difference reflects how much one of the motors has more power than the other.
			//If the motor difference is 1.0, then one of the motors is stopped and the other is is running at full speed.
			motorDifference = getMotorDifferenceFromTurningAngle(Math.abs(angleInDegrees));

			//The new speed and angle is calculated using these 2 components: forward and turning.
			//It is trivial to model the robot's dynamics when one of the motor is stopped (maximum turning speed),
			//or when both motors are running at the same speed (0 turning speed). We therefore combined both in a single model,
			//where there is a forward component and a turning component.
			//
			//When going forward, the motor difference is 0 (both running at the same speed).
			//When turning at the maximum turning speed, the motor difference
			forwardComponent = 1.0 - motorDifference;
			turningComponent = 1.0 - forwardComponent;

			turningComponent *= speed;
			forwardComponent *= speed;

			turningSpeed = getTurningSpeedFromDifferenceOneStoppedMotor(turningComponent);
			forwardSpeed = getForwardSpeedInMs(forwardComponent);
			angleInDegrees = Math.max(-9, Math.min(9, angle + getTurningAngleFromTurningSpeed(turningSpeed) * Math.signum(angleInDegrees)));

		} else {
			//the robot is turning moderately, which means that both motors are turning and different speeds
			
			motorDifference = getMotorDifferenceFromAngleOneFullMotor(Math.abs(angleInDegrees));
			turningSpeed = getTurningSpeedFromMotorDifferenceOneMotorFull(motorDifference) * translateSpeedReduction(speed);
		}

		double x = drone.getPosition().getX();
		double y = drone.getPosition().getY();
		double o = drone.getOrientation();

		if (speed < 0.01) {
			//allow the robot to come to a halt if the speed is very low
			turningSpeed = 0;
			forwardSpeed = 0;
		}

		turningSpeed = turningSpeed * (1 + random.nextGaussian() * NOISESTDEV);
		forwardSpeed = forwardSpeed * (1 + random.nextGaussian() * NOISESTDEV);

		x = x + (turningSpeed + forwardSpeed) * timeDelta * Math.cos(o);
		y = y + (turningSpeed + forwardSpeed) * timeDelta * Math.sin(o);

		if (desiredSpeed >= 0.01) {
			double newOrientation = currentOrientation + Math.toRadians(angleInDegrees);
			
			newOrientation = currentOrientation + Math.toRadians(angleInDegrees*1.5);

			double orientationDifference = newOrientation - currentOrientation;
			
			if(currentOrientation == 0)
				currentOrientation = drone.getOrientation();
			
			double change = orientationDifference*1.0;
			double maxChange = Math.toRadians(7);
			
			if(change > maxChange)
				change = maxChange;
			if(change < -maxChange)
				change = -maxChange;
			
			double inertiaDiff = heading-inertia;
			inertia+= inertiaDiff*0.35; 
			
			inertiaDiff = (heading-inertia);
			
			currentOrientation+=change*(1-Math.abs(inertiaDiff));
			
			currentOrientation = MathUtils.modPI2(currentOrientation);
			drone.setOrientation(currentOrientation);
			
		}

		drone.setPosition(new Vector2d(x, y));

		prevSpeed = speed;
		angle = angleInDegrees;
	}

	@Override
	public String toString() {
		return "RudderActuator [heading=" + heading + ", speed=" + speed + "]";
	}

	private double translateSpeedReduction(double speed) {
		return 0.7 * speed + 0.3;
	}

	public double getForwardSpeedInMs(double percentage) {
		return 153.99 * Math.pow(percentage, 0.3663) / 100.0;
	}

	private double getTurningSpeedFromDifferenceOneStoppedMotor(double speedDifference) {

		if (speedDifference == 0)
			return 0;

		return (-35.322 * Math.pow(speedDifference, 2) + 133.59 * speedDifference + 18.964) / 100.0;
	}

	private double getMotorDifferenceFromAngleOneFullMotor(double angle) {
		return -0.0068 * Math.pow(angle, 2) + 0.1614 * angle + 0.0903;
	}

	public double getTurningAngleFromDifferenceOneMotorFull(double speedDifference) {
		return 10.564 * speedDifference - 2.0412;
	}

	public double getTurningSpeedFromMotorDifferenceOneMotorFull(double difference) {
		return (-28.958 * difference + 139.88) / 100.0;
	}

	private double getMotorDifferenceFromTurningAngle(double angle) {
		return Math.min(0.0098 * Math.pow(angle, 2) + 0.0244 * angle, 1);
	}

	private double getTurningAngleFromTurningSpeed(double turningSpeed) {
		return 7.6401 * turningSpeed;
	}

	/**
	 * This function maps the desired heading to a rotational speed, in degrees per second
	 * 
	 * @param heading in percentage
	 * @return rotational speed in degrees per second
	 * 
	 */
	private double getTurningAngleFromHeading(double heading) {
		return 9 * heading;
	}

	public double getSpeed() {
		return speed;
	}

	public double getHeading() {
		return heading;
	}

}