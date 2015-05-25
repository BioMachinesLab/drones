package commoninterface.neuralnetwork;

import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

import commoninterface.AquaticDroneCI;
import commoninterface.CIFactory;
import commoninterface.RobotCI;
import commoninterface.neuralnetwork.inputs.CINNInput;
import commoninterface.neuralnetwork.outputs.CINNOutput;
import commoninterface.utils.CIArguments;

public abstract class CINeuralNetwork implements Serializable{
	protected Vector<CINNInput> inputs;
	protected Vector<CINNOutput> outputs;

	protected boolean weightNoiseEnabled  = false;
	protected double  weightNoiseAmount   = 0.0;
	protected Random  weightNoiseRandom   = null;
	
	protected double[] weights;

	protected double[] inputNeuronStates;
	protected double[] outputNeuronStates;

	protected int numberOfInputNeurons    = 0;
	protected int numberOfOutputNeurons   = 0;
	protected int genomeLength = -1;
	
	public boolean printValues = false;
	
	public void create(Vector<CINNInput> inputs, Vector<CINNOutput> outputs) {
		this.inputs = inputs;
		this.outputs = outputs;

		for (CINNInput i : inputs) {
			numberOfInputNeurons += i.getNumberOfInputValues();
		}

		for (CINNOutput i : outputs) {
			numberOfOutputNeurons += i.getNumberOfOutputValues();
		}

		inputNeuronStates  = new double[numberOfInputNeurons];
		outputNeuronStates = new double[numberOfOutputNeurons];
	}


	public int getNumberOfInputNeurons() {
		return numberOfInputNeurons;
	}

	public int getNumberOfOutputNeurons() {
		return numberOfOutputNeurons;
	}

	public void setWeights(double[] weights) {
		if (weights.length != genomeLength) {
			throw new IllegalArgumentException("Found " + weights.length + " weights, but need " + genomeLength + " for " + this.getClass() + " with " + numberOfInputNeurons + " input neurons and " + numberOfOutputNeurons + " output neurons.\nNNInputs: " + inputs + "\nOutputs: " + outputs);
		}
		
		this.weights = weights.clone();

		if (weightNoiseEnabled) {
			for (int i = 0; i < this.weights.length; i++)
				this.weights[i] += weightNoiseAmount * (2.0 * (weightNoiseRandom.nextDouble() - 0.5));
		}
	}

	public void controlStep(double time) {
		int currentInputValue = 0;
		//		boolean difZero=false;
		for (CINNInput i : inputs) {
			for (int j = 0; j < i.getNumberOfInputValues(); j++) {
				inputNeuronStates[currentInputValue++] = i.getValue(j);				
				//				if (i.getValue(j)!=0)
				//					difZero=true;
			}
		}

		//		if (!difZero){
		//			System.out.println("ERROR - all zeros in the inputs");
		//		}
		outputNeuronStates = propagateInputs(inputNeuronStates);
		int currentOutputValue = 0;
		
		for (CINNOutput o : outputs) {
			for (int j = 0; j < o.getNumberOfOutputValues(); j++) {
				o.setValue(j, outputNeuronStates[currentOutputValue++]);				
			}
			o.apply();
		}
	}

	protected abstract double[] propagateInputs(double[] inputValues);

	public void setRequiredNumberOfWeights(int numberOfWeights) {
		this.genomeLength = numberOfWeights;		
	}

	public int getGenomeLength() {
		return genomeLength;
	}

	public void enableWeightNoise(double noiseAmount, Random random) {
		weightNoiseAmount  = noiseAmount;
		weightNoiseEnabled = true;
		weightNoiseRandom  = random;
	}
	
	public Vector<CINNInput> getInputs() {
		return inputs;
	}
	
	public Vector<CINNOutput> getOutputs() {
		return outputs;
	}
	
	public double[] getInputNeuronStates() {
		return inputNeuronStates;
	}
	
	public double[] getOutputNeuronStates() {
		return outputNeuronStates;
	}
	
	public void setInputs(Vector<CINNInput> inputs) {
		this.inputs = inputs;
	}
	
	public double[] getWeights() {
		return weights;
	}
	
	public abstract void reset();
	
	public static CINeuralNetwork getNeuralNetwork(RobotCI robot, CIArguments arguments) {
		
		Vector<CINNInput> inputs = CINNInput.getNNInputs(robot, arguments);
		Vector<CINNOutput> outputs = CINNOutput.getNNOutputs(robot, arguments);
		
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Neural Network 'classname' not defined: "+ arguments.toString());
		
		CINeuralNetwork network = (CINeuralNetwork)CIFactory.getInstance(arguments.getArgumentAsString("classname"),inputs,outputs,arguments); 

		if(arguments.getArgumentIsDefined("weights")) {
			String[] rawArray = arguments.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			network.setWeights(weights);
		}
		
		return network;
	}
}