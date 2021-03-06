/*
 * Created on 23-Jun-2005
 *
 * 
 */
package commoninterface.neat.core;

import java.util.ArrayList;

import commoninterface.neat.nn.core.ActivationFunction;
import commoninterface.neat.nn.core.Neuron;
import commoninterface.neat.nn.core.Synapse;

/**
 * @author MSimmerson
 *
 *         Specific NEAT neuron
 */
public class NEATNeuron implements Neuron {
	private static final long serialVersionUID = 4853092524890493125L;
	private double lastActivation;
	private double bias;
	private double[] weights;
	private ActivationFunction activationFunction;
	private int id;
	private int type;
	private int depth;
	private ArrayList<NEATNeuron> sourceNeurons;
	private ArrayList<Synapse> incomingSynapses;
	private boolean isInput = false;

	public NEATNeuron(ActivationFunction function, int id, int type) {
		this.activationFunction = function;
		this.id = id;
		this.type = type;
		this.sourceNeurons = new ArrayList<NEATNeuron>();
		this.incomingSynapses = new ArrayList<Synapse>();
		this.isInput = (type == NEATNodeGene.INPUT);
		this.depth = -1;
	}

	public void addSourceNeuron(NEATNeuron neuron) {
		this.sourceNeurons.add(neuron);
	}

	public void addIncomingSynapse(Synapse synapse) {
		this.incomingSynapses.add(synapse);
	}

	public ArrayList<Synapse> incomingSynapses() {
		return (this.incomingSynapses);
	}

	public ArrayList<NEATNeuron> sourceNeurons() {
		return (this.sourceNeurons);
	}

	@Override
	public double lastActivation() {
		return (this.lastActivation);
	}

	/**
	 * If it is an input neuron, returns the input, else will run through the
	 * specified activation function.
	 * 
	 */
	@Override
	public double activate(double[] nInputs) {
		double neuronIp = 0;
		int i = 0;
		double weight;
		double input;
		Synapse synapse;
		Object[] incoming = this.incomingSynapses.toArray();
		// acting as a bias neuron
		this.lastActivation = -1;

		if (!this.isInput) {
			if (nInputs.length > 0) {
				for (i = 0; i < nInputs.length; i++) {
					input = nInputs[i];
					synapse = (Synapse) incoming[i];
					if (synapse.isEnabled()) {
						weight = synapse.getWeight();
						neuronIp += (input * weight);
					}
				}
				neuronIp += (-1 * this.bias);
				this.lastActivation = this.activationFunction.activate(neuronIp);
			}
		} else {
			// neuronIp = nInputs[0];
			this.lastActivation = nInputs[0];
		}

		return (this.lastActivation);
	}

	@Override
	public ActivationFunction function() {
		return (this.activationFunction);
	}

	@Override
	public void modifyWeights(double[] weightMods, double[] momentum, boolean mode) {
		System.arraycopy(weightMods, 0, this.weights, 0, this.weights.length);

	}

	@Override
	public void modifyBias(double biasMod, double momentum, boolean mode) {
		this.bias = biasMod;
	}

	@Override
	public double[] weights() {
		return (this.weights);
	}

	@Override
	public double bias() {
		return (this.bias);
	}

	@Override
	public double[] lastWeightDeltas() {
		return null;
	}

	@Override
	public double lastBiasDelta() {
		return 0;
	}

	public int id() {
		return (this.id);
	}

	public int neuronType() {
		return (this.type);
	}

	public int neuronDepth() {
		return (this.depth);
	}

	public void setNeuronDepth(int depth) {
		this.depth = depth;
	}
}
