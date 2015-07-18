package commoninterface.entities;

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

	public String getLogMessage() {
		String str = getLatLon().getLat()+" "+getLatLon().getLon();
		return "entity added "+getClass().getSimpleName()+" "+getName()+" "+str;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+";"+getLatLon().getLat()+";"+getLatLon().getLon()+";";
	}

}
