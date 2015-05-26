package dataObjects;

import java.io.Serializable;
import java.net.InetAddress;

import network.server.BehaviorServerMessage;
import network.server.GPSServerData;
import network.server.NeuralActivationsServerMessage;

import commoninterface.dataobjects.GPSData;
import commoninterface.entities.RobotLocation;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.NeuralActivationsMessage;

public class DroneData implements Serializable {
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
	private GPSServerData gpsData;

	// System informations and status
	private String systemStatusMessage = "";
	private BehaviorServerMessage behaviourMessage;
	private NeuralActivationsServerMessage neuralActivations;

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

	public DroneData() {
		this(null, "<no name>");
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

	public String getSystemStatusMessage() {
		return systemStatusMessage;
	}

	public BehaviorMessage getBehaviour() {
		return behaviourMessage.getAsBehaviorMessage();
	}

	public NeuralActivationsMessage getNeuralActivations() {
		return neuralActivations.getAsNeuralActivationsMessage();
	}

	public GPSServerData getGPSData() {
		return gpsData;
	}

	// Setters
	public void setIpAddr(InetAddress ipAddr) {
		this.ipAddr = ipAddr;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public void setSystemStatusMessage(String systemStatusMessage) {
		this.systemStatusMessage = systemStatusMessage;
	}

	public void setBehaviour(BehaviorMessage behaviourMessage) {
		this.behaviourMessage = new BehaviorServerMessage(behaviourMessage);
	}

	public void setNeuralActivations(NeuralActivationsMessage neuralActivations) {
		this.neuralActivations = new NeuralActivationsServerMessage(
				neuralActivations);
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
