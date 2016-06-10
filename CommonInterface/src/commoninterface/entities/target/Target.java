package commoninterface.entities.target;

import commoninterface.RobotCI;
import commoninterface.entities.GeoEntity;
import commoninterface.utils.jcoord.LatLon;

public class Target extends GeoEntity {
	private static final long serialVersionUID = -5889567979726217144L;
	private double radius;

	private Formation formation = null;
	private MotionData targetMotionData = null;
	private boolean occupied = false;
	private RobotCI occupant = null;

	public Target(String name, LatLon latLon, double radius) {
		super(name, latLon);
		this.radius = radius;
	}

	/*
	 * Getters and setters
	 */
	public void setOccupied(boolean occupied) {
		this.occupied = occupied;

		if (!occupied) {
			occupant = null;
		}
	}

	public void setMotionData(MotionData targetMotionData) {
		this.targetMotionData = targetMotionData;
	}

	public MotionData getTargetMotionData() {
		return targetMotionData;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public boolean isOccupied() {
		return occupied;
	}

	public void setOccupant(RobotCI occupant) {
		this.occupant = occupant;
		occupied = (occupant != null);
	}

	public RobotCI getOccupant() {
		return occupant;
	}

	public Formation getFormation() {
		return formation;
	}

	public void setFormation(Formation formation) {
		this.formation = formation;
	}

	/*
	 * Default methods
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Target)
			return super.equals(obj) && latLon.equals(((Target) obj).getLatLon())
					&& occupied == ((Target) obj).isOccupied() && radius == ((Target) obj).getRadius();
		return false;
	}

	@Override
	public String toString() {
		if (formation != null) {
			return "[Formation Target] Formation: " + formation.getName() + "\t" + super.toString() + "\tHash: "
					+ System.identityHashCode(this) + "\tName: " + name.substring(6) + "\tOccupied: " + occupied;
		} else {
			return "[Target] " + super.toString() + "\tHash: " + System.identityHashCode(this) + "\tName: "
					+ name.substring(6) + "\tOccupied: " + occupied;
		}
	}
}