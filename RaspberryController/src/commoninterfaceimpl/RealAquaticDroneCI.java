package commoninterfaceimpl;

import io.IOManager;
import io.SystemInfoMessageProvider;
import io.SystemStatusMessageProvider;
import io.input.ControllerInput;
import io.output.ControllerOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import network.BehaviorConnectionListener;
import network.ConnectionHandler;
import network.ConnectionListener;
import network.ControllerMessageHandler;
import network.MotorConnectionListener;
import network.broadcast.RealBroadcastHandler;
import network.messages.Message;
import network.messages.MessageProvider;
import objects.Entity;
import utils.NetworkUtils;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;
import commoninterface.CISensor;
import commoninterface.LedState;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.utils.CIArguments;
import commoninterface.utils.jcoord.LatLon;
import dataObjects.GPSData;

public class RealAquaticDroneCI extends Thread implements AquaticDroneCI {

	private static long CYCLE_TIME = 100;// in miliseconds

	private String status = "";
	private String initMessages = "\n";
	private IOManager ioManager;
	private ControllerMessageHandler messageHandler;

	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	private BehaviorConnectionListener behaviorConnectionListener;
	private BroadcastHandler broadcastHandler;

	private List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();

	private CIArguments args;
	private CILogger logger;
	private long startTimeInMillis;
	private double timestep = 0;
	private double leftSpeed = 0;
	private double rightSpeed = 0;

	private CIBehavior activeBehavior = null;
	private ArrayList<Entity> entities = new ArrayList<Entity>();

	@Override
	public void begin(CIArguments args, CILogger logger) {
		this.startTimeInMillis = System.currentTimeMillis();
		this.args = args;
		this.logger = logger;

		addShutdownHooks();

		initIO();
		initMessageProviders();
		initConnections();

		messageHandler = new ControllerMessageHandler(this);
		messageHandler.start();

		setStatus("Running!\n");

		logger.logMessage(initMessages);
	}

	@Override
	public void run() {
		while (true) {

			long lastCycleTime = System.currentTimeMillis();
			CIBehavior current = activeBehavior;
			if (current != null) {
				current.step(timestep);
				if (current.getTerminateBehavior()) {
					stopActiveBehavior();
				}
				ioManager.setMotorSpeeds(leftSpeed, rightSpeed);
			}

			if (broadcastHandler != null)
				broadcastHandler.update(timestep);

			long timeToSleep = CYCLE_TIME
					- (System.currentTimeMillis() - lastCycleTime);

			if (timeToSleep > 0) {
				try {
					Thread.sleep(timeToSleep);
				} catch (InterruptedException e) {
				}
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
		logger.logMessage("# Shutting down Controller...");

		if (logger != null)
			logger.stopLogging();

		ioManager.shutdown();

		System.out.println("# Finished Controller cleanup!");
	}

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

			logger.logMessage(".");
			initMessages += "[INIT] ConnectionListener: ok\n";

			motorConnectionListener = new MotorConnectionListener(this);
			motorConnectionListener.start();
			
			behaviorConnectionListener = new BehaviorConnectionListener(this);
			behaviorConnectionListener.start();

			broadcastHandler = new RealBroadcastHandler(this);

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

	// Behaviors
	public void startBehavior(CIBehavior b) {
		stopActiveBehavior();
		activeBehavior = b;
	}

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

	public String getInitMessages() {
		return initMessages;
	}

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

	public String getStatus() {
		return status;
	}

	public IOManager getIOManager() {
		return ioManager;
	}

}