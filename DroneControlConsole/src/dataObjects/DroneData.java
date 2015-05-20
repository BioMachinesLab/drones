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

	public String getSystemStatusMessage() {
		return systemStatusMessage;
	}

	public BehaviorMessage getBehaviour() {
		return behaviourMessage;
	}

	public NeuralActivationsMessage getNeuralActivations() {
		return neuralActivations;
	}

	public GPSData getGPSData(){
		return gpsData;
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

	public void setSystemStatusMessage(String systemStatusMessage) {
		this.systemStatusMessage = systemStatusMessage;
	}

	public void setBehaviour(BehaviorMessage behaviourMessage) {
		this.behaviourMessage = behaviourMessage;
	}

	public void setNeuralActivations(NeuralActivationsMessage neuralActivations) {
		this.neuralActivations = neuralActivations;
	}

	public void setGPSData(GPSData gpsData){
		this.gpsData=gpsData;
	}
}
