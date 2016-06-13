package commoninterface.entities;

import commoninterface.mathutils.Vector2d;

public class ThymioSharedEntity extends VirtualEntity {
	private static final long serialVersionUID = -3519849484398469921L;
	private String observerAddress;

	public ThymioSharedEntity(String name, String observerAddress, Vector2d position) {
		super(name, position);
		this.observerAddress = observerAddress;
	}

	public String getObserverAddress() {
		return observerAddress;
	}

}
