package helpers;

public class TestMotors {
	
	public static void main(String[] args) {
		TestMotors tm = new TestMotors();
		
		for(double i = -1 ; i < 1.0; i+=0.05) {
			tm.setRudder(i, 0.5);
		}
//		tm.setRudder(1.0, 1);
//		tm.setRudder(0.9, 1);
//		tm.setRudder(0.9, 0.9);
//		tm.setRudder(0.89, 1);
//		tm.setRudder(0.89, 0.9);
	}
	
	public void setRudder(double heading, double speed) {
		
		double angleInDegrees = getTurningAngleFromHeading(heading)*-1;
		double motorDifference = 0;
		double forwardComponent = 0;
		double turningComponent = 0;
//		double turningSpeed = 0;
		
		double lw = 0;
		double rw = 0;
		
		if(Math.abs(heading) >= 0.9 || Math.abs(heading) == 0) {
			
			if(Math.abs(heading) >= 0.9)
				heading = 1.0*Math.signum(heading);
			
			if(Math.abs(heading) <= 0.1)
				heading = 0;
			
			angleInDegrees = getTurningAngleFromHeading(heading)*-1;
			
			motorDifference = getMotorDifferenceFromTurningAngle(Math.abs(angleInDegrees));
			
			forwardComponent = 1.0 - motorDifference;
			turningComponent = 1.0 - forwardComponent;
			
			turningComponent*=speed;
			forwardComponent*=speed;
			
			if(heading > 0) {
				lw = turningComponent;
			} else if(heading < 0) {
				rw = turningComponent;
			} else {
				lw = forwardComponent;
				rw = forwardComponent;
			}
			
//			angleInDegrees = getTurningAngleFromTurningSpeed(turningSpeed)*Math.signum(angleInDegrees);
//			System.out.println(angleInDegrees);
			
		} else {
			
			motorDifference = getMotorDifferenceFromAngleOneFullMotor(Math.abs(angleInDegrees));
			
			if(heading > 0) {
				lw = 1;
				rw = 1 - motorDifference;
			} else if(heading < 0) {
				lw = 1 - motorDifference;
				rw = 1;
			}
			
			lw*=speed;
			rw*=speed;
		}
		
		if(speed < 0.01) {
			lw = 0;
			rw = 0;
		}
		
		System.out.println(heading+"\t"+speed+"\t"+motorDifference*speed);
	}
	
	private double getMotorDifferenceFromAngleOneFullMotor(double angle) {
		return -0.0068 * Math.pow(angle,2) + 0.1614*angle+ 0.0903;
	}
	
	public double getTurningAngleFromDifferenceOneMotorFull(double speedDifference) {
		return 10.564 * speedDifference - 2.0412;
	}
	
	public double getTurningSpeedFromMotorDifferenceOneMotorFull(double difference) {
		return (-28.958*difference + 139.88) / 100.0;
	}
	
	private double getMotorDifferenceFromTurningAngle(double angle) {
		return Math.min(0.0098*Math.pow(angle,2) + 0.0244*angle,1);
	}
	
	private double getTurningAngleFromTurningSpeed(double turningSpeed) {
		return 7.6401 * turningSpeed;
	}
	
	private double getTurningAngleFromHeading(double heading) {
		return 9*heading;
	}
	
}
