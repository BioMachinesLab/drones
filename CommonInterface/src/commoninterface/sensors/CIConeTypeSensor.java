package commoninterface.sensors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import objects.Entity;
import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.mathutils.GeometricCalculator;
import commoninterface.mathutils.GeometricInfo;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public abstract class CIConeTypeSensor extends CISensor {

	public static final float NOISESTDEV = 0.05f; 
	public static final int DEFAULT_RANGE = 10;

	protected double 			   range;
	protected double[]             readings;
	protected double[] 			   angles;
	protected double               angleposition;
	protected int 				   numberOfSensors;
	protected Vector2d 			   sensorPosition 	= new Vector2d();
	protected double 			   openingAngle = 90;
	protected GeometricCalculator geoCalc;
	protected Random random;
	protected double[] obstacleReadings;
	protected Vector2d robotPos = new Vector2d();
	
	public CIConeTypeSensor(int id, AquaticDroneCI drone, CIArguments args) {
		super(id, drone, args);
		this.geoCalc = new GeometricCalculator();
		
		numberOfSensors = (args.getArgumentIsDefined("numbersensors")) ? args.getArgumentAsInt("numbersensors") : 1;
		range = (args.getArgumentIsDefined("range")) ? args.getArgumentAsDouble("range") : DEFAULT_RANGE;
		openingAngle = Math.toRadians((args.getArgumentIsDefined("angle")) ? args.getArgumentAsDouble("angle") : 90);
		
		this.readings 		= new double[numberOfSensors];
		this.angles 		= new double[numberOfSensors];
		
		setupPositions(numberOfSensors);
	}
	
	public void setupPositions(Vector2d[] positions) {
		Vector2d frontVector = new Vector2d(1,0); 
		for (int i=0;i< numberOfSensors;i++){
			Vector2d v = positions[i];
			angles[i] = (v.getY()<0?-1:1)*v.angle(frontVector);
		}
	}
	
	public void setupPositions(int numberSensors) {
		double delta = 2 * Math.PI / numberSensors;
		double angle = 0;
		for (int i=0;i< numberSensors;i++){
			angles[i] = angle;
			angle+=delta;
		}
	}

	public double[] getAngles() {
		return angles;
	}
	
	public double getRange() {
		return range;
	}
	
	public double getOpeningAngle() {
		return openingAngle;
	}
	
	public void update(double time, ArrayList<Entity> entities) {
		
		robotPos = CoordinateUtilities.GPSToCartesian(drone.getGPSLatitude(), drone.getGPSLongitude());
		
		try { 
			for(int j = 0; j < readings.length; j++){
				readings[j] = 0.0;
			}
			Iterator<Entity> iterator = entities.iterator();
			while(iterator.hasNext()){
				Entity source=iterator.next();
				calculateSourceContributions(source);
			}
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	public double getSensorReading(int sensorNumber){
		return readings[sensorNumber];
	}
	
	protected void calculatedObstacleContributions(Entity source) {
		for(int j = 0; j < obstacleReadings.length; j++){
			obstacleReadings[j] = Math.max(calculateContributionToSensor(j, source), readings[j]);
		}
	}

	protected void calculateSourceContributions(Entity source) {
		for(int j = 0; j < readings.length; j++){
			if(openingAngle > 0.018){ //1degree
				readings[j] = Math.max(calculateContributionToSensor(j, source), readings[j]);
			}
		}
	}

	protected GeometricInfo getSensorGeometricInfo(int sensorNumber, Vector2d toPoint){
		double orientation = angles[sensorNumber] + Math.toRadians(drone.getCompassOrientationInDegrees());
		sensorPosition.set(robotPos.getX(), robotPos.getY());
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(sensorPosition, orientation, toPoint);
		return sensorInfo;
	}
	
	protected abstract double calculateContributionToSensor(int i, Entity source);

	public int getNumberOfSensors() {
		return numberOfSensors;
	}
	
	public void setRange(double range) {
		this.range = range;
	}
	
	public void setOpeningAngle(double openingAngle) {
		this.openingAngle = openingAngle;
	}
}