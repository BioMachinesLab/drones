package commoninterface.sensors;

import java.util.ArrayList;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.ThymioCI;
import commoninterface.mathutils.GeometricCalculator;
import commoninterface.objects.Entity;
import commoninterface.objects.VirtualEntity;
import commoninterface.utils.CIArguments;

public abstract class ThymioConeTypeCISensor extends CISensor {

	protected double[] readings;
	protected double[] angles;
	protected double range = 1;
	protected int numberSensors = 1;
	protected double openingAngle = Math.toRadians(90);
	protected GeometricCalculator geoCalc = new GeometricCalculator();

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

	public abstract boolean validEntity(Entity e);

	@Override
	public void update(double time, ArrayList<Entity> entities) {
		
		for(int j = 0; j < readings.length; j++){
			readings[j] = 0.0;
		}
		
		for (Entity e : entities) {
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
		if(thymio.getVirtualPosition() != null){
			double distance = thymio.getVirtualPosition().distanceTo(e.getPosition());
			double sensorAngle = angles[sensorNumber];
			
			if(distance < getRange() && sensorAngle < (openingAngle / 2.0) && (sensorAngle > (-openingAngle / 2.0))) 
				return (getRange() - distance) / getRange();
			
		}
		return 0;
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
	
	@Override
	public int getNumberOfSensors() {
		return readings.length;
	}
}
