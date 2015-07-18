package commoninterface.network.messages;

import commoninterface.mathutils.Vector2d;

public class ThymioVirtualPositionMessage extends Message {

	private static final long serialVersionUID = -398651655354216523L;
	private Vector2d virtualThymioPosition;
	private double virtualOrientation;

	public ThymioVirtualPositionMessage(Vector2d virtualThymioPosition,
			double virtualOrientation, String senderHostname) {
		super(senderHostname);
		this.virtualThymioPosition = virtualThymioPosition;
		this.virtualOrientation = virtualOrientation;
	}

	public Vector2d getVirtualThymioPosition() {
		return virtualThymioPosition;
	}

	public double getVirtualOrientation() {
		return virtualOrientation;
	}
	
	@Override
	public Message getCopy() {
		return new ThymioVirtualPositionMessage(virtualThymioPosition, virtualOrientation, senderHostname);
	}

}
