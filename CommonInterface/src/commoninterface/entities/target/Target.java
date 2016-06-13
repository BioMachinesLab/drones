package commoninterface.entities.target;

import commoninterface.RobotCI;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.target.motion.MixedMotionData;
import commoninterface.entities.target.motion.MotionData;
import commoninterface.utils.jcoord.LatLon;

public class Target extends GeoEntity {
	private static final long serialVersionUID = -8376280695329140274L;

	private double radius;

	private Formation formation = null;
	private MotionData motionData = null;
	private boolean occupied = false;
	private RobotCI occupant = null;
	private boolean inFormation = false;
	private LatLon originalPosition;

	public Target(String name, LatLon latLon, double radius) {
		super(name, latLon);
		this.radius = radius;
		originalPosition = new LatLon(latLon);
	}

	public void step(double time) {
		setLatLon(getTargetMotionData().calculatePosition(time));
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

	public void setMotionData(MotionData motionData) {
		this.motionData = motionData;
	}

	public MotionData getTargetMotionData() {
		if (inFormation) {
			MixedMotionData m = new MixedMotionData(this, originalPosition);

			if (motionData != null) {
				m.addMotionData(motionData);
			}

			if (formation.getMotionData() != null) {
				m.addMotionData(formation.getMotionData());
			}

			return m;
		} else {
			return motionData;
		}
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
		inFormation = true;
	}

	public boolean isInFormation() {
		return inFormation;
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