package helpers;

public class TestMotors2 {
	
	public static void main(String[] args) {
		TestMotors2 tm = new TestMotors2();
		for(double i = -1 ; i < 1.0; i+=0.05) {
			tm.setRudder(i, 0.5);
		}
	}
	
	public void setRudder(double heading, double speed) {
		
		double angle = 0;
		double a = 0;
		double NEW_ANGLE = 1;
		double ANGLE_DECAY = 0;
		double prevSpeed = speed;
		
		double angleInDegrees = Math.max(-9, Math.min(9, angle + (getTurningAngleFromHeading(heading) * -1) * NEW_ANGLE));
		
		
		double motorDifference = 0;
		double forwardComponent = 0;
		double turningComponent = 0;
		double turningSpeed = 0;

		double maxIncrementUp = 1.0 / (1.0 * 10.0); // 1 second to accel to full
													// speed
		double maxIncrementDown = 1.0 / (5.0 * 10.0); // 5 seconds to stop

		double desiredSpeed = speed;

		if (speed > prevSpeed + maxIncrementUp)
			speed = prevSpeed + maxIncrementUp;
		else if (speed < prevSpeed - maxIncrementDown)
			speed = prevSpeed - maxIncrementDown;
		
		if (Math.abs(heading) >= 0.9/* || Math.abs(heading) < 0.1 */) {

			if (Math.abs(heading) >= 0.9)
				heading = 1.0 * Math.signum(heading);

			if (Math.abs(heading) <= 0.1)
				heading = 0;

			angleInDegrees = Math.max(-9, Math.min(9, angle 
					+ (getTurningAngleFromHeading(heading) * -1) * NEW_ANGLE));
			motorDifference = getMotorDifferenceFromTurningAngle(Math
					.abs(angleInDegrees));

			forwardComponent = 1.0 - motorDifference;
			turningComponent = 1.0 - forwardComponent;

			turningComponent *= speed;
			forwardComponent *= speed;

			turningSpeed = getTurningSpeedFromDifferenceOneStoppedMotor(turningComponent);
			angleInDegrees = Math.max(-9, Math.min(9, angle
					+
					getTurningAngleFromTurningSpeed(turningSpeed)
					* Math.signum(angleInDegrees) *NEW_ANGLE));
			
		} else {
			motorDifference = getMotorDifferenceFromAngleOneFullMotor(Math
					.abs(angleInDegrees));
			turningSpeed = getTurningSpeedFromMotorDifferenceOneMotorFull(motorDifference)
					* translateSpeedReduction(speed);
			System.out.println(heading+"\t"+speed+"\t"+motorDifference* translateSpeedReduction(speed));
		}

		// turningSpeed*=.5;

//		double x = drone.getPosition().getX();
//		double y = drone.getPosition().getY();
//		double o = drone.getOrientation();

		if (speed < 0.01) {
			turningSpeed = 0;
		}

//		x = x + (turningSpeed + forwardSpeed) * timeDelta * Math.cos(o);
//		y = y + (turningSpeed + forwardSpeed) * timeDelta * Math.sin(o);

		if (desiredSpeed >= 0.01) {
			double newOrientation = a + Math.toRadians(angleInDegrees);
			
//			newOrientation = a + Math.toRadians(angleInDegrees*1.5);
//
//			double diff = newOrientation - a;
//			
//			// 1.0 7
//			
//			double change = diff*1.0;
//			double maxChange = Math.toRadians(7);
//			
//			if(change > maxChange)
//				change = maxChange;
//			if(change < -maxChange)
//				change = -maxChange;
//			
//			double inertiaDiff = heading-inertia;
//			inertia+= inertiaDiff*0.35; //0.35
//			
//			inertiaDiff = (heading-inertia);
//			
//			a+=change*(1-Math.abs(inertiaDiff));
//			
//			drone.setOrientation(a);
				
			
		}

//		drone.setPosition(new Vector2d(x, y));

		prevSpeed = speed;
		angle = angleInDegrees*ANGLE_DECAY;
	}

	private double translateSpeedReduction(double speed) {
		return 0.7 * speed + 0.3;
	}

	private double getForwardSpeedInMs(double percentage) {
		return 153.99 * Math.pow(percentage, 0.3663) / 100.0;
	}

	private double getTurningSpeedFromDifferenceOneStoppedMotor(
			double speedDifference) {

		if (speedDifference == 0)
			return 0;

		return (-35.322 * Math.pow(speedDifference, 2) + 133.59
				* speedDifference + 18.964) / 100.0;
	}

	private double getMotorDifferenceFromAngleOneFullMotor(double angle) {
		return -0.0068 * Math.pow(angle, 2) + 0.1614 * angle + 0.0903;
	}

	public double getTurningAngleFromDifferenceOneMotorFull(
			double speedDifference) {
		return 10.564 * speedDifference - 2.0412;
	}

	public double getTurningSpeedFromMotorDifferenceOneMotorFull(
			double difference) {
		return (-28.958 * difference + 139.88) / 100.0;
	}

	private double getMotorDifferenceFromTurningAngle(double angle) {
		return Math.min(0.0098 * Math.pow(angle, 2) + 0.0244 * angle, 1);
	}

	private double getTurningAngleFromTurningSpeed(double turningSpeed) {
		return 7.6401 * turningSpeed;
	}

	private double getTurningAngleFromHeading(double heading) {
		return 9 * heading;
	}


}
