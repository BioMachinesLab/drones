package commoninterface.entities;

import commoninterface.utils.jcoord.LatLon;

public abstract class GeoEntity extends Entity {

	private static final long serialVersionUID = -2730857744364736763L;
	protected LatLon latLon;

	public GeoEntity(String name, LatLon latLon) {
		super(name);
		this.latLon = latLon;
	}

	public LatLon getLatLon() {
		return latLon;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ";" + getLatLon().getLat() + ";" + getLatLon().getLon() + ";";
	}

	public void setLatLon(LatLon latLon) {
		this.latLon = latLon;
	}

	@Override
	public abstract GeoEntity clone();
}
