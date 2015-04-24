package commoninterfaceimpl;

import io.SystemStatusMessageProvider;
import io.ThymioIOManager;
import io.input.ControllerInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.broadcast.RealBroadcastHandler;
import commoninterface.CIBehavior;
import commoninterface.CILogger;
import commoninterface.CISensor;
import commoninterface.RealRobotCI;
import commoninterface.ThymioCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.CommandConnectionListener;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.ConnectionListener;
import commoninterface.network.ControllerMessageHandler;
import commoninterface.network.MotorConnectionListener;
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.objects.Entity;
import commoninterface.utils.CIArguments;

public class RealThymioCI extends RealRobotCI implements ThymioCI {

	private static long CYCLE_TIME = 100;// in miliseconds

	private String status = "";
	private String initMessages = "\n";
	private ThymioIOManager ioManager;
	private ControllerMessageHandler messageHandler;

	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	private CommandConnectionListener commandConnectionListener;
	private BroadcastHandler broadcastHandler;
	
	private List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();

	private CIArguments args;
	private long startTimeInMillis;
	private double timestep = 0;
	private double behaviorTimeStep = 0;
	private double leftSpeed = 0;
	private double rightSpeed = 0;

	private boolean startBehavior;
	
	private CIBehavior activeBehavior = null;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	
	private Vector2d virtualPosition;
	private Double virtualOrientation;
	
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

		setStatus("Running!\n");

		logger.logMessage(initMessages);
	}

	@Override
	public void run() {
		while (true) {

			long lastCycleTime = System.currentTimeMillis();
			CIBehavior current = activeBehavior;
			if (current != null) {
				if(startBehavior)
					behaviorTimeStep = 0;
				
				current.step(behaviorTimeStep);
				
				if(startBehavior)
					startBehavior = false;
				
				if (current.getTerminateBehavior()) {
					stopActiveBehavior();
				}
			}

			ioManager.setMotorSpeeds(leftSpeed, rightSpeed);
			
			if (broadcastHandler != null)
				broadcastHandler.update(timestep);

			long timeToSleep = CYCLE_TIME - (System.currentTimeMillis() - lastCycleTime);

			if (timeToSleep > 0) {
				try {
					Thread.sleep(timeToSleep);
				} catch (InterruptedException e) {
				}
			}

			timestep++;
			behaviorTimeStep++;
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
		ioManager.stopThymio();

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
	
	public void processInformationRequest(Message request, ConnectionHandler conn) {
		messageHandler.addMessage(request, conn);
	}
	
	// Init's
	private void initIO() {
		ioManager = new ThymioIOManager();
		initMessages += ioManager.getInitMessages();
	}
	
	private void initMessageProviders() {
		System.out.println("Creating Message Providers:");
		
		messageProviders.add(new SystemStatusMessageProvider(this));
		System.out.println("\tSystemStatusMessageProvider");

		for (ControllerInput i : ioManager.getInputs()) {
			if (i instanceof MessageProvider) {
				messageProviders.add((MessageProvider) i);
				System.out.println("\t"+i.getClass().getSimpleName());
			}
		}

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
			
			commandConnectionListener = new CommandConnectionListener(this);
			commandConnectionListener.start();
			
			ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
			broadcastMessages.add(new HeartbeatBroadcastMessage(this));
			broadcastHandler = new RealBroadcastHandler(this,broadcastMessages);

			logger.logMessage(".");
			initMessages += "[INIT] MotorConnectionListener: ok\n";

		} catch (IOException e) {
			initMessages += "[INIT] Unable to start Network Connection Listeners! ("
					+ e.getMessage() + ")\n";
		}
	}
	
	// Behaviors
	public void startBehavior(CIBehavior b) {
		stopActiveBehavior();
		activeBehavior = b;
		startBehavior = true;
	}

	public void stopActiveBehavior() {
		if (activeBehavior != null) {
			activeBehavior.cleanUp();
			activeBehavior = null;
			ioManager.setMotorSpeeds(0, 0);
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
	
	public String getInitMessages() {
		return initMessages;
	}

	public List<MessageProvider> getMessageProviders() {
		return messageProviders;
	}

	@Override
	public List<Short> getInfraredSensorsReadings() {
		return (List<Short>) ioManager.getProximitySensorsReadings();
	}

	@Override
	public double getTimeSinceStart() {
		long elapsedMillis = System.currentTimeMillis() - this.startTimeInMillis;
		return ((double) elapsedMillis) / 1000.0;
	}

	@Override
	public String getStatus() {
		return status;
	}

	public ThymioIOManager getIOManager() {
		return ioManager;
	}

	@Override
	public Vector2d getVirtualPosition() {
		return virtualPosition;
	}
	
	@Override
	public void setVirtualPosition(double x, double y) {
		if(virtualPosition == null)
			virtualPosition = new Vector2d(x, y);
		else
			virtualPosition.set(x, y);
	}
	
	@Override
	public Double getVirtualOrientation() {
		return virtualOrientation;
	}

	@Override
	public void setVirtualOrientation(double orientation) {
		virtualOrientation = orientation;
	}

	@Override
	public double getThymioRadius() {
		return 0.08;
	}
	
}
