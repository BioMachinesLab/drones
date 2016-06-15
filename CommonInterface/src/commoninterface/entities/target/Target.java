package commoninterface.entities.target;

import commoninterface.entities.GeoEntity;
import commoninterface.entities.target.motion.MixedMotionData;
import commoninterface.entities.target.motion.MotionData;
import commoninterface.utils.jcoord.LatLon;

public class Target extends GeoEntity {
	private static final long serialVersionUID = -8376280695329140274L;
	private static final double HYSTERESIS_TIME = 50; // In miliseconds

	private double radius;
	private Formation formation = null;
	private MotionData motionData = null;
	private boolean occupied = false;
	private String occupantID = null;
	private boolean inFormation = false;
	private LatLon originalPosition;

	private double histeresysTime = HYSTERESIS_TIME;
	private double histeresysTimeStorage = HYSTERESIS_TIME;

	private double enterTime = 0;
	private boolean entered = false;

	public Target(String name, LatLon latLon, double radius) {
		super(name, latLon);
		this.radius = radius;
		originalPosition = new LatLon(latLon);
	}

	public void step(double time) {
		setLatLon(getMotionData().calculatePosition(time));

		if (entered) {
			enterTime = time;
			entered = false;
			occupied = true;
		}

		if (occupied && (time - enterTime >= histeresysTime)) {
			occupantID = null;
			occupied = false;
		}
	}

	/*
	 * Getters and setters
	 */
	public void setOccupied(boolean occupied) {
		entered = occupied;
	}

	public void setMotionData(MotionData motionData) {
		this.motionData = motionData;
	}

	public void enableHisteresys(boolean enable) {
		if (enable) {
			histeresysTime = histeresysTimeStorage;
		} else {
			histeresysTime = 0;
		}
	}

	public void setHisteresysTime(double histeresysTime) {
		this.histeresysTimeStorage = histeresysTime;

		if (histeresysTime != 0) {
			this.histeresysTime = histeresysTime;
		}
	}

	public MotionData getMotionData() {
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

	public void setOccupantID(String occupantID) {
		if (!(occupantID == null && this.occupantID == null)) {
			entered = true;
		}
		this.occupantID = occupantID;
	}

	public String getOccupantID() {
		return occupantID;
	}

	public Formation getFormation() {
		return formation;
	}

	public void setFormation(Formation formation) {
		this.formation = formation;

		inFormation = (formation != null);
	}

	public void setInFormation(boolean inFormation) {
		this.inFormation = inFormation;
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
		if (inFormation) {
			if (formation == null) {
				return "[Formation Target] Formation: null\t" + super.toString() + "\tHash: "
						+ System.identityHashCode(this) + "\tName: " + name.substring(6) + "\tOccupied: " + occupied;
			} else {
				return "[Formation Target] Formation: " + formation.getName() + "\t" + super.toString() + "\tHash: "
						+ System.identityHashCode(this) + "\tName: " + name.substring(6) + "\tOccupied: " + occupied;
			}
		} else {
			return "[Target] " + super.toString() + "\tHash: " + System.identityHashCode(this) + "\tName: "
					+ name + "\tOccupied: " + occupied;
		}
	}

	@Override
	public Target clone() {
		Target t = new Target(occupantID, new LatLon(originalPosition), radius);
		t.setOccupantID(occupantID);
		t.setOccupied(occupied);
		t.setFormation(formation);
		t.setHisteresysTime(histeresysTime);
		t.setMotionData(motionData.clone());

		return t;
	}
}