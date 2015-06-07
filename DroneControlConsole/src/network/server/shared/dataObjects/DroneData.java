package network.server.shared.dataObjects;

import java.io.Serializable;

import network.server.shared.BehaviorServerMessage;
import network.server.shared.GPSServerData;
import network.server.shared.NeuralActivationsServerMessage;

public class DroneData implements Serializable {
	private static final long serialVersionUID = -5255732989855282894L;

	/*
	 * Variables
	 */
	private String name = "";
	private String ipAddr;

	// Broadcasted informations
	private long timeSinceLastHeartbeat = -1;

	// Telemetry Informations
	private double compassOrientation;
	private GPSServerData gpsData;

	// System informations and status
	private String systemStatusMessage = "";
	private BehaviorServerMessage behaviourMessage;
	private NeuralActivationsServerMessage neuralActivations;
	private BatteryStatusServerData batteryStatus;

	/*
	 * Methods
	 */
	// Constructors
	public DroneData(String ipAddr, String name) {
		this.ipAddr = ipAddr;
		this.name = name;
		gpsData = new GPSServerData();
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

	public BatteryStatusServerData getBatteryStatus() {
		return batteryStatus;
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
	
	public void setOrientation(double compassOrientation) {
		this.compassOrientation = compassOrientation;
	}

	public void setSystemStatusMessage(String systemStatusMessage) {
		this.systemStatusMessage = systemStatusMessage;
	}

	public void setBehaviour(BehaviorServerMessage behaviourMessage) {
		this.behaviourMessage = behaviourMessage;
	}

	public void setNeuralActivations(
			NeuralActivationsServerMessage neuralActivations) {
		this.neuralActivations = neuralActivations;
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

	public void setBatteryStatus(BatteryStatusServerData batteryStatus) {
		this.batteryStatus = batteryStatus;
	}
}
