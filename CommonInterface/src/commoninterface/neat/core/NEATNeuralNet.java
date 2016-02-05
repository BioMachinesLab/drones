/*
 * Created on 22-Jun-2005
 *
 * 
 */
package commoninterface.neat.core;

import java.util.ArrayList;
import java.util.Collection;

import commoninterface.neat.data.core.NetworkInput;
import commoninterface.neat.data.core.NetworkOutputSet;
import commoninterface.neat.ga.core.Chromosome;
import commoninterface.neat.ga.core.Gene;
import commoninterface.neat.nn.core.ActivationFunction;
import commoninterface.neat.nn.core.NeuralNet;
import commoninterface.neat.nn.core.NeuralNetDescriptor;
import commoninterface.neat.nn.core.NeuralNetLayer;
import commoninterface.neat.nn.core.Neuron;
import commoninterface.neat.nn.core.Synapse;
import commoninterface.neat.nn.core.functions.LinearFunction;
import commoninterface.neat.nn.core.functions.SigmoidFunction;
import commoninterface.neat.nn.core.functions.TanhFunction;

/**
 * @author MSimmerson
 *
 * The NEAT Neural Network
 */
public class NEATNeuralNet implements NeuralNet {
    
        private static final long serialVersionUID = -1L;
	private NEATNetDescriptor descriptor;
	private Synapse[] connections;
	private NEATNeuron[] neurons;
	private int level = 0;
	//private int depthLevel = 0;
	
	public NEATNeuron[] neurons() {
		return (this.neurons);
	}
        
        public Synapse[] connections() {
            return connections;
        }
	
	/**
	 * Exercises the network for the given input data set
	 */
	@Override
	public NetworkOutputSet execute(NetworkInput netInput) {
		NEATNetOutputSet opSet;
		double[] outputs;
		this.level = 0;
		int i;
		// trawl through the graph bacwards from each output node
		Object[] outputNeurons = this.outputNeurons().toArray();
		if (outputNeurons.length == 0) {
			System.err.println("No output neurons");
		}
		outputs = new double[outputNeurons.length];
		
		for (i = 0; i < outputs.length; i++) {
			outputs[i] = this.neuronOutput((NEATNeuron)outputNeurons[i], netInput);
		}
		
		opSet = new NEATNetOutputSet();
		opSet.addNetworkOutput(new NEATNetOutput(outputs));
		return (opSet);
	}
	
	public ArrayList outputNeurons() {
		ArrayList outputNeurons = new ArrayList();
		int i;
		
		for (i = 0; i < this.neurons.length; i++) {
			if (this.neurons[i].neuronType() == NEATNodeGene.OUTPUT) {
				outputNeurons.add(this.neurons[i]);
			}
		}
		return (outputNeurons);
	}
	
	private double neuronOutput(NEATNeuron neuron, NetworkInput netInput) {
		double output = 0;
		double[] inputPattern;
		// find its inputs
		Object[] sourceNodes = neuron.sourceNeurons().toArray();
		int i;
		
		this.level++;
		if (neuron.neuronType() == NEATNodeGene.INPUT) {
			inputPattern = new double[1];
			// match the input column to the input node, id's start from 1
			inputPattern[0] = netInput.pattern()[neuron.id() - 1];
		} else {
			inputPattern = new double[sourceNodes.length];
			for (i = 0; i < sourceNodes.length; i++) {
				if (neuron.id() == ((NEATNeuron)sourceNodes[i]).id()) {				
					// Self Recurrent
					//cat.debug("Self Recurrent:" + neuron.id() + ":" + ((NEATNeuron)sourceNodes.get(i)).id());
					inputPattern[i] = neuron.lastActivation();
				} else if (neuron.neuronDepth() > ((NEATNeuron)sourceNodes[i]).neuronDepth()) {
					// Recurrent
					//cat.debug("Recurrent:" + neuron.id() + ":" + ((NEATNeuron)sourceNodes.get(i)).id());
					inputPattern[i] = ((NEATNeuron)sourceNodes[i]).lastActivation();
				} else {
					inputPattern[i] = this.neuronOutput((NEATNeuron)sourceNodes[i], netInput);
//					if (((Synapse)incomingSynapses[i]).isEnabled()) {
//						inputPattern[i] = this.neuronOutput((NEATNeuron)sourceNodes[i], netInput);
//					} else {
//						cat.info("Stop recursion");
//					}
				}
			}
		}
		output = neuron.activate(inputPattern);
		this.level--;
		return (output);
	}

	/**
	 * Generates a neural network structure based on the network descriptor
	 *
	 */
	public void updateNetStructure() {
		// use descriptor's chromo to create net 
		Chromosome netStructure = this.descriptor.neatStructure();
		ArrayList nodes = new ArrayList();
		ArrayList links = new ArrayList();
		Gene[] genes = netStructure.genes();
		int i;
		
		for (i = 0; i < netStructure.size(); i++) {
			if (genes[i] instanceof NEATNodeGene) {					
				nodes.add(genes[i]);
			} else if (genes[i] instanceof NEATLinkGene) {	
				if (((NEATLinkGene)genes[i]).isEnabled()) {
					// only add enabled links to the net structure
					links.add(genes[i]);
				}
			}
		}
		
		this.connections = this.createLinks(links, this.createNeurons(nodes));
		this.assignNeuronDepth(this.outputNeurons(), 0);
	}
	
	private void assignNeuronDepth(ArrayList neurons, int depth) {
		int i;
		NEATNeuron neuron;
		
		for (i = 0; i < neurons.size(); i++) {
			neuron = (NEATNeuron)neurons.get(i);
			if (neuron.neuronType() == NEATNodeGene.OUTPUT) {
				if (neuron.neuronDepth() == -1) {
					neuron.setNeuronDepth(depth);
					this.assignNeuronDepth(neuron.sourceNeurons(), depth + 1);
				}
			} else if (neuron.neuronType() == NEATNodeGene.HIDDEN) {
				if (neuron.neuronDepth() == -1) {
					neuron.setNeuronDepth(depth);
					this.assignNeuronDepth(neuron.sourceNeurons(), depth + 1);				
				}
			} else if (neuron.neuronType() == NEATNodeGene.INPUT) {
				neuron.setNeuronDepth(Integer.MAX_VALUE);
			}
		}
	}
	
	private NEATNeuron[] createNeurons(ArrayList nodes) {
		this.neurons = new NEATNeuron[nodes.size()];
		NEATNodeGene gene;
		int i;
		
		for (i = 0; i < neurons.length; i++) {
			gene = (NEATNodeGene)nodes.get(i);
			this.neurons[i] = new NEATNeuron(this.createActivationFunction(gene), gene.id(), gene.getType());
			this.neurons[i].modifyBias(gene.bias(), 0, true);
		}

		return (neurons);
	}
	
	private ActivationFunction createActivationFunction(NEATNodeGene gene) {
		ActivationFunction function = null;
		// inputs are passed through
		if (gene.getType() == NEATNodeGene.INPUT) {
			function = new LinearFunction();
		} else if (gene.getType() == NEATNodeGene.OUTPUT){
			function = new SigmoidFunction(gene.sigmoidFactor());
		} else {
			function = new TanhFunction();
		}
		
		return (function);		
	}
	
	private Synapse[] createLinks(ArrayList links, NEATNeuron[] neurons) {
		NEATLinkGene gene;
		Synapse[] synapses = new Synapse[links.size()];
		int i;
		NEATNeuron from;
		NEATNeuron to;
		
		for (i = 0; i < links.size(); i++) {
			gene = (NEATLinkGene)links.get(i);
			from = this.findNeuronById(neurons, gene.getFromId());
			to = this.findNeuronById(neurons, gene.getToId());
			to.addSourceNeuron(from);
			synapses[i] = new Synapse(from, to, gene.getWeight());
			synapses[i].setEnabled(gene.isEnabled());
			to.addIncomingSynapse(synapses[i]);
		}
		
		return (synapses);
	}
	
	private NEATNeuron findNeuronById(NEATNeuron[] neurons, int id) {
		boolean found = false;
		NEATNeuron neuron = null;
		int i = 0;
		
		while (!found) {
			if (neurons[i].id() == id) {
				neuron = neurons[i];
				found = true;
			} else {
				i++;
			}
		}
		
		return (neuron);
	}
	
	/**
	 * Updates the internal network structure
	 */
	@Override
	public void createNetStructure(NeuralNetDescriptor descriptor) {
		this.descriptor = (NEATNetDescriptor)descriptor;
	}

	@Override
	public NeuralNetDescriptor netDescriptor() {
		return (this.descriptor);
	}

	@Override
	public Collection hiddenLayers() {
		return null;
	}

	@Override
	public NeuralNetLayer outputLayer() {
		return null;
	}

	@Override
	public void seedNet(double[] weights) {
	}

	@Override
	public int requiredWeightCount() {
		return 0;
	}

	@Override
	public int netID() {
		return 0;
	}

	@Override
	public Neuron neuronAt(int x, int y) {
		return null;
	}
}
