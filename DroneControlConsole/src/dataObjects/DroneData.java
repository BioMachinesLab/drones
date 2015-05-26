package dataObjects;

import java.net.InetAddress;

import commoninterface.dataobjects.GPSData;
import commoninterface.entities.RobotLocation;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.NeuralActivationsMessage;

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
	private GPSData gpsData;

	// System informations and status
	private String systemStatusMessage = "";
	private BehaviorMessage behaviourMessage;
	private NeuralActivationsMessage neuralActivations;

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
		return behaviourMessage;
	}

	public NeuralActivationsMessage getNeuralActivations() {
		return neuralActivations;
	}

	public GPSData getGPSData() {
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
		this.behaviourMessage = behaviourMessage;
	}

	public void setNeuralActivations(NeuralActivationsMessage neuralActivations) {
		this.neuralActivations = neuralActivations;
	}

	public void setGPSData(GPSData gpsData) {
		this.gpsData = gpsData;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DroneData) {
			return ((DroneData) obj).getIpAddr().equals(ipAddr)
					&& ((DroneData) obj).getName().equals(name);
		} else {
			return false;
		}
	}
}
