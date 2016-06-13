package commoninterface.entities;

import commoninterface.mathutils.Vector2d;

public class VirtualEntity extends Entity {
	private static final long serialVersionUID = -8783337426327198048L;
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

	@Override
	public VirtualEntity clone() {
		return new VirtualEntity(name, new Vector2d(position));
	}
}
