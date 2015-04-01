package simpletestbehaviors;

import commoninterface.CIBehavior;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.utils.CIArguments;

public class ControllerCIBehavior extends CIBehavior {
	
	private CINeuralNetwork network;
	private double currentTimeStep;
	
	public ControllerCIBehavior(CIArguments args, RobotCI robot) {
		super(args, robot);
		
		initSensors(new CIArguments(args.getArgumentAsString("sensors")));
		network = CINeuralNetwork.getNeuralNetwork(robot, new CIArguments(args.getArgumentAsString("network")));
	}
	
	protected void initSensors(CIArguments args) {
		
		robot.getCISensors().clear();
		
		for(int i = 0 ; i < args.getNumberOfArguments() ; i++) {
			CIArguments sensorArgs = new CIArguments(args.getArgumentAsString(args.getArgumentAt(i)));
			CISensor s = CISensor.getSensor(robot,sensorArgs.getArgumentAsString("classname"), sensorArgs);
			robot.getCISensors().add(s);
		}
	}
	
	@Override
	public void step(double timestep) {
			
		if(network == null || robot.getCISensors().isEmpty())
			return;
		
		//Sensors are updated here because other behaviors might
		//not need sensors. Ideally, this should be moved to the
		//main loop at the drone, but it would be heavier in terms
		//of processing.
		for(CISensor s : robot.getCISensors()) {
			s.update(timestep, robot.getEntities());
		}
		
		network.controlStep(timestep);
		currentTimeStep = timestep;
	}
	
	@Override
	public void cleanUp() {
		robot.setMotorSpeeds(0, 0);
		network.reset();
	}
	
	public CINeuralNetwork getNeuralNetwork() {
		return network;
	}
	
	public double getCurrentTimeStep() {
		return currentTimeStep;
	}
	
}