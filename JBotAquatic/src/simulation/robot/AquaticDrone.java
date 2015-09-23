package simulation.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import network.SimulatedBroadcastHandler;
import network.messageproviders.CompassMessageProvider;
import network.messageproviders.GPSMessageProvider;
import simpletestbehaviors.ChangeWaypointCIBehavior;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.actuator.PropellersActuator;
import simulation.robot.actuator.RudderActuator;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.CompassSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CISensor;
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
import commoninterface.network.ConnectionHandler;
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
import controllers.DummyController;

public class AquaticDrone extends DifferentialDriveRobot implements AquaticDroneCI{

	private Simulator simulator;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();
	private PropellersActuator propellers;
	private RudderActuator rudderActuator;
	private SimulatedBroadcastHandler broadcastHandler;
	private Waypoint activeWaypoint;
	
	@ArgumentsAnnotation(name="gpserror", defaultValue = "0.0")
	private double gpsError = 0;
	
	@ArgumentsAnnotation(name="compasserror", defaultValue = "0.0")
	private double compassError = 0;
	
	@ArgumentsAnnotation(name="compassoffset", defaultValue = "0.0")
	private double compassOffset = 0;
	
	@ArgumentsAnnotation(name="commrange", defaultValue = "0.0")
	private double commRange = 0.0;
	
	private ArrayList<MessageProvider> messageProviders;
	private ArrayList<CIBehavior> alwaysActiveBehaviors = new ArrayList<CIBehavior>();
	private CIBehavior activeBehavior;
	private double activeBehaviorStep = 0;
	
	private DroneType droneType = DroneType.DRONE;
	
	private RobotLogger logger;
	
	private double leftPercentage = 0;
	private double rightPercentage = 0;
	
	private RobotKalman kalmanFilterGPS;
	private RobotKalman kalmanFilterCompass;
	
	private LatLon gpsLatLon;
	private LatLon prevMeasuredLatLon;
	private double compassOrientation;
	private CompassSensor compassSensor;
	private LatLon origin = CoordinateUtilities.cartesianToGPS(0,0);
	private boolean badGPS = false;
	
	private boolean rudder = false;
	
	private double headingOffset = 0;
	private double speedOffset = 0;
	private boolean configuredSensors = false;
	private Arguments args;
	
	public AquaticDrone(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.simulator = simulator;
		
		this.args = args;
		
		ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
		
		broadcastMessages.add(new HeartbeatBroadcastMessage(this));
		broadcastMessages.add(new PositionBroadcastMessage(this));
		broadcastMessages.add(new SharedDroneBroadcastMessage(this));
		
		broadcastHandler = new SimulatedBroadcastHandler(this, broadcastMessages);
		
		gpsError = args.getArgumentAsDoubleOrSetDefault("gpserror", gpsError);
		
		compassError = args.getArgumentAsDoubleOrSetDefault("compasserror", compassError);
		compassOffset = args.getArgumentAsDoubleOrSetDefault("compassoffset", compassOffset);
		commRange = args.getArgumentAsDoubleOrSetDefault("commrange", commRange);
		
		if(compassOffset > 0) {
			double error = simulator.getRandom().nextDouble();
			error = error*2 - 1;
			compassOffset = compassOffset*error;
		}
		
		if(commRange == 0)
			throw new RuntimeException("[AquaticDrone] CommRange is at 0!");
		
		
		if(args.getArgumentIsDefined("changewaypoint"))
			setProperty("changewaypoint", args.getArgumentAsString("changewaypoint"));
		
		if(args.getArgumentIsDefined("avoiddrones"))
			setProperty("avoiddrones", args.getArgumentAsString("avoiddrones"));
		
		if(args.getArgumentIsDefined("kalmanfilter"))
			setProperty("kalmanfilter", args.getArgumentAsString("kalmanfilter"));
		
		headingOffset = args.getArgumentAsDoubleOrSetDefault("headingoffset",headingOffset);
		
		if(headingOffset > 0) {
			double error = simulator.getRandom().nextDouble()*2-1;
			headingOffset*=error;
		}
		
		speedOffset = args.getArgumentAsDoubleOrSetDefault("speedoffset",speedOffset);
		
		if(speedOffset > 0) {
			double error = simulator.getRandom().nextDouble()*2-1;
			speedOffset*=error;
		}
		
		badGPS = args.getFlagIsTrue("badgps");
		
		if(args.getArgumentIsDefined("rudder"))
			setProperty("rudder", args.getArgumentAsString("rudder"));
		
		rudder = args.getFlagIsTrue("rudder");
		
		log(LogCodex.encodeLog(LogType.MESSAGE, "IP " + getNetworkAddress()));
	}
	
	@Override
	public void shutdown() {}
	
	@Override
	public void setWheelSpeed(double left, double right) {
		super.setWheelSpeed(left, right);
	}
	
	@Override
	public void updateSensors(double simulationStep, ArrayList<PhysicalObject> teleported) {
		
		if(!configuredSensors) {
			sensors.add(new CompassSensor(simulator, sensors.size()+1, this, new Arguments("")));
			configuredSensors = true;
		}
		
		updatePhysicalSensors();
		
		super.updateSensors(simulationStep, teleported);
		
		if(activeBehavior != null) {
			activeBehavior.step(activeBehaviorStep++);
		} else if(getController() instanceof DummyController){
			setMotorSpeeds(0, 0);
		}
		
	}

	@Override
	public double getCompassOrientationInDegrees() {
		return compassOrientation;
	}

	@Override
	public LatLon getGPSLatLon() {
		return gpsLatLon;
	}
	
	@Override
	public double getGPSOrientationInDegrees() {
		return getCompassOrientationInDegrees();
	}

	@Override
	public double getTimeSinceStart() {
		return ((double)simulator.getTime())/10.0;
	}
	
	@Override
	public void setLed(int index, commoninterface.LedState state) {
		LedState robotState;
		
		switch(state) {
			case BLINKING:
				robotState = LedState.BLINKING;
				break;
			case OFF:
				robotState = LedState.OFF;
				break;
			case ON:
				robotState = LedState.ON;
				break;
			default:
				robotState = LedState.OFF;
		}
		
		setLedState(robotState);
	}
	
	public void setMotorSpeeds(double leftMotorPercentage, double rightMotorPercentage) {
		leftPercentage = leftMotorPercentage;
		rightPercentage = rightMotorPercentage;
		
		if(rudder) {
			double speed = (leftMotorPercentage+rightMotorPercentage)/2;
			rudderActuator.setSpeed(speed);
		}else {
			propellers.setLeftPercentage(leftMotorPercentage);
			propellers.setRightPercentage(rightMotorPercentage);
		}
	}
	
	public void setRudder(double heading, double speed) {
		
		double h = heading+headingOffset;
		h = Math.max(-1, h);
		h = Math.min(1,h);
		
		double s = speed;
		
		if(speed > 0) {
			s = speed*(1.0-speedOffset);
			s = Math.max(0,s);
			s = Math.min(1,s);
		}
		
		rudderActuator.setHeading(h);
		rudderActuator.setSpeed(s);
	}
	
	@Override
	public void updateActuators(Double time, double timeDelta) {
		
		
		for(CIBehavior b : alwaysActiveBehaviors) {
			b.step(time);
		}
		
		 if(!rudder) {
			propellers.move(this,leftWheelSpeed,rightWheelSpeed,timeDelta);
		}
		
		for (Actuator actuator : actuators) {
			actuator.apply(this);
			//rudder updates the robot position here
		}
		
		if(stopTimestep > 0) {
			rightWheelSpeed = 0;
			leftWheelSpeed = 0;
			stopTimestep--;
		}
		
		broadcastHandler.update(time);

	}
	
	public double getCommRange() {
		return commRange;
	}

	@Override
	public void begin(HashMap<String,CIArguments> args) {}

	@Override
	public ArrayList<Entity> getEntities() {
		return entities;
	}
	
	@Override
	public ArrayList<CISensor> getCISensors() {
		return cisensors;
	}
	
	@Override
	public String getNetworkAddress() {
		return getId()+"."+getId()+"."+getId()+"."+(getId()+100);
	}
	
	@Override
	public BroadcastHandler getBroadcastHandler() {
		return broadcastHandler;
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	@Override
	public Waypoint getActiveWaypoint() {
		return activeWaypoint;
	}
	
	@Override
	public void setActiveWaypoint(Waypoint wp) {
		this.activeWaypoint = wp;
	}
	
	@Override
	public String getInitMessages() {
		return "Simulated drone with ID "+getId();
	}
	
	@Override
	public void processInformationRequest(Message request, ConnectionHandler conn) {
		Message response = null;
		
		for (MessageProvider p : getMessageProviders()) {
			response = p.getMessage(request);
			
			if (response != null)
				break;
		}
		
		if(conn != null && response != null) {
			conn.sendData(response);
		}
	}
	
	@Override
	public void reset() {
		leftWheelSpeed = 0;
		rightWheelSpeed = 0;
	}

	@Override
	public RobotLogger getLogger() {
		return logger;
	}
	
	@Override
	public List<MessageProvider> getMessageProviders() {
		
		//We only do this here because messageProviders might not be necessary
		//most of the times, and it saves simulation time
		if(messageProviders == null) {
			initMessageProviders();
		}
		
		return messageProviders;
	}
	
	private void initMessageProviders() {
		messageProviders = new ArrayList<MessageProvider>();
		
		messageProviders.add(new CompassMessageProvider(this));
		messageProviders.add(new GPSMessageProvider(this));
		messageProviders.add(new EntityMessageProvider(this));
		messageProviders.add(new EntitiesMessageProvider(this));
		messageProviders.add(new BehaviorMessageProvider(this));
		messageProviders.add(new NeuralActivationsMessageProvider(this));
		messageProviders.add(new LogMessageProvider(this));
	}
	
	@Override
	public String getStatus() {
		if(getActiveBehavior() != null)
			return "Running behavior "+getActiveBehavior().getClass().getSimpleName();
		return "Idle";
	}
	
	@Override
	public void startBehavior(CIBehavior b) {
		stopActiveBehavior();
		activeBehavior = b;
		activeBehavior.start();
		activeBehaviorStep = 0;
		
		String str = "Starting CIBehavior " + b.getClass().getSimpleName();
		log(LogCodex.encodeLog(LogType.MESSAGE, str));
	}

	@Override
	public void stopActiveBehavior() {
		if (activeBehavior != null) {
			activeBehavior.cleanUp();
			
			String str = "Stopping CIBehavior "
					+ activeBehavior.getClass().getSimpleName();
			log(LogCodex.encodeLog(LogType.MESSAGE, str));
			
			activeBehavior = null;
		}
		setMotorSpeeds(0, 0);
	}
	
	@Override
	public CIBehavior getActiveBehavior() {
		return activeBehavior;
	}
	
	private void log(String msg) {
		if(logger != null)
			logger.logMessage(msg);
	}
	
	@Override
	public DroneType getDroneType() {
		return droneType;
	}
	
	@Override
	public void setDroneType(DroneType droneType) {
		this.droneType = droneType;
		
	}
	
	@Override
	public double getLeftMotorSpeed() {
		return leftPercentage;
	}
	
	@Override
	public double getRightMotorSpeed() {
		return rightPercentage;
	}
	
	@Override
	public void replaceEntity(Entity e) {
		synchronized(entities){
			entities.remove(e);
			entities.add(e);
		}
	}
	
	private LatLon updateGPSPosition() {
		if(badGPS && simulator.getTime() % 2 != 0 && prevMeasuredLatLon != null) {
			return prevMeasuredLatLon;
		}
		
		LatLon gpsLatLon = CoordinateUtilities.cartesianToGPS(getPosition().getX(), getPosition().getY());
		
		if(gpsError > 0) {
			
			commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(gpsLatLon);
			double radius = simulator.getRandom().nextDouble()*gpsError;
			double angle = simulator.getRandom().nextDouble()*Math.PI*2;
			
			if(radius > 0) {
				pos.setX(pos.getX()+radius*Math.cos(angle));
				pos.setY(pos.getY()+radius*Math.sin(angle));
			}
			
			gpsLatLon = CoordinateUtilities.cartesianToGPS(pos);
		}
		
		return gpsLatLon;
	}
	
	private double updateCompassOrientation() {
		if(compassSensor == null)
			compassSensor = (CompassSensor) getSensorByType(CompassSensor.class);
		
		double heading = (360-(compassSensor.getSensorReading(0) * 360) + 90) % 360;
		double error = compassError*simulator.getRandom().nextDouble()*2-compassError;
		error+=compassOffset;
		return heading+error;
	}
	
	private void updatePhysicalSensors() {
		
		LatLon measuredLatLon = updateGPSPosition();
		double measuredCompass = updateCompassOrientation();
		
		if(kalmanFilterGPS != null) {
			if(measuredLatLon != null) {
				if(prevMeasuredLatLon == null || prevMeasuredLatLon.getLat() != measuredLatLon.getLat() || prevMeasuredLatLon.getLon() != measuredLatLon.getLon()) {
					RobotLocation rl = kalmanFilterGPS.getEstimation(measuredLatLon, compassOrientation);
					gpsLatLon = rl.getLatLon();
				}
			}
		} else {
			gpsLatLon = measuredLatLon;
		}
		
		prevMeasuredLatLon = measuredLatLon;
		
		if(kalmanFilterCompass != null) {
			
			if(measuredCompass != -1) {
				RobotLocation rl = kalmanFilterCompass.getEstimation(origin, measuredCompass);
				compassOrientation = rl.getOrientation();
			}
		} else {
			compassOrientation = measuredCompass;
		}
	}
	
	@Override
	public double getMotorSpeedsInPercentage() {
		if(rudder)
			return rudderActuator.getSpeed();
		else
			return (leftPercentage+rightPercentage) / 2.0;
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
		
		if(name.equals("rudder")) {
			
			findBehavior(RudderActuator.class, actuators.iterator(), true);
			findBehavior(PropellersActuator.class, actuators.iterator(), true);
			rudderActuator = null;
			propellers = null;
			
			rudder = value.equals("1");
			if(rudder) {
				rudderActuator = new RudderActuator(simulator, actuators.size()+1, args); 
				actuators.add(rudderActuator);
			} else {
				propellers = new PropellersActuator(simulator, actuators.size()+1, args);
				actuators.add(propellers);
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
