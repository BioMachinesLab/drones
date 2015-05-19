package commoninterface.sensors;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import commoninterface.entities.VirtualEntity;
import commoninterface.mathutils.GeometricCalculator;
import commoninterface.mathutils.GeometricInfo;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;

public abstract class ThymioConeTypeCISensor extends CISensor {

	protected double[] readings;
	protected double[] angles;
	protected double range = 1;
	protected int numberSensors = 1;
	protected double openingAngle = Math.toRadians(90);
	protected GeometricCalculator geoCalc = new GeometricCalculator();
	protected Vector2d sensorPosition = new Vector2d();

	private ThymioCI thymio;
	
	public ThymioConeTypeCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
		numberSensors = args.getArgumentAsIntOrSetDefault("numbersensors", numberSensors);
		openingAngle = Math.toRadians(args.getArgumentAsIntOrSetDefault("angle", 90));

		this.readings = new double[numberSensors];
		this.angles = new double[numberSensors];
		thymio = (ThymioCI)robot;
		
		setupPositions(numberSensors);
	}

	public abstract boolean validEntity(Object e);

	@Override
	public void update(double time, Object[] entities) {
		
		for(int j = 0; j < readings.length; j++){
			readings[j] = 0.0;
		}
		
		for (Object e : entities) {
			if (validEntity(e)) {
				for(int j=0; j<numberSensors; j++) {
					if(openingAngle > 0.018){ //1degree
						readings[j] = Math.max(calculateContributionToSensor(j, (VirtualEntity)e), readings[j]);
					}
				}
			}
		}
		
	}
	
	protected double calculateContributionToSensor(int sensorNumber, VirtualEntity e) {
		if(thymio.getVirtualPosition() != null && thymio.getVirtualOrientation() != null){
			GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, e.getPosition());
			
			if((sensorInfo.getDistance() < getRange()) && 
			   (sensorInfo.getAngle() < (openingAngle / 2.0)) && 
			   (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
				sensedEntity(e);
				return (getRange() - sensorInfo.getDistance()) / getRange();
			}
		}
		return 0;
	}
	
	private GeometricInfo getSensorGeometricInfo(int sensorNumber, Vector2d toPoint){
		double orientation=angles[sensorNumber]+thymio.getVirtualOrientation();
		sensorPosition.set(Math.cos(orientation) * thymio.getThymioRadius() + thymio.getVirtualPosition().getX(),
				Math.sin(orientation) * thymio.getThymioRadius() + thymio.getVirtualPosition().getY());
		
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(sensorPosition, orientation, toPoint);
		return sensorInfo;
	}
	
	public void setupPositions(int numberSensors) {
		double delta = 2 * Math.PI / numberSensors;
		double angle = 0;
		for (int i = 0; i < numberSensors; i++) {
			angles[i] = angle;
			angle += delta;
		}
	}

	public double[] getAngles() {
		return angles;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}

	public double getRange() {
		return range;
	}
	
	public double getOpeningAngle() {
		return openingAngle;
	}
	
	@Override
	public int getNumberOfSensors() {
		return readings.length;
	}
	
	protected void sensedEntity(VirtualEntity ve){}
	
}
