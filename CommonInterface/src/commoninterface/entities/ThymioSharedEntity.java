package commoninterface.entities;

import commoninterface.mathutils.Vector2d;

public class ThymioSharedEntity extends VirtualEntity {

	private String observerAddress;
	
	public ThymioSharedEntity(String name, String observerAddress, Vector2d position) {
		super(name, position);
		this.observerAddress = observerAddress;
	}

	public String getObserverAddress() {
		return observerAddress;
	}
	
}
