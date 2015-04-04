package objects;

import commoninterface.utils.jcoord.LatLon;

public abstract class GeoEntity extends Entity {
	
	protected LatLon latLon;
	
	public GeoEntity(String name, LatLon latLon) {
		super(name);
		this.latLon = latLon;
	}
	
	public LatLon getLatLon() {
		return latLon;
	}
	

}
