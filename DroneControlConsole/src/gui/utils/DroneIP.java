package gui.utils;

public class DroneIP {

	public enum DroneStatus{RUNNING, DETECTED, NOT_RUNNING};
	
	private String ip;
	private DroneStatus status;
	
	
	public DroneIP(String ip) {
		super();
		this.ip = ip;
		status = DroneStatus.NOT_RUNNING;
	}


	public String getIp() {
		return ip;
	}


	public DroneStatus getStatus() {
		return status;
	}
	
	public void setStatus(DroneStatus status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return ip;
	}
	
}
