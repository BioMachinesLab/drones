package network.server.shared.messages;

public class DronesMotorsSet extends ServerMessage {
	private int left = 0;
	private int right = 0;
	private int speedLimit = 0;
	private int offset = 0;
	private String droneName;
	private String droneIP;

	public DronesMotorsSet() {
		super(MessageType.DRONES_MOTORS_SET);
	}

	public void setLeftSpeed(int left) {
		this.left = left;
	}

	public void setRightSpeed(int right) {
		this.right = right;
	}

	public void setDroneIP(String droneIP) {
		this.droneIP = droneIP;
	}
	
	public void setDroneName(String droneName) {
		this.droneName = droneName;
	}

	public void setSpeedLimit(int speedLimit) {
		this.speedLimit = speedLimit;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLeftSpeed() {
		return left;
	}

	public int getRightSpeed() {
		return right;
	}

	public int getSpeedLimit() {
		return speedLimit;
	}

	public int getOffset() {
		return offset;
	}

	public String getDroneIP() {
		return droneIP;
	}
	
	public String getDroneName() {
		return droneName;
	}
}
