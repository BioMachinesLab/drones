package controllers;

import java.awt.Color;
import mathutils.Vector2d;

import controllers.Controller;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.sensors.PositionSensor;
import simulation.util.Arguments;
import simulation.robot.sensors.WallRaySensor;
import simulation.robot.actuator.PropellersActuator;
import simulation.robot.actuators.TwoWheelActuator;



public class TaskAllocationController extends Controller {



	private PositionSensor positionSensor;
	private WallRaySensor WRSensor;
	private DifferentialDriveRobot r;
	private PropellersActuator props;
	private TwoWheelActuator wheels;
	private int numberOfRobots;
	private double width;
	private double height;
	private Simulator sim;
	
	public TaskAllocationController(Simulator simulator, Robot robot,
			Arguments args) {
		super(simulator, robot, args);
		// TODO Auto-generated constructor stub
		numberOfRobots = args.getArgumentAsIntOrSetDefault("numberofrobot", 1);
		width = args.getArgumentAsDoubleOrSetDefault("width", 45);
		height = args.getArgumentAsDoubleOrSetDefault("height", 45);
		r = (DifferentialDriveRobot)robot;
		sim = simulator;
	}
	
	private void randomOrientation(){
		r.setOrientation(sim.getRandom().nextDouble()*Math.PI*2);
		currentState = State.AREA_ALLOCATION;
	}

	public enum State{
		AREA_ALLOCATION, RECRUITING, EXPLORING, RECRUITED, CHANGE_ORIENTATION
	}	
	private State currentState = State.AREA_ALLOCATION;	
	
	@Override
	public void controlStep(double time){
		
		positionSensor = (PositionSensor)robot.getSensorByType(PositionSensor.class);
		WRSensor = (WallRaySensor)robot.getSensorByType(WallRaySensor.class);
		props = (PropellersActuator)robot.getActuatorByType(PropellersActuator.class);
		wheels = (TwoWheelActuator)robot.getActuatorByType(TwoWheelActuator.class);
		
		switch(currentState){
		
		case AREA_ALLOCATION:
			//explore();
			allocateArea();
			break;
			
		case CHANGE_ORIENTATION:
			randomOrientation();
		
		case EXPLORING:
			r.setBodyColor(Color.YELLOW);
			explore();
			break;
			
		case RECRUITING:
			break;
			
		case RECRUITED:
			break;
		}
		
	}
	
	private void allocateArea() {
		// TODO Auto-generated method stub
		double areaToMonitor = (width*height)/numberOfRobots;
		System.out.println(areaToMonitor);
		boolean flag = false;
		r.setWheelSpeed(0.3, 0.3);
		if(flag == false && r.getPosition().x > (width/2) || r.getPosition().x < -(width/2) || r.getPosition().y > (height/2) || r.getPosition().y < -(height/2)){
			currentState = State.CHANGE_ORIENTATION;
		}
	}

	private void explore(){
		Vector2d currentPosition;
		r.setWheelSpeed(0.5, 0.5);
		currentPosition = r.getPosition();
		System.out.println(currentPosition.x);
	}

}
