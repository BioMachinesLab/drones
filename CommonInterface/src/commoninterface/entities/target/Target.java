package commoninterface.entities.target;

import commoninterface.RobotCI;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;

public class Target extends Waypoint {
	private static final long serialVersionUID = -2587408229710651773L;
	private boolean occupied = false;
	private MotionData targetMotionData = null;
	private double radius = Double.MIN_VALUE;
	private RobotCI occupant = null;

	public Target(String name, LatLon latLon) {
		super(name, latLon);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Target)
			return super.equals(obj) && latLon.equals(((Target) obj).getLatLon())
					&& occupied == ((Target) obj).isOccupied() && radius == ((Target) obj).getRadius();
		return false;
	}

	public void setOccupied(boolean occupied) {
		this.occupied = occupied;

		if (!occupied) {
			occupant = null;
		}
	}

	public boolean isOccupied() {
		return occupied;
	}

	@Override
	public String toString() {
		return "[Target] " + super.toString() + "\tHash: " + System.identityHashCode(this) + "\tName: "
				+ name.substring(6) + "\tOccupied: " + occupied;
	}

	public void setMotionData(MotionData targetMotionData) {
		this.targetMotionData = targetMotionData;
	}

	public MotionData getTargetMotionData() {
		return targetMotionData;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRadius() {
		return radius;
	}

	public void setOccupant(RobotCI occupant) {
		this.occupant = occupant;
		occupied = (occupant != null);
	}

	public RobotCI getOccupant() {
		return occupant;
	}
}