package simulation.robot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import org.joda.time.LocalDateTime;
import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import network.SimulatedBroadcastHandler;
import simpletestbehaviors.ChangeWaypointCIBehavior;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.actuator.PropellersActuator;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.CompassSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CISensor;
import commoninterface.dataobjects.GPSData;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.CompassMessage;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.EntityMessage;
import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.SystemStatusMessage;
import commoninterface.objects.Entity;
import commoninterface.objects.GeoFence;
import commoninterface.objects.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class AquaticDrone extends DifferentialDriveRobot implements AquaticDroneCI{

	private double frictionConstant = 0.21;//0.05
	private double accelarationConstant = 0.20;//0.1
	private Vector2d velocity = new Vector2d();
	private Simulator simulator;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();
	private PropellersActuator propellers;
	private SimulatedBroadcastHandler broadcastHandler;
	private Waypoint activeWaypoint;
	
	@ArgumentsAnnotation(name="gpserror", defaultValue = "0.0")
	private double gpsError = 0;
	
	@ArgumentsAnnotation(name="compasserror", defaultValue = "0.0")
	private double compassError = 0;
	
	@ArgumentsAnnotation(name="commrange", defaultValue = "0.0")
	private double commRange = 0.0;
	
	private ArrayList<CIBehavior> alwaysActiveBehaviors = new ArrayList<CIBehavior>();
	
	public AquaticDrone(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.simulator = simulator;
		
		ArrayList<BroadcastMessage> broadcastMessages = new ArrayList<BroadcastMessage>();
		broadcastMessages.add(new HeartbeatBroadcastMessage(this));
		broadcastMessages.add(new PositionBroadcastMessage(this));
		broadcastHandler = new SimulatedBroadcastHandler(this, broadcastMessages);
		
		gpsError = args.getArgumentAsDoubleOrSetDefault("gpserror", gpsError);
		compassError = args.getArgumentAsDoubleOrSetDefault("compasserror", compassError);
		commRange = args.getArgumentAsDoubleOrSetDefault("commrange", commRange);
		
		if(commRange == 0)
			throw new RuntimeException("[AquaticDrone] CommRange is at 0!");
		
		alwaysActiveBehaviors.add(new ChangeWaypointCIBehavior(new CIArguments(""), this));
		
		sensors.add(new CompassSensor(simulator, sensors.size()+1, this, args));
		actuators.add(new PropellersActuator(simulator, actuators.size()+1, args));
	}
	
	@Override
	public void shutdown() {}
	
	@Override
	public void setWheelSpeed(double left, double right) {
		super.setWheelSpeed(left, right);
	}
	
	@Override
	public void updateSensors(double simulationStep,ArrayList<PhysicalObject> teleported) {
		for(CIBehavior b : alwaysActiveBehaviors)
			b.step(simulationStep);
		super.updateSensors(simulationStep, teleported);
	}

	public void setMotorSpeeds(double leftMotorPercentage, double rightMotorPercentage) {
		if(propellers == null)
			propellers = (PropellersActuator) getActuatorByType(PropellersActuator.class);
		
		propellers.setLeftPercentage(leftMotorPercentage);
		propellers.setRightPercentage(rightMotorPercentage);
	}

	@Override
	public double getCompassOrientationInDegrees() {
		CompassSensor compassSensor = (CompassSensor) getSensorByType(CompassSensor.class);
		double heading = (360-(compassSensor.getSensorReading(0) * 360) + 90) % 360;
		double error = compassError*simulator.getRandom().nextDouble()*2-compassError;
		return heading+error;
	}

	@Override
	public LatLon getGPSLatLon() {
		
		LatLon latLon = CoordinateUtilities.cartesianToGPS(getPosition().getX(), getPosition().getY());
		
		if(gpsError > 0) {
			
			commoninterface.mathutils.Vector2d pos = CoordinateUtilities.GPSToCartesian(latLon);
			double radius = simulator.getRandom().nextDouble()*gpsError;
			double angle = simulator.getRandom().nextDouble()*Math.PI*2;

			pos.setX(pos.getX()+radius*Math.cos(angle));
			pos.setY(pos.getY()+radius*Math.sin(angle));
			
			return CoordinateUtilities.cartesianToGPS(pos);
		}
		
		return latLon;
	}
	
	@Override
	public double getGPSOrientationInDegrees() {
		return getCompassOrientationInDegrees();
	}

	@Override
	public double getTimeSinceStart() {
		return simulator.getTime()*10;
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
	
	private double motorModel(double d) {
		return 0.0048*Math.exp(2.4912*Math.abs(d*2)) - 0.0048;
	}
	
	@Override
	public void updateActuators(Double time, double timeDelta) {
		
		if(stopTimestep > 0) {
			rightWheelSpeed = 0;
			leftWheelSpeed = 0;
			stopTimestep--;
		}
		
		double lw = Math.signum(rightWheelSpeed-leftWheelSpeed);
		
		orientation = MathUtils.modPI2(orientation + motorModel(rightWheelSpeed-leftWheelSpeed)*lw);
		
//		System.out.println(leftWheelSpeed+" "+rightWheelSpeed+" "+Math.toDegrees(orientation));
		
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

	}
	
	public double getCommRange() {
		return commRange;
	}

	@Override
	public void begin(CIArguments args) {
		
	}

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
		return getId()+":"+getId()+":"+getId()+":"+getId();
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
		
		if(request instanceof InformationRequest && ((InformationRequest)request).getMessageTypeQuery() == InformationRequest.MessageType.NEURAL_ACTIVATIONS){
			//TODO
//			if (getActiveBehavior() instanceof ControllerCIBehavior)
//				response = createNeuralActivationMessage();
			
		}else{
			
			if(response == null && request instanceof InformationRequest
					&& ((InformationRequest) request).getMessageTypeQuery() == InformationRequest.MessageType.COMPASS) {
				response = new CompassMessage((int)getCompassOrientationInDegrees());
			} else if(response == null && request instanceof InformationRequest
					&& ((InformationRequest) request).getMessageTypeQuery() == InformationRequest.MessageType.GPS) {
				GPSData gps = new GPSData();
				LatLon latLon = getGPSLatLon();
				gps.setLatitudeDecimal(latLon.getLat());
				gps.setLongitudeDecimal(latLon.getLon());
				gps.setFix(true);
				gps.setDate(LocalDateTime.now());
				response = new GPSMessage(gps);
			} else if(response == null && request instanceof EntityMessage) {
				response = handleEntityMessage(request);
			} else if(response == null && request instanceof EntitiesMessage) {
				response = handleEntitiesMessage(request);
			} else if(response == null && request instanceof BehaviorMessage) {
				response = handleBehaviorMessage(request);
			}
			
			//TODO: Pass the FileLogger to the RobotCI
			//TODO
//			if(response == null && request instanceof LogMessage) {
//				LogMessage lm = (LogMessage)request;
//				
//				RobotLogger logger = robot.getLogger();
//				
//				if(logger != null)
//					logger.logMessage(lm.getLog());
//			}
		}
		
		if (response == null) {
			
			String sResponse = "No message provider for the current request (";
			
			if(request instanceof InformationRequest) {
				InformationRequest ir = (InformationRequest)request;
				sResponse+=ir.getMessageTypeQuery() + ")";
			} else {
				sResponse+=request.getClass().getSimpleName() + ")";
			}
			
			response = new SystemStatusMessage(sResponse);
		}
		
		conn.sendData(response);
	}
	
	private Message handleEntityMessage(Message m) {
		EntityMessage wm = (EntityMessage)m;
		Entity e = wm.getEntity();
		
		if(getEntities().contains(e)) {
			getEntities().remove(e);
		}
		
		if(e instanceof Waypoint) {
			ArrayList<Waypoint> wps = Waypoint.getWaypoints(this);
			
			if(wps.isEmpty())
				setActiveWaypoint((Waypoint)e);
		}
		
		getEntities().add(e);
		return m;
	}
	
	private Message handleEntitiesMessage(Message m) {
		EntitiesMessage wm = (EntitiesMessage)m;
		LinkedList<Entity> entities = wm.getEntities();
		
		Iterator<Entity> i = getEntities().iterator();
		
		while(i.hasNext()) {
			Entity e = i.next();
			if(e instanceof GeoFence)
				i.remove();
			if(e instanceof Waypoint)
				i.remove();
		}
		
		getEntities().addAll(entities);
		
		ArrayList<Waypoint> wps = Waypoint.getWaypoints(this);
		
		if(!wps.isEmpty())
			setActiveWaypoint(wps.get(0));
		
		return m;
	}
	
	private Message handleBehaviorMessage(Message m) {
		//TODO
		return null;
		/*BehaviorMessage bm = (BehaviorMessage)m;
		
     	 if(bm.getSelectedStatus()) {
     		try {
     			
     			ArrayList<Class<?>> classes = ClassLoadHelper.findRelatedClasses(bm.getSelectedBehavior());
     			
     			if(classes == null || classes.isEmpty()) {
     				return null;
     			}
     			
     			Class<CIBehavior> chosenClass = (Class<CIBehavior>)classes.get(0);
         		Constructor<CIBehavior> constructor = chosenClass.getConstructor(new Class[] { CIArguments.class, RobotCI.class});
				CIBehavior ctArgs = constructor.newInstance(new Object[] { new CIArguments(bm.getArguments()), this });
				startBehavior(ctArgs);
     		} catch(ReflectiveOperationException e) {
     			e.printStackTrace();
     		}
     	 } else {
     		 robot.stopActiveBehavior();
     	 }
     	 bm.setArguments("");
     	 return bm;*/
	}
	
	@Override
	public void reset() {
		leftWheelSpeed = 0;
		rightWheelSpeed = 0;
	}
}