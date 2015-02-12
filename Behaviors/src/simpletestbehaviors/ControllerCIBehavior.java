package simpletestbehaviors;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;
import commoninterface.CISensor;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.utils.CIArguments;

public class ControllerCIBehavior extends CIBehavior {
	
	private CINeuralNetwork network;
	
	public ControllerCIBehavior(CIArguments args, AquaticDroneCI drone) {
		super(args, drone);
		
		initSensors(new CIArguments(args.getArgumentAsString("sensors")));
		network = CINeuralNetwork.getNeuralNetwork(drone, new CIArguments(args.getArgumentAsString("network")));
	}
	
	protected void initSensors(CIArguments args) {
		
		drone.getCISensors().clear();
		
		for(int i = 0 ; i < args.getNumberOfArguments() ; i++) {
			CIArguments sensorArgs = new CIArguments(args.getArgumentAsString(args.getArgumentAt(i)));
			CISensor s = CISensor.getSensor(drone,sensorArgs.getArgumentAsString("classname"), sensorArgs);
			drone.getCISensors().add(s);
		}
	}
	
	@Override
	public void step(double timestep) {
		
		if(network == null || drone.getCISensors().isEmpty())
			return;
		
		//Sensors are updated here because other behaviors might
		//not need sensors. Ideally, this should be moved to the
		//main loop at the drone, but it would be heavier in terms
		//of processing.
		for(CISensor s : drone.getCISensors()) {
			s.update(timestep, drone.getEntities());
		}
		
		network.controlStep(timestep);
	}
	
	@Override
	public void cleanUp() {
		drone.setMotorSpeeds(0, 0);
		network.reset();
	}
	
}