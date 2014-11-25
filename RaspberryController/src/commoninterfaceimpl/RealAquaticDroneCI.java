package commoninterfaceimpl;

import io.IOManager;
import io.SystemInfoMessageProvider;
import io.SystemStatusMessageProvider;
import io.input.ControllerInput;
import io.output.ControllerOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import network.ConnectionHandler;
import network.ConnectionListener;
import network.ControllerMessageHandler;
import network.MotorConnectionListener;
import network.messages.Message;
import network.messages.MessageProvider;
import objects.Waypoint;
import simpletestbehaviors.TurnToOrientationCIBehavior;
import utils.Nmea0183ToDecimalConverter;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;

import dataObjects.GPSData;

public class RealAquaticDroneCI extends Thread implements AquaticDroneCI {

	private String status = "";
	private String initMessages = "\n";
	private IOManager ioManager;
	private ControllerMessageHandler messageHandler;
	
	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	
	private List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private List<CIBehavior> behaviors = new ArrayList<CIBehavior>();
	
	private String[] args;
	private CILogger logger;
	private long     startTimeInMillis;
	
	private LinkedList<CIBehavior> activeBehaviors = new LinkedList<CIBehavior>();
	private LinkedList<Waypoint> waypoints = new LinkedList<Waypoint>();
	
	@Override
	public void begin(String[] args, CILogger logger) {		
		this.startTimeInMillis = System.currentTimeMillis();
		this.args   = args;
		this.logger = logger; 
		
		addShutdownHooks();
		
		initIO();
		initBehaviors();
		initMessageProviders();
		initConnections();

		messageHandler = new ControllerMessageHandler(this);
		messageHandler.start();

		setStatus("Running!\n");

		logger.logMessage(initMessages);
	}
	
	@Override
	public void start() {
		while(true) {
			
			Iterator<CIBehavior> i = activeBehaviors.iterator();
			
			while(i.hasNext()) {
				CIBehavior b = i.next();
				b.step();
			}
			
			try {
				Thread.sleep((long) (1000*behaviors.get(0).getControlStepPeriod()));
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public void shutdown() {
		logger.logMessage("# Shutting down Controller...");
		
		if (logger != null)
			logger.stopLogging();

		System.out.println("# Finished Controller cleanup!");
	}

	@Override
	public void setMotorSpeeds(double left, double right) {
		System.out.println("Left: "+(left / 2.0 + 0.5)+" Right: "+ (right/ 2.0 + 0.5));
		ioManager.setMotorSpeeds(left / 2.0 + 0.5, right / 2.0 + 0.5);
	}

	@Override
	public double getCompassOrientationInDegrees() {
		double orientation = ioManager.getCompassModule().getHeadingInDegrees();
		return (orientation % 360.0);
	}

	@Override
	public double getGPSLatitude() {
		GPSData gpsData = ioManager.getGpsModule().getReadings();
		
		//NMEA format: e.g. 3844.9474N 00909.2214W
		String latitude = gpsData.getLatitude();

		double lat = Double.parseDouble(latitude.substring(0,latitude.length()-1));
		char latPos = latitude.charAt(latitude.length()-1);

		lat = Nmea0183ToDecimalConverter.convertLatitudeToDecimal(lat, latPos);
		
		return lat;
	}

	@Override
	public double getGPSLongitude() {
		GPSData gpsData = ioManager.getGpsModule().getReadings();

		//NMEA format: e.g. 3844.9474N 00909.2214W
		String longitude = gpsData.getLongitude();
		
		double lon = Double.parseDouble(longitude.substring(0,longitude.length()-1));
		char lonPos = longitude.charAt(longitude.length()-1);
		
		lon = Nmea0183ToDecimalConverter.convertLongitudeToDecimal(lon, lonPos);
		
		return lon;
	}

	@Override
	public double getTimeSinceStart() {
		long elapsedMillis =  System.currentTimeMillis() - this.startTimeInMillis;
		
		return ((double) elapsedMillis) / 1000.0;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void processInformationRequest(Message request, ConnectionHandler conn) {
		messageHandler.addMessage(request, conn);
	}

	public String getInitMessages() {
		return initMessages;
	}

	public List<MessageProvider> getMessageProviders() {
		return messageProviders;
	}


	public IOManager getIOManager() {
		return ioManager;
	}
		
	private void initIO() {
		ioManager = new IOManager(this);
		initMessages += ioManager.getInitMessages();
	}

	private void initConnections() {
		try {
			connectionListener = new ConnectionListener(this);
			connectionListener.start();

			logger.logMessage(".");
			initMessages += "[INIT] ConnectionListener: ok\n";

			motorConnectionListener = new MotorConnectionListener(this);
			motorConnectionListener.start();

			logger.logMessage(".");
			initMessages += "[INIT] MotorConnectionListener: ok\n";

		} catch (IOException e) {
			initMessages += "[INIT] Unable to start Network Connection Listeners! ("
					+ e.getMessage() + ")\n";
		}
	}
	
	private void initBehaviors() {

		try{
			CIBehavior turnBehavior = new TurnToOrientationCIBehavior(new String[]{}, this, logger);
			CIBehavior go = new GoToWaypointCIBehavior(new String[]{}, this, logger);
			behaviors.add(turnBehavior);
			behaviors.add(go);
		} catch(Exception e) {
			initMessages += "[INIT] Behavior "+e.getMessage()+"\n";
		}
	}


	/**
	 * Create a message provider for all the possible message provider classes
	 * like the inputs, outputs, system information queries
	 */
	private void initMessageProviders() {
		messageProviders.add(new SystemInfoMessageProvider());
		messageProviders.add(new SystemStatusMessageProvider(this));

		for (ControllerInput i : ioManager.getInputs()) {
			if (i instanceof MessageProvider)
				messageProviders.add((MessageProvider) i);
		}

		for (ControllerOutput o : ioManager.getOutputs()) {
			if (o instanceof MessageProvider)
				messageProviders.add((MessageProvider) o);
		}
	}

	@Override
	public void setLed(int index, AquaticDroneCI.LedState state) {
		if (index >= 0 && index < ioManager.getDebugLeds().getNumberOfOutputs()) {
			switch (state) {
			case ON:
				ioManager.getDebugLeds().removeBlinkLed(index);
				ioManager.getDebugLeds().setValue(index, 1);
				break;
			case OFF:
				ioManager.getDebugLeds().removeBlinkLed(index);
				ioManager.getDebugLeds().setValue(index, 0);
				break;
			case BLINKING:
				ioManager.getDebugLeds().addBlinkLed(index);
				break;
			} 
		} else {
			logger.logError("Invalid led index: " + index + ", must be >= 0 and < " + ioManager.getDebugLeds().getNumberOfOutputs());
		}
	}
	
	public List<CIBehavior> getBehaviors() {
		return behaviors;
	}
	
	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				shutdown();
			}
		});
	}
	
	public void executeBehavior(CIBehavior behavior, boolean active) {
		
		if(!active) {
			activeBehaviors.remove(behavior);
			behavior.cleanUp();
		} else {
			activeBehaviors.add(behavior);
		}
	}
	
	@Override
	public LinkedList<Waypoint> getWaypoints() {
		return waypoints;
	}
}