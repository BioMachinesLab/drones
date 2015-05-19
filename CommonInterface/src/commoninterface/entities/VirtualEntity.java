package commoninterface.entities;

import commoninterface.mathutils.Vector2d;

public class VirtualEntity extends Entity {

	private Vector2d position;

	public VirtualEntity(String name, Vector2d position) {
		super(name);
		this.position = position;
	}

	public Vector2d getPosition() {
		return position;
	}
	
	public double getX() {
		return position.x;
	}
	
	public double getY() {
		return position.y;
	}
	
}
