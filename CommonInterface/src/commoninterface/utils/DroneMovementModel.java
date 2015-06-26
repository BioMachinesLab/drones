package commoninterface.utils;

import commoninterface.entities.RobotLocation;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.jcoord.LatLon;

public class DroneMovementModel {
	
	/**
	 * THIS IS NOT CURRENTLY BEING USED
	 */
	
	private static double timeDelta = 0.1;
	protected static double backwardSpeed = 0.5;
	
	private Vector2d pos;
	private Vector2d velocity = new Vector2d();
	private Vector2d kalmanPosition = new Vector2d();
	
	private double orientation;
	private double kalmanOrientation;
	private double frictionConstant = 0.21;//0.05
	private double accelarationConstant = 0.20;//0.1
	
	private RobotKalman kalman = new RobotKalman();
	
	public DroneMovementModel(Vector2d pos, double orientation) {
		this.pos = new Vector2d(pos);
		this.orientation = Math.toRadians(orientation);
	}
	
	public void correctPosition(Vector2d pos) {
		this.pos = new Vector2d(pos);
		this.orientation = Math.toRadians(orientation);
	}
	
	public void correctOrientation(double orientation) {
		this.orientation = Math.toRadians(orientation);
	}
	
	public Vector2d cartesianPosition() {
		return new Vector2d(pos);
	}
	
	public LatLon getSimulatedGPSPosition() {
		return CoordinateUtilities.cartesianToGPS(pos);
	}
	
	public double getSimulatedOrientation() {
		return Math.toDegrees(orientation);
	}
	
	public Vector2d getKalmanPosition() {
		return kalmanPosition;
	}
	
	public double getKalmanOrientation() {
		return Math.toDegrees(kalmanOrientation);
	}
	
	public void move(double percentageL, double percentageR) {
		
		double leftWheelSpeed = calculateSpeed(percentageL);
		double rightWheelSpeed = calculateSpeed(percentageR);
		
//		leftWheelSpeed = 0.1;
//		rightWheelSpeed = 0.1;
		
		double lw = Math.signum(rightWheelSpeed-leftWheelSpeed);
		
		orientation = MathUtils.modPI2(orientation + motorModel(rightWheelSpeed-leftWheelSpeed)*lw);
		
		double accelDirection = (rightWheelSpeed+leftWheelSpeed) < 0 ? -1 : 1;
		double lengthOfAcc = accelarationConstant * (leftWheelSpeed + rightWheelSpeed);
		//Backwards motion should be slower. This value here is just an
		//estimate, and should be improved by taking real world samples
		if(accelDirection < 0)
			lengthOfAcc*=0.2;
		
		Vector2d accelaration = new Vector2d(lengthOfAcc * Math.cos(orientation), lengthOfAcc * Math.sin(orientation));
		
		velocity.setX(velocity.getX() * (1 - frictionConstant));
		velocity.setY(velocity.getY() * (1 - frictionConstant));    
		
		velocity.add(accelaration);
		
		pos.set(
				pos.getX() + timeDelta * velocity.getX(), 
				pos.getY() + timeDelta * velocity.getY()
				);
		
		
		RobotLocation estimatedLoc = kalman.getEstimation(getSimulatedGPSPosition(), getSimulatedOrientation());
//		pos = new Vector2d(CoordinateUtilities.GPSToCartesian(estimatedLoc.getLatLon()));
		kalmanPosition = new Vector2d(CoordinateUtilities.GPSToCartesian(estimatedLoc.getLatLon()));
		pos = new Vector2d(kalmanPosition);
//		orientation = Math.toRadians(estimatedLoc.getOrientation());
		kalmanOrientation = Math.toRadians(estimatedLoc.getOrientation());;
	}
	
	private double motorModel(double d) {
		return d*0.5;
//		return 0.0048*Math.exp(2.4912*Math.abs(d*2)) - 0.0048;
	}
	
	private double calculateSpeed(double percentage) {
		
		if(percentage == 0) {
			return 0;
		} else {
			double speed = getSpeedInMs(Math.abs(percentage));
			return percentage >= 0 ? speed : -speed*backwardSpeed;
		}
	}
	
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

}
