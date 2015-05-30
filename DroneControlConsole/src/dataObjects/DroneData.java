package dataObjects;

import java.io.Serializable;

import network.server.BehaviorServerMessage;
import network.server.GPSServerData;
import network.server.NeuralActivationsServerMessage;
import network.server.RobotServerLocation;

import commoninterface.network.messages.NeuralActivationsMessage;

public class DroneData implements Serializable {
	/*
	 * Variables
	 */
	private String name = "";
	private String ipAddr;

	// Broadcasted informations
	private long timeSinceLastHeartbeat = -1;

	// Telemetry Informations
	private RobotServerLocation robotLocation;
	private double compassOrientation;
	private GPSServerData gpsData;

	// System informations and status
	private String systemStatusMessage = "";
	private BehaviorServerMessage behaviourMessage;
	private NeuralActivationsServerMessage neuralActivations;

	/*
	 * Methods
	 */
	// Constructors
	public DroneData(String ipAddr, String name) {
		this.ipAddr = ipAddr;
		this.name = name;
	}

	public DroneData(String name) {
		this(null, name);
	}

	public DroneData() {
		this(null, "<no name>");
	}

	// Getters
	public String getName() {
		return name;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public long getTimeSinceLastHeartbeat() {
		return timeSinceLastHeartbeat;
	}

	public RobotServerLocation getRobotLocation() {
		return robotLocation;
	}

	public double getCompassOrientation() {
		return compassOrientation;
	}

	public String getSystemStatusMessage() {
		return systemStatusMessage;
	}

	public BehaviorServerMessage getBehaviour() {
		return behaviourMessage;
	}

	public NeuralActivationsServerMessage getNeuralActivations() {
		return neuralActivations;
	}

	public GPSServerData getGPSData() {
		return gpsData;
	}

	// Setters
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTimeSinceLastHeartbeat(long timeSinceLastHeartbeat) {
		this.timeSinceLastHeartbeat = timeSinceLastHeartbeat;
	}

	public void setRobotLocation(RobotServerLocation robotLocation) {
		this.robotLocation = robotLocation;
		this.compassOrientation = robotLocation.getOrientation();
	}

	public void setOrientation(double compassOrientation) {
		this.compassOrientation = compassOrientation;
	}

	public void setSystemStatusMessage(String systemStatusMessage) {
		this.systemStatusMessage = systemStatusMessage;
	}

	public void setBehaviour(BehaviorServerMessage behaviourMessage) {
		this.behaviourMessage = behaviourMessage;
	}

	public void setNeuralActivations(NeuralActivationsMessage neuralActivations) {
		this.neuralActivations = new NeuralActivationsServerMessage(
				neuralActivations);
	}

	public void setGPSData(GPSServerData gpsData) {
		this.gpsData = gpsData;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DroneData) {
			DroneData drone = ((DroneData) obj);
			return (drone.getIpAddr() == null && ipAddr == null || drone
					.getIpAddr().equals(ipAddr))
					&& (drone.getName() == null && name == null || drone
							.getName().equals(name));

		} else {
			return false;
		}
	}

}
