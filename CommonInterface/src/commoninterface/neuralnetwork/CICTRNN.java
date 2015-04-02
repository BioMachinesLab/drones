package commoninterface.neuralnetwork;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import commoninterface.neuralnetwork.inputs.CINNInput;
import commoninterface.neuralnetwork.outputs.CINNOutput;
import commoninterface.utils.CIArguments;

public class CICTRNN extends CINeuralNetwork {
	protected double    timeStep = 0.2;
    protected double    tau      = 2.5;
    
	protected int       numberOfHiddenNodes;
	protected double[]  hiddenDeltaStates;
	protected double[]  hiddenStates;
	protected double[]  inputToHiddenWeights;
	protected double[]  hiddenBiases;
	protected double[]  hiddenToHiddenWeights;
	protected double[]  hiddenTaus;
	protected double[]  hiddenToOutputWeights;
	protected double[]  outputBiases;

	public CICTRNN(Vector<CINNInput> inputs, Vector<CINNOutput> outputs, CIArguments arguments) {
		numberOfHiddenNodes = arguments.getArgumentAsIntOrSetDefault("hiddennodes",5);
		create(inputs, outputs, numberOfHiddenNodes);
	}
			
	@Override
	protected double[] propagateInputs(double[] inputValues) {		
		
		// Update delta state of hidden layer from inputs:
	    for(int i = 0; i < numberOfHiddenNodes; i++) {
	        hiddenDeltaStates[i] = -hiddenStates[i];
	        
	        for (int j = 0; j < numberOfInputNeurons; j++) {
	            // weight * sigmoid(state)
	            hiddenDeltaStates[i] += inputToHiddenWeights[i * numberOfInputNeurons + j] * inputValues[j];
	        }	  
	    }

	    // Update delta state from hidden layer, self-recurrent connections:
	    for (int i = 0; i < numberOfHiddenNodes; i++)
	    {
	        for (int j = 0; j < numberOfHiddenNodes; j++) {
	            double z              = 1.0/(Math.exp(-(hiddenStates[j] + hiddenBiases[j])) + 1.0 );
	            hiddenDeltaStates[i] += hiddenToHiddenWeights[i * numberOfHiddenNodes + j] * z;
	        }
	    }
	    
	    for(int  i = 0; i < numberOfHiddenNodes; i++) 
	    {
	        hiddenStates[i] += hiddenDeltaStates[i] * timeStep/hiddenTaus[i];
	    }

	    // Update the outputs layer::
	    for (int i = 0; i < numberOfOutputNeurons; i++)
	    {
	        for (int j = 0; j < numberOfHiddenNodes; j++)
	        {
	            double z = ((1.0)/(Math.exp(-( hiddenStates[j] + hiddenBiases[j])) + 1.0 ));
	            outputNeuronStates[i] += hiddenToOutputWeights[i * numberOfHiddenNodes + j] * z;
	        }

	        // Compute the activation function immediately, since this is
	        // what we return and since the output layer is not recurrent:
	        outputNeuronStates[i] = ((1.0)/(Math.exp(-(outputNeuronStates[i] + outputBiases[i])) + 1.0 ));
	    }
	    
//	    String inputs = "";
//	    String outputs = "";
//	    for(double d : inputValues)
//	    	inputs+=d+" ";
//    	for(double d : outputNeuronStates)
//    		outputs+=d+" ";
	    
//    	System.out.println("Inputs: " + inputs);
//    	System.out.println("Outputs: " + outputs);
    	
	    if(printValues) {
	    	PrintWriter out;
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter("out.txt", true)));
			
		        String s = "";
		        
		    	for(double d : inputValues)
		    		s+=d+" ";
		    	for(double d : outputNeuronStates)
		    		s+=d+" ";
		    	
		    	out.println(s);
		    	out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    return outputNeuronStates;
	}

	public void create(Vector<CINNInput> inputs, Vector<CINNOutput> outputs, int numberOfHiddenNeurons) {
		super.create(inputs, outputs);
		this.numberOfHiddenNodes = numberOfHiddenNeurons;

		int A = numberOfInputNeurons;
	    int B = numberOfHiddenNeurons;
	    int C = numberOfOutputNeurons;

	    setRequiredNumberOfWeights((A+1) * B + B*B + (B+1) * C + B);
	}
		
	@Override
	public void reset() {
		
		if(printValues) {
	    	PrintWriter out;
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter("out.txt", true)));
		    	out.println("");
		    	out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
		if (weights != null) {
	    	int A = numberOfInputNeurons;
	    	int B = numberOfHiddenNodes;
	    	int C = numberOfOutputNeurons;

	    	inputToHiddenWeights  = new double[A * B];
	    	hiddenToHiddenWeights = new double[B * B];
	    	hiddenBiases          = new double[B];

	    	hiddenTaus            = new double[B];

	    	hiddenToOutputWeights = new double[B * C];
	    	outputBiases          = new double[C];


	    	int chromosomePosition = 0;
	    	for (int i = 0; i < A * B; i++) {
	    		inputToHiddenWeights[i] = weights[chromosomePosition++];
	    	}

	    	for (int i = 0; i < B * B; i++) {
	    		hiddenToHiddenWeights[i] = weights[chromosomePosition++];
	    	}

	    	for (int i = 0; i < B; i++) {
	    		hiddenBiases[i] = weights[chromosomePosition++];
	    	}

	    	for (int i = 0; i < B; i++) {
	    		hiddenTaus[i] = Math.pow(10, (-1.0 + (tau * (weights[chromosomePosition++] + 10.0) / 20)));
	    	}

	    	for (int i = 0; i < B * C; i++) {
	    		hiddenToOutputWeights[i] = weights[chromosomePosition++];
	    	}

	    	for (int i = 0; i < C; i++)
	    	{
	    		outputBiases[i] = weights[chromosomePosition++];
	    	}

	    	hiddenDeltaStates     = new double[B];
	    	hiddenStates          = new double[B];

	    	for (int i = 0; i < B; i++) {
	    		hiddenStates[i] = 0;
	    	}

	    }
	}
	
	public double[] getHiddenStates() {
		return hiddenStates;
	}
	
	public double[] getHiddenTaus() {
		return hiddenTaus;
	}

	public void setWeights(double[] weights) {
		super.setWeights(weights);
		reset();
	}
}
