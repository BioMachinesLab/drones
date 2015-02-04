package commoninterface.sensors;

import java.util.ArrayList;

import objects.Entity;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.mathutils.GeometricCalculator;
import commoninterface.mathutils.GeometricInfo;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public abstract class ConeTypeCISensor extends CISensor {

	protected double[] readings;
	protected double[] angles;
	protected double range = 1;
	protected int numberSensors = 1;
	protected double openingAngle = Math.toRadians(90);
	protected GeometricCalculator geoCalc = new GeometricCalculator();
	private Vector2d sensorPosition = new Vector2d();
	private Vector2d robotCartesian;

	public ConeTypeCISensor(int id, AquaticDroneCI drone, CIArguments args) {
		super(id, drone, args);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
		numberSensors = args.getArgumentAsIntOrSetDefault("numbersensors",
				numberSensors);
		openingAngle = Math.toRadians(args.getArgumentAsIntOrSetDefault(
				"angle", 90));

		this.readings = new double[numberSensors];
		this.angles = new double[numberSensors];

		setupPositions(numberSensors);
	}

	public abstract boolean validEntity(Entity e);

	@Override
	public void update(double time, ArrayList<Entity> entities) {
		
		LatLon robotLatLon = drone.getGPSLatLon();
		robotCartesian = CoordinateUtilities.GPSToCartesian(robotLatLon);
		
		for(int j = 0; j < readings.length; j++){
			readings[j] = 0.0;
		}
		
		for (Entity e : entities) {
			if (validEntity(e)) {
				Vector2d entityCartesian = CoordinateUtilities.GPSToCartesian(new LatLon(e.getLatitude(),e.getLongitude()));
				for(int j=0; j<numberSensors; j++) {			
					readings[j] = Math.max(calculateContributionToSensor(j, entityCartesian), readings[j]);
				}
			}
		}
	}
	
	protected double calculateContributionToSensor(int sensorNumber, Vector2d e) {
		
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, e);
		
		if((sensorInfo.getAngle() < (openingAngle / 2.0)) && 
				   (sensorInfo.getAngle() > (-openingAngle / 2.0))) {

			return (getRange() - sensorInfo.getDistance()) / getRange();
		}
		
		return 0;
	}
	
	protected GeometricInfo getSensorGeometricInfo(int sensorNumber, Vector2d e){
		
		double orientation= angles[sensorNumber] + Math.toRadians(drone.getCompassOrientationInDegrees()-90);
		
		sensorPosition.set(robotCartesian.getX(), robotCartesian.getY());
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(
				sensorPosition, orientation, e);
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
	
	@Override
	public int getNumberOfSensors() {
		return readings.length;
	}
}
