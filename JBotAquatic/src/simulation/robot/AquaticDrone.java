package simulation.robot;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	@ArgumentsAnnotation(name="commrange", defaultValue = "0.0")
	private double commRange = 0.0;
	
	private ArrayList<MessageProvider> messageProviders;
	private ArrayList<CIBehavior> alwaysActiveBehaviors = new ArrayList<CIBehavior>();
	private CIBehavior activeBehavior;
	
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
	
	public AquaticDrone(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.simulator = simulator;
		
		ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
		
		broadcastMessages.add(new HeartbeatBroadcastMessage(this));
		broadcastMessages.add(new PositionBroadcastMessage(this));
		broadcastMessages.add(new SharedDroneBroadcastMessage(this));
		
		broadcastHandler = new SimulatedBroadcastHandler(this, broadcastMessages);
		
		gpsError = args.getArgumentAsDoubleOrSetDefault("gpserror", gpsError);
		compassError = args.getArgumentAsDoubleOrSetDefault("compasserror", compassError);
		commRange = args.getArgumentAsDoubleOrSetDefault("commrange", commRange);
		
		if(commRange == 0)
			throw new RuntimeException("[AquaticDrone] CommRange is at 0!");
		
		if(args.getArgumentAsIntOrSetDefault("changewaypoint", 1) == 1)
			alwaysActiveBehaviors.add(new ChangeWaypointCIBehavior(new CIArguments(""), this));
		
		if(args.getArgumentAsIntOrSetDefault("avoiddrones", 1) == 1)
			alwaysActiveBehaviors.add(new AvoidDronesInstinct(new CIArguments(""), this));
		
		if(args.getArgumentAsIntOrSetDefault("kalman", 0) == 1) {
			kalmanFilterGPS = new RobotKalman();
			kalmanFilterCompass = new RobotKalman();
		}
		
		badGPS = args.getFlagIsTrue("badgps");
		
		sensors.add(new CompassSensor(simulator, sensors.size()+1, this, args));
		
		rudder = args.getFlagIsTrue("rudder");
		
		if(rudder) {
			rudderActuator = new RudderActuator(simulator, actuators.size()+1, args); 
			actuators.add(rudderActuator);
		} else {
			propellers = new PropellersActuator(simulator, actuators.size()+1, args);
			actuators.add(propellers);
		}
		
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
		
		updatePhysicalSensors();
		
		super.updateSensors(simulationStep, teleported);
		
		if(activeBehavior != null) {
			activeBehavior.step(simulationStep);
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
			rudderActuator.setSpeed((leftMotorPercentage+rightMotorPercentage)/2);
		}else {
			propellers.setLeftPercentage(leftMotorPercentage);
			propellers.setRightPercentage(rightMotorPercentage);
		}
	}
	
	public void setRudder(double heading, double speed) {
		rudderActuator.setHeading(heading);
		rudderActuator.setSpeed(speed);
	}
	
	/*
	@Override
	public void updateActuators(Double time, double timeDelta) {
		
		for(CIBehavior b : alwaysActiveBehaviors)
			b.step(time);
		
		if(stopTimestep > 0) {
			rightWheelSpeed = 0;
			leftWheelSpeed = 0;
			stopTimestep--;
		}
		
		double lw = Math.signum(rightWheelSpeed - leftWheelSpeed);
		
//		orientation = MathUtils.modPI2(orientation + motorModel(rightWheelSpeed-leftWheelSpeed)*lw);
		orientation = MathUtils.modPI2(orientation + motorModel(rightWheelSpeed-leftWheelSpeed)*lw);
		
		double accelDirection = (rightWheelSpeed+leftWheelSpeed) < 0 ? -1 : 1;
		
		double lengthOfAcc = accelarationConstant * (leftWheelSpeed + rightWheelSpeed);
		
		//Backwards motion should be slower. This value here is just an
		//estimate, and should be improved by taking real world samples
		if(accelDirection < 0)
			lengthOfAcc*=0.2;
		
		Vector2d accelaration = new Vector2d(lengthOfAcc * FastMath.cosQuick(orientation), lengthOfAcc * FastMath.sinQuick(orientation));
		
		velocity.setX(velocity.getX() * (1 - frictionConstant));
		velocity.setY(velocity.getY() * (1 - frictionConstant));    
		
		velocity.add(accelaration);
		
		position.set(
				position.getX() + timeDelta * velocity.getX(), 
				position.getY() + timeDelta * velocity.getY());
		
		for (Actuator actuator : actuators) {
			actuator.apply(this);
		}
		
		broadcastHandler.update(time);
	}*/
	
	
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
		//TODO REMOVE THIS DEBUG LINE!!
//		System.out.println("DEBUG DEBUG DEBUG AQUATICDRONE");
//		this.activeWaypoint = Waypoint.getWaypoints(this).get(getId());
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
		if(badGPS && simulator.getTime() % 10 != 0 && prevMeasuredLatLon != null) {
			return prevMeasuredLatLon;
		}
		
		LatLon gpsLatLon = CoordinateUtilities.cartesianToGPS(getPosition().getX(), getPosition().getY());
		
		if(gpsError > 0) {
			
			commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(gpsLatLon);
			double radius = simulator.getRandom().nextDouble()*gpsError;
			double angle = simulator.getRandom().nextDouble()*Math.PI*2;

			pos.setX(pos.getX()+radius*Math.cos(angle));
			pos.setY(pos.getY()+radius*Math.sin(angle));
			
			gpsLatLon = CoordinateUtilities.cartesianToGPS(pos);
		}
		
		return gpsLatLon;
	}
	
	private double updateCompassOrientation() {
		if(compassSensor == null)
			compassSensor = (CompassSensor) getSensorByType(CompassSensor.class);
		
		double heading = (360-(compassSensor.getSensorReading(0) * 360) + 90) % 360;
		double error = compassError*simulator.getRandom().nextDouble()*2-compassError;
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
}
