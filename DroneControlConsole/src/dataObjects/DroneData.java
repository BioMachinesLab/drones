package dataObjects;

import java.net.InetAddress;

import commoninterface.entities.RobotLocation;

public class DroneData {
	/*
	 * Variables
	 */
	private String name = "";
	private InetAddress ipAddr;

	// Broadcasted informations
	private long timeSinceLastHeartbeat = -1;

	// Telemetry Informations
	private RobotLocation robotLocation;
	private double compassOrientation;

	/*
	 * Methods
	 */
	// Constructors
	public DroneData(InetAddress ipAddr, String name) {
		this.ipAddr = ipAddr;
		this.name = name;
	}

	public DroneData(String name) {
		this(null, name);
	}

	public DroneData(InetAddress ipAddr) {
		this(ipAddr, "<no name>");
	}

	// Getters
	public String getName() {
		return name;
	}

	public InetAddress getIpAddr() {
		return ipAddr;
	}

	public long getTimeSinceLastHeartbeat() {
		return timeSinceLastHeartbeat;
	}

	public RobotLocation getRobotLocation() {
		return robotLocation;
	}

	public double getCompassOrientation() {
		return compassOrientation;
	}

	// Setters
	public void setTimeSinceLastHeartbeat(long timeSinceLastHeartbeat) {
		this.timeSinceLastHeartbeat = timeSinceLastHeartbeat;
	}

	public void setRobotLocation(RobotLocation robotLocation) {
		this.robotLocation = robotLocation;
		this.compassOrientation = robotLocation.getOrientation();
	}

	public void setOrientation(double compassOrientation) {
		this.compassOrientation = compassOrientation;
	}

}
