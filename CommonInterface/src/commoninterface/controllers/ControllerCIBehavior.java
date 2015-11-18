package commoninterface.controllers;

import java.util.ArrayList;

import commoninterface.CIBehavior;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.neuralnetwork.outputs.CINNOutput;
import commoninterface.utils.CIArguments;

public class ControllerCIBehavior extends CIBehavior {
	
	private CINeuralNetwork network;
	private ArrayList<String> inputsNames;
	private ArrayList<String> outputsNames;
	private ArrayList<Double[]> inputs;
	private ArrayList<Double[]> outputs;
	private CIArguments args;
	private String description = "";
	private boolean logInputsOutputs = false;
	
	public ControllerCIBehavior(CIArguments args, RobotCI robot) {
		super(args, robot);
		this.args = args;
	}
	
	@Override
	public void start() {
		inputsNames = new ArrayList<String>();
		outputsNames = new ArrayList<String>();
		if(logInputsOutputs) {
			inputs = new ArrayList<Double[]>();
			outputs = new ArrayList<Double[]>();
		}
		initSensors(new CIArguments(args.getArgumentAsString("sensors")));
		network = CINeuralNetwork.getNeuralNetwork(robot, new CIArguments(args.getArgumentAsString("network")));
		
		if(args.getArgumentAsString("description") != null) {
			description+=" "+args.getArgumentAsString("description");
		}
		
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
		
		Object[] entities = robot.getEntities().toArray();
		
		for(CISensor s : robot.getCISensors()) {
			s.update(timestep,entities);
		}
		
		network.controlStep(timestep);
		
		updateNetworkActivations(network);
		
	}
	
	@Override
	public void cleanUp() {
		robot.setMotorSpeeds(0, 0);
		if(network != null)
			network.reset();
	}
	
	public CINeuralNetwork getNeuralNetwork() {
		return network;
	}
	
	private synchronized void updateNetworkActivations(CINeuralNetwork network){
		if(inputsNames.isEmpty()){
			
			for (CISensor sensor : robot.getCISensors()) {
				for (int i = 0; i < sensor.getNumberOfSensors(); i++) {
					String inputName = sensor.getClass().getSimpleName().replace("Sensor","NNInput");
					inputsNames.add(inputName + "_" + i);
				}
			}
			
		}
		
		if(outputsNames.isEmpty()){
			for (CINNOutput output : network.getOutputs()) {
				for(int i=0; i < output.getNumberOfOutputValues(); i++)
					outputsNames.add(output.getClass().getSimpleName()+"_"+i);
			}
		}

		if(logInputsOutputs) {
			inputs.add(convertDoubleArray(network.getInputNeuronStates()));
			outputs.add(convertDoubleArray(network.getOutputNeuronStates()));
		}
		
	}
	
	/**
	 * Position 0 - List with the names of the inputs
	 * Position 1 - List with the names of the outputs
	 * Position 2 - Values of the states of the inputs since the last time that this method was called
	 * Position 3 - Values of the states of the outputs since the last time that this method was called
	 * 
	 */
	public synchronized ArrayList<?>[] getNeuralNetworkActivations(){
		ArrayList<?>[] activations = new ArrayList[4];
		
		activations[0] = inputsNames;
		activations[1] = outputsNames;
		activations[2] = (ArrayList<?>) inputs.clone();
		activations[3] = (ArrayList<?>) outputs.clone();
		
		inputs.clear();
		outputs.clear();
		
		return activations;
	}
	
	
	/**
	 * Convert double[] to Double[]
	 * 
	 */
	private Double[] convertDoubleArray(double[] array){
		Double[] convertedArray = new Double[array.length];
		
		for (int i = 0; i < array.length; i++) 
			convertedArray[i] = array[i];
		
		return convertedArray;
	}
	
	@Override
	public String toString() {
		return super.toString() + description;
	}
	
}