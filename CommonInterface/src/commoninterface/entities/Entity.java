package commoninterface.entities;

import java.io.Serializable;

public abstract class Entity implements Serializable{
	
	protected String name = "";
	protected long timestepReceived = 0;
	
	public Entity(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setTimestepReceived(long timestep) {
		this.timestepReceived = timestep;
	}
	
	public long getTimestepReceived() {
		return timestepReceived;
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
	
	@Override
	public String toString() {
		return name;
	}
	
}
