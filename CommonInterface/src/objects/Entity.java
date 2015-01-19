package objects;

import commoninterface.mathutils.Vector2d;

public abstract class Entity {
	
	protected double latitude;
	protected double longitude;
	protected double orientation;
	protected String name = "";
	protected Vector2d latLon;
	
	public Entity(double lat, double lon) {
		this.latitude = lat;
		this.longitude = lon;
	}
	
	public Entity(double lat, double lon, double orientation) {
		this(lat, lon);
		this.orientation = orientation;
	}
	
	public Entity(double lat, double lon, String name) {
		this(lat, lon);
		this.name = name;
	}
	
	public Entity(double lat, double lon, double orientation, String name) {
		this(lat, lon, orientation);
		this.name = name;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public String getName() {
		return name;
	}
	
	public double getOrientation() {
		return orientation;
	}
	
	public Vector2d getLatLon() {
		if(latLon == null)
			latLon = new Vector2d(latitude, longitude);
		return latLon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
