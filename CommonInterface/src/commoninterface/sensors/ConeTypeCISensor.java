package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.mathutils.GeometricCalculator;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public abstract class ConeTypeCISensor extends CISensor {

	protected double[] readings;
	protected double[] angles;
	protected double range = 1;
	protected int numberSensors = 1;
	protected double openingAngle = Math.toRadians(90);
	protected double sensorShift = 0;
	protected GeometricCalculator geoCalc = new GeometricCalculator();

	protected AquaticDroneCI drone;
	
	public ConeTypeCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
		sensorShift = Math.toRadians(args.getArgumentAsDoubleOrSetDefault("sensorshift", sensorShift));
		numberSensors = args.getArgumentAsIntOrSetDefault("numbersensors",
				numberSensors);
		openingAngle = Math.toRadians(args.getArgumentAsIntOrSetDefault(
				"angle", 90));

		this.readings = new double[numberSensors];
		this.angles = new double[numberSensors];
		drone = (AquaticDroneCI)robot;
		
		setupPositions(numberSensors);
	}

	public abstract boolean validEntity(Entity e);

	@Override
	public void update(double time, Object[] entities) {
		
		for(int j = 0; j < readings.length; j++){
			readings[j] = 0.0;
		}
		
		for (Object e : entities) {
			
			if(e instanceof GeoEntity) {
			
				GeoEntity ge = (GeoEntity)e;
				
				if (validEntity(ge)) {
					for(int j=0; j<numberSensors; j++) {			
						readings[j] = Math.max(calculateContributionToSensor(j, ge), readings[j]);
					}
				}
			}
		}
	}
	
	protected double calculateContributionToSensor(int sensorNumber, GeoEntity ge) {
		
		LatLon droneLatLon = drone.getGPSLatLon();
		LatLon e = ge.getLatLon();
		
		double distance = CoordinateUtilities.distanceInMeters(droneLatLon, e);
		
		if(distance < getRange()) { 

			double absoluteAngle = Math.toRadians(CoordinateUtilities.angleInDegrees(drone.getGPSLatLon(), e) ); 
			double sensorAngle = angles[sensorNumber] + Math.toRadians(drone.getCompassOrientationInDegrees());
			
			double relativeAngle = sensorAngle - absoluteAngle;
			
			while(relativeAngle > Math.PI)
				relativeAngle-=2*Math.PI;
			while(relativeAngle < -Math.PI)
				relativeAngle+=2*Math.PI;
			
			if((relativeAngle < (openingAngle / 2.0)) && (relativeAngle > (-openingAngle / 2.0))) {
				sensedEntity(ge);
				return (getRange() - distance) / getRange();
			}
		}
		
		return 0;
	}
	
	public void setupPositions(int numberSensors) {
		double delta = 2 * Math.PI / numberSensors;
		double angle = sensorShift;
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
	
	protected void sensedEntity(Entity e){}
	
}
