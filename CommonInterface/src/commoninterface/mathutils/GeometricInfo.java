package commoninterface.mathutils;

import java.io.Serializable;

public class GeometricInfo implements Serializable {
	private double angle;
	private double distance;
	public GeometricInfo(double angle, double distance) {
		super();
		this.angle = angle;
		this.distance = distance;
	}
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "GeometricInfo [angle=" + angle + ", distance=" + distance + "]";
	}
	
	
}