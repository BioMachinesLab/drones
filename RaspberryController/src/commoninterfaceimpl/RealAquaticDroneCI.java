package commoninterfaceimpl;

import io.IOManager;
import io.SystemStatusMessageProvider;
import io.input.ControllerInput;
import io.output.ControllerOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import simpletestbehaviors.ChangeWaypointCIBehavior;
import utils.FileLogger;
import network.CommandConnectionListener;
import network.ControllerMessageHandler;
import network.MotorConnectionListener;
import network.broadcast.RealBroadcastHandler;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;
import commoninterface.CISensor;
import commoninterface.LedState;
import commoninterface.RealRobotCI;
import commoninterface.dataobjects.GPSData;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.ConnectionListener;
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.objects.Entity;
import commoninterface.objects.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.jcoord.LatLon;

public class RealAquaticDroneCI extends RealRobotCI implements AquaticDroneCI {

	private static long CYCLE_TIME = 100;// in miliseconds

	private String status = "";
	private String initMessages = "\n";
	private IOManager ioManager;
	private ControllerMessageHandler messageHandler;

	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	private CommandConnectionListener commandConnectionListener;
	private BroadcastHandler broadcastHandler;

	private List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();

	private CIArguments args;
	private boolean run = true;
	private long startTimeInMillis;
	private double timestep = 0;
	private double leftSpeed = 0;
	private double rightSpeed = 0;
	
	private CIBehavior activeBehavior = null;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	
	private Waypoint activeWaypoint;
	
	private ArrayList<CIBehavior> alwaysActiveBehaviors = new ArrayList<CIBehavior>(); 

	@Override
	public void begin(CIArguments args) {
		this.startTimeInMillis = System.currentTimeMillis();
		this.args = args;

		addShutdownHooks();

		initIO();
		initMessageProviders();
		initConnections();
		
		messageHandler = new ControllerMessageHandler(this);
		messageHandler.start();
		
		alwaysActiveBehaviors.add(new ChangeWaypointCIBehavior(new CIArguments(""), this));

		setStatus("Running!\n");

		if(logger != null)
			logger.logMessage(initMessages);
	}

	@Override
	public void run() {
		while (run) {

			long lastCycleTime = System.currentTimeMillis();
			CIBehavior current = activeBehavior;
			try {
				
				for(CIBehavior b : alwaysActiveBehaviors)
					b.step(timestep);
				
				if (current != null) {
					current.step(timestep);
					if (current.getTerminateBehavior()) {
						stopActiveBehavior();
					}
				}
			} catch(Exception e){
				e.printStackTrace();
			}
			
			ioManager.setMotorSpeeds(leftSpeed, rightSpeed);

			if (broadcastHandler != null)
				broadcastHandler.update(timestep);

			long timeToSleep = CYCLE_TIME
					- (System.currentTimeMillis() - lastCycleTime);

			if (timeToSleep > 0) {
				try {
					Thread.sleep(timeToSleep);
				} catch (InterruptedException e) {}
			}

			timestep++;
		}
	}

	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				shutdown();
			}
		});
	}
	
	@Override
	public void shutdown() {
		if(logger != null)
			logger.logMessage("# Shutting down Controller...");
		
		run = false;

		if (logger != null)
			logger.stopLogging();
		

		ioManager.shutdown();

		System.out.println("# Finished Controller cleanup!");
	}

	@Override
	public void reset() {
		ioManager.shutdownMotors();

		if (activeBehavior != null) {
			activeBehavior.cleanUp();
			activeBehavior = null;
			ioManager.setMotorSpeeds(leftSpeed, rightSpeed);
		}
		try {
			// make sure that the current control step is processed
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}

	}
	
	@Override
	public void processInformationRequest(Message request,
			ConnectionHandler conn) {
		messageHandler.addMessage(request, conn);
	}

	// Init's
	private void initIO() {
		ioManager = new IOManager(this);
		initMessages += ioManager.getInitMessages();
	}

	private void initConnections() {
		try {
			
			System.out.println("Starting connections...");

			connectionListener = new ConnectionListener(this);
			connectionListener.start();

			if(logger != null)
				logger.logMessage(".");
			initMessages += "[INIT] ConnectionListener: ok\n";

			motorConnectionListener = new MotorConnectionListener(this);
			motorConnectionListener.start();
			
			commandConnectionListener = new CommandConnectionListener(this);
			commandConnectionListener.start();

			ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
			broadcastMessages.add(new HeartbeatBroadcastMessage(this));
			broadcastMessages.add(new PositionBroadcastMessage(this));
					
			broadcastHandler = new RealBroadcastHandler(this, broadcastMessages);

			if(logger != null)
				logger.logMessage(".");
			initMessages += "[INIT] MotorConnectionListener: ok\n";

		} catch (IOException e) {
			initMessages += "[INIT] Unable to start Network Connection Listeners! ("
					+ e.getMessage() + ")\n";
		}
	}

	/**
	 * Create a message provider for all the possible message provider classes
	 * like the inputs, outputs, system information queries
	 */
	private void initMessageProviders() {
		System.out.println("Creating Message Providers:");
		//@miguelduarte42 SystemInfoMessageProvider takes ~20 seconds and it
		//is not currently used, so I just removed it
//		messageProviders.add(new SystemInfoMessageProvider());
//		System.out.println("\tSystemInfoMessageProvider");
		messageProviders.add(new SystemStatusMessageProvider(this));
		System.out.println("\tSystemStatusMessageProvider");

		for (ControllerInput i : ioManager.getInputs()) {
			if (i instanceof MessageProvider) {
				messageProviders.add((MessageProvider) i);
				System.out.println("\t"+i.getClass().getSimpleName());
			}
		}

		for (ControllerOutput o : ioManager.getOutputs()) {
			if (o instanceof MessageProvider) {
				messageProviders.add((MessageProvider) o);
				System.out.println("\t"+o.getClass().getSimpleName());
			}
		}
	}

	// Behaviors
	@Override
	public void startBehavior(CIBehavior b) {
		stopActiveBehavior();
		activeBehavior = b;
	}

	@Override
	public void stopActiveBehavior() {
		if (activeBehavior != null) {
			activeBehavior.cleanUp();
			activeBehavior = null;
			ioManager.setMotorSpeeds(leftSpeed, rightSpeed);
		}
	}

	// Setters
	@Override
	public void setLed(int index, LedState state) {
		if (ioManager.getDebugLeds() != null) {
			if (index >= 0
					&& index < ioManager.getDebugLeds().getNumberOfOutputs()) {
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
				if(logger != null)
					logger.logError("Invalid led index: " + index
						+ ", must be >= 0 and < "
						+ ioManager.getDebugLeds().getNumberOfOutputs());
			}
		}
	}

	@Override
	public void setMotorSpeeds(double left, double right) {
		leftSpeed = left;
		rightSpeed = right;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	// Getters
	@Override
	public String getNetworkAddress() {
		return NetworkUtils.getAddress();
	}

	@Override
	public BroadcastHandler getBroadcastHandler() {
		return broadcastHandler;
	}

	@Override
	public CIBehavior getActiveBehavior() {
		return activeBehavior;
	}

	@Override
	public ArrayList<CISensor> getCISensors() {
		return cisensors;
	}

	@Override
	public ArrayList<Entity> getEntities() {
		return entities;
	}

	@Override
	public String getInitMessages() {
		return initMessages;
	}

	@Override
	public List<MessageProvider> getMessageProviders() {
		return messageProviders;
	}

	@Override
	public double getCompassOrientationInDegrees() {
		try {
			double orientation = ioManager.getCompassModule()
					.getHeadingInDegrees();
			return (orientation % 360.0);
		} catch (Exception e) {
		}

		return -1;
	}

	@Override
	public LatLon getGPSLatLon() {
		try {
			GPSData gpsData = ioManager.getGpsModule().getReadings();

			if (gpsData.getLatitudeDecimal() == 0
					|| gpsData.getLongitudeDecimal() == 0)
				return null;

			return new LatLon(gpsData.getLatitudeDecimal(),
					gpsData.getLongitudeDecimal());
		} catch (Exception e) {
		}

		return null;
	}

	@Override
	public double getGPSOrientationInDegrees() {
		try {
			GPSData gpsData = ioManager.getGpsModule().getReadings();
			return gpsData.getOrientation();
		} catch (Exception e) {
		}

		return -1;
	}

	@Override
	public double getTimeSinceStart() {
		long elapsedMillis = System.currentTimeMillis()
				- this.startTimeInMillis;

		return ((double) elapsedMillis) / 1000.0;
	}

	@Override
	public String getStatus() {
		return status;
	}

	public IOManager getIOManager() {
		return ioManager;
	}
	
	public void startLogger() {
		FileLogger fileLogger = new FileLogger(this);
		fileLogger.start();
		this.logger = fileLogger;
	}
	
	@Override
	public Waypoint getActiveWaypoint() {
		return activeWaypoint;
	}
	
	@Override
	public void setActiveWaypoint(Waypoint wp) {
		this.activeWaypoint = wp;	
	}

}
