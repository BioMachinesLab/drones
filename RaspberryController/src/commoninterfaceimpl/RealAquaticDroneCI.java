package commoninterfaceimpl;

import io.IOManager;
import io.SystemStatusMessageProvider;
import io.input.ControllerInput;
import io.output.ControllerOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import network.broadcast.RealBroadcastHandler;
import simpletestbehaviors.ChangeWaypointCIBehavior;
import utils.FileLogger;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CISensor;
import commoninterface.LedState;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.dataobjects.GPSData;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.instincts.AvoidDronesInstinct;
import commoninterface.instincts.AvoidObstaclesInstinct;
import commoninterface.messageproviders.BehaviorMessageProvider;
import commoninterface.messageproviders.EntitiesMessageProvider;
import commoninterface.messageproviders.EntityMessageProvider;
import commoninterface.messageproviders.LogMessageProvider;
import commoninterface.messageproviders.NeuralActivationsMessageProvider;
import commoninterface.network.CommandConnectionListener;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.ConnectionListener;
import commoninterface.network.ControllerMessageHandler;
import commoninterface.network.MotorConnectionListener;
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import commoninterface.network.broadcast.SharedDroneBroadcastMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.RobotKalman;
import commoninterface.utils.RobotLogger;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class RealAquaticDroneCI extends Thread implements AquaticDroneCI {

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

	private boolean run = true;
	private long startTimeInMillis;
	private double timestep = 0;
	private double leftSpeed = 0;
	private double rightSpeed = 0;
	private double behaviorTimestep = 0;
	
	private CIBehavior activeBehavior = null;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	
	private Waypoint activeWaypoint;
	
	private DroneType droneType = DroneType.DRONE;
	
	private ArrayList<CIBehavior> alwaysActiveBehaviors = new ArrayList<CIBehavior>(); 
	
	private RobotLogger logger;
	
	private double currentOrientation = 0;
	private double currentGPSOrientation = 0;
	private LatLon currentLatLon = null;
	private LatLon prevMeasuredLatLon = null;
	
	private RobotKalman kalmanFilterGPS;
	private RobotKalman kalmanFilterCompass;
	private LatLon origin = CoordinateUtilities.cartesianToGPS(0,0);
	
	private double rudderHeading = 0;
	private double rudderSpeed = 0;

	@Override
	public void begin(HashMap<String,CIArguments> args) {
		this.startTimeInMillis = System.currentTimeMillis();

		addShutdownHooks();

		initIO(args.get("--io"));
		initMessageProviders();
		initConnections();
		
		configureArguments(args.get("--general"));
		
		messageHandler = new ControllerMessageHandler(this);
		messageHandler.start();
		
		setStatus("Running!\n");

		log(LogCodex.encodeLog(LogType.MESSAGE, initMessages));
	}

	@Override
	public void run() {
		while (run) {
			
			updateSensors();
			
			long lastCycleTime = System.currentTimeMillis();
			CIBehavior current = activeBehavior;
			try {
				
				if (current != null) {
					
					current.step(behaviorTimestep++);
					
					for(CIBehavior b : alwaysActiveBehaviors)
						b.step(timestep);
					
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
		log(LogCodex.encodeLog(LogType.MESSAGE, "Shutting down Controller..."));

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
	private void initIO(CIArguments args) {
		ioManager = new IOManager(this, args);
		initMessages += ioManager.getInitMessages();
	}

	private void initConnections() {
		try {
			
			System.out.println("Starting connections...");

			connectionListener = new ConnectionListener(this);
			connectionListener.start();

			initMessages += "[INIT] ConnectionListener: ok\n";

			motorConnectionListener = new MotorConnectionListener(this);
			motorConnectionListener.start();
			
			commandConnectionListener = new CommandConnectionListener(this);
			commandConnectionListener.start();

			ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
			
			broadcastMessages.add(new HeartbeatBroadcastMessage(this));
			broadcastMessages.add(new PositionBroadcastMessage(this));
			broadcastMessages.add(new SharedDroneBroadcastMessage(this));
					
			broadcastHandler = new RealBroadcastHandler(this, broadcastMessages);

			initMessages += "[INIT] MotorConnectionListener: ok\n";
			
			log(LogCodex
					.encodeLog(LogType.MESSAGE, "IP " + getNetworkAddress()));
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
		
		messageProviders.add(new EntityMessageProvider(this));
		System.out.println("\tEntityMessageProvider");
		messageProviders.add(new EntitiesMessageProvider(this));
		System.out.println("\tEntitiesMessageProvider");
		messageProviders.add(new BehaviorMessageProvider(this));
		System.out.println("\tBehaviorMessageProvider");
		messageProviders.add(new NeuralActivationsMessageProvider(this));
		System.out.println("\tNeuralActivationsMessageProvider");
		messageProviders.add(new LogMessageProvider(this));
		System.out.println("\tLogMessageProvider");
	}

	// Behaviors
	@Override
	public void startBehavior(CIBehavior b) {
		stopActiveBehavior();
		activeBehavior = b;
		behaviorTimestep = 0;
		activeBehavior.start();
		
		String str= "Starting CIBehavior "+activeBehavior.toString();
		log(LogCodex.encodeLog(LogType.MESSAGE, str));
	}

	@Override
	public void stopActiveBehavior() {
		if (activeBehavior != null) {
			activeBehavior.cleanUp();
			
			String str = "Stopping CIBehavior "
					+ activeBehavior.toString();
			log(LogCodex.encodeLog(LogType.MESSAGE, str));
			
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
		return currentOrientation;
	}

	@Override
	public LatLon getGPSLatLon() {
		return currentLatLon;
	}

	@Override
	public double getGPSOrientationInDegrees() {
		return currentGPSOrientation;
	}
	
	private void updateSensors() {
		
		LatLon measuredLatLon = updateGPS();
		double measuredCompass = updateCompass();
		
		if(kalmanFilterGPS != null) {
			if(measuredLatLon != null) {
				//timestep < 100 so that the filtered position converges to the real position 
				if(timestep < 100 || prevMeasuredLatLon == null || prevMeasuredLatLon.getLat() != measuredLatLon.getLat() || prevMeasuredLatLon.getLon() != measuredLatLon.getLon()) {
					RobotLocation rl = kalmanFilterGPS.getEstimation(measuredLatLon, currentOrientation);
					prevMeasuredLatLon = measuredLatLon;
					currentLatLon = rl.getLatLon();
				}
			}
		} else {
			currentLatLon = measuredLatLon;
		}
		
		if(kalmanFilterCompass != null) {
			
			if(measuredCompass != -1) {
				RobotLocation rl = kalmanFilterCompass.getEstimation(origin, measuredCompass);
				currentOrientation = rl.getOrientation();
			}
		} else {
			currentOrientation = measuredCompass;
		}
	}
	
	private LatLon updateGPS() {
		try {
			
			GPSData gpsData = ioManager.getGpsModule().getReadings();
			
			currentGPSOrientation = gpsData.getOrientation();
			
			if (gpsData.getLatitudeDecimal() == 0
					|| gpsData.getLongitudeDecimal() == 0)
				return null;
			else
				return new LatLon(gpsData.getLatitudeDecimal(),gpsData.getLongitudeDecimal());
		} catch (Exception e) {
			currentOrientation = -1;
			return null;
		}
	}
	
	private double updateCompass() {
		try {
			double orientation = ioManager.getCompassModule()
					.getHeadingInDegrees();
			return (orientation % 360.0);
		} catch (Exception e) {
			return -1;
		}

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
	
	public void log(String msg) {
		if(logger != null)
			logger.logMessage(msg);
	}
	
	@Override
	public RobotLogger getLogger() {
		return logger;
	}
	
	@Override
	public DroneType getDroneType() {
		return droneType;
	}
	
	@Override
	public void setDroneType(DroneType droneType) {
		this.droneType = droneType;
	}
	
	private void configureArguments(CIArguments args) {
		
		if(args.getArgumentIsDefined("dronetype"))
			setProperty("dronetype", args.getArgumentAsString("dronetype"));
		
		if(args.getFlagIsTrue("filelogger"))
			this.startLogger();
		
		if(args.getArgumentIsDefined("compassoffset") && ioManager.getCompassModule() != null)
			ioManager.getCompassModule().setOffset(args.getArgumentAsInt("compassoffset"));
		
		if(args.getArgumentIsDefined("changewaypoint"))
			setProperty("changewaypoint", args.getArgumentAsString("changewaypoint"));
		
		if(args.getArgumentIsDefined("avoiddrones"))
			setProperty("avoiddrones", args.getArgumentAsString("avoiddrones"));
		
		if(args.getArgumentIsDefined("avoidobstacles"))
			setProperty("avoidobstacles", args.getArgumentAsString("avoidobstacles"));
		
		if(args.getArgumentIsDefined("kalmanfilter"))
			setProperty("kalmanfilter", args.getArgumentAsString("kalmanfilter"));
				
	}
	
	@Override
	public double getLeftMotorSpeed() {
		return leftSpeed;
	}
	
	@Override
	public double getRightMotorSpeed() {
		return rightSpeed;
	}

	@Override
	public void replaceEntity(Entity e) {
		synchronized(entities){
			entities.remove(e);
			entities.add(e);
		}
	}
	
	@Override
	public void setRudder(double heading, double speed) {
		
		this.rudderHeading = heading;
		this.rudderSpeed = speed;
		
		double angleInDegrees = getTurningAngleFromHeading(heading)*-1;
		double motorDifference = 0;
		double forwardComponent = 0;
		double turningComponent = 0;
		double turningSpeed = 0;
		
		double lw = 0;
		double rw = 0;
		
		if(Math.abs(heading) >= 0.9/* || Math.abs(heading) < 0.1*/) {
			
			if(Math.abs(heading) >= 0.9)
				heading = 1.0*Math.signum(heading);
			
			if(Math.abs(heading) <= 0.1)
				heading = 0;
			
			angleInDegrees = getTurningAngleFromHeading(heading)*-1;
			motorDifference = getMotorDifferenceFromTurningAngle(Math.abs(angleInDegrees));
			
			forwardComponent = 1.0 - motorDifference;
			turningComponent = 1.0 - forwardComponent;
			
			turningComponent*=speed;
			forwardComponent*=speed;
			
			if(heading > 0) {
				lw = turningComponent;
			} else if(heading < 0) {
				rw = turningComponent;
			} else {
				lw = forwardComponent;
				rw = forwardComponent;
			}
			
			angleInDegrees = getTurningAngleFromTurningSpeed(turningSpeed)*Math.signum(angleInDegrees);
			
		} else {
			
			motorDifference = getMotorDifferenceFromAngleOneFullMotor(Math.abs(angleInDegrees));
			
			if(heading > 0) {
				lw = 1;
				rw = 1 - motorDifference;
			} else if(heading < 0) {
				lw = 1 - motorDifference;
				rw = 1;
			}
			
			lw*=speed;
			rw*=speed;
		}
		
		if(speed < 0.01) {
			lw = 0;
			rw = 0;
		}
		
		setMotorSpeeds(lw, rw);
	}
	
	private double getMotorDifferenceFromAngleOneFullMotor(double angle) {
		return -0.0068 * Math.pow(angle,2) + 0.1614*angle+ 0.0903;
	}
	
	public double getTurningAngleFromDifferenceOneMotorFull(double speedDifference) {
		return 10.564 * speedDifference - 2.0412;
	}
	
	public double getTurningSpeedFromMotorDifferenceOneMotorFull(double difference) {
		return (-28.958*difference + 139.88) / 100.0;
	}
	
	private double getMotorDifferenceFromTurningAngle(double angle) {
		return Math.min(0.0098*Math.pow(angle,2) + 0.0244*angle,1);
	}
	
	private double getTurningAngleFromTurningSpeed(double turningSpeed) {
		return 7.6401 * turningSpeed;
	}
	
	private double getTurningAngleFromHeading(double heading) {
		return 9*heading;
	}
	
	public double getRudderHeading() {
		return rudderHeading;
	}
	
	public double getRudderSpeed() {
		return rudderSpeed;
	}
	
	@Override
	public double getMotorSpeedsInPercentage() {
		return (getLeftMotorSpeed()+getRightMotorSpeed())/2.0;
	}
	
	@Override
	public void setProperty(String name, String value) {
		if(name.equals("changewaypoint")) {
			boolean found = findBehavior(ChangeWaypointCIBehavior.class, alwaysActiveBehaviors.iterator(), value.equals("0"));
			if(value.equals("1") && !found)
				alwaysActiveBehaviors.add(new ChangeWaypointCIBehavior(new CIArguments(""), this));
		}
		
		if(name.equals("avoiddrones")) {
			boolean found = findBehavior(AvoidDronesInstinct.class, alwaysActiveBehaviors.iterator(), value.equals("0"));
			if(value.equals("1") && !found)
				alwaysActiveBehaviors.add(new AvoidDronesInstinct(new CIArguments(""), this));
		}
		
		if(name.equals("avoidobstacles")) {
			boolean found = findBehavior(AvoidObstaclesInstinct.class, alwaysActiveBehaviors.iterator(), value.equals("0"));
			if(value.equals("1") && !found)
				alwaysActiveBehaviors.add(new AvoidObstaclesInstinct(new CIArguments(""), this));
		}
		
		if(name.equals("kalmanfilter")) {
			if(value.equals("1")) {
				kalmanFilterGPS = new RobotKalman();
				kalmanFilterCompass = new RobotKalman();
			} else {
				kalmanFilterGPS = null;
				kalmanFilterCompass = null;
			}
		}
		
		if(name.equals("dronetype"))
			droneType = DroneType.valueOf(value);
	}
	
	private boolean findBehavior(Class<?> c, Iterator<?> i, boolean remove) {
		boolean found = false;
		
		while(i.hasNext()) {
			Object current = i.next();
			if(c.isInstance(current)) {
				found = true;
				if(remove)
					i.remove();
				break;
			}
		}
		return found;
	}

}
