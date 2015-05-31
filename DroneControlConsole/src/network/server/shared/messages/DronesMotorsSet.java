package network.server.shared.messages;

public class DronesMotorsSet extends ServerMessage {
	private int left = 0;
	private int right = 0;
	private String droneID;

	public DronesMotorsSet() {
		super(MessageType.DRONES_MOTORS_SET);
	}

	public void setLeftSpeed(int left) {
		this.left = left;
	}

	public void setRightSpeed(int right) {
		this.right = right;
	}

	public int getLeftSpeed() {
		return left;
	}

	public int getRightSpeed() {
		return right;
	}

	public void setDroneID(String droneID) {
		this.droneID = droneID;
	}

	public String getDroneID() {
		return droneID;
	}
}
