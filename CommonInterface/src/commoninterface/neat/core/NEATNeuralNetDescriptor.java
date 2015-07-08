/*
 * Created on Sep 29, 2004
 *
 */
package commoninterface.neat.core;

import java.util.ArrayList;
import java.util.Collection;

import commoninterface.neat.core.control.NEAT;
import commoninterface.neat.nn.core.Learnable;
import commoninterface.neat.nn.core.NeuralNetDescriptor;
import commoninterface.neat.nn.core.NeuralNetLayerDescriptor;
import commoninterface.neat.nn.core.NeuralNetType;

/**
 * @author MSimmerson
 *
 * Describes the structure of the NEAT network
 */
public class NEATNeuralNetDescriptor implements NeuralNetDescriptor {
	private ArrayList layerDescriptors = new ArrayList();
	private static final NEAT netType = new NEAT();
	private int netInputs;
	private Learnable learnable;
	private boolean recurrent;

	public NEATNeuralNetDescriptor(int netInputs, Learnable learnable) {
		this(netInputs, learnable, false);
	}
	
	public NEATNeuralNetDescriptor(int netInputs, Learnable learnable, boolean recurrent) {
		this.netInputs = netInputs;
		this.learnable = learnable;
		this.recurrent = recurrent;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetDescriptor#addLayerDescriptor(org.neat4j.ailibrary.nn.core.NeuralNetLayerDescriptor)
	 */
	public void addLayerDescriptor(NeuralNetLayerDescriptor descriptor) {
		this.layerDescriptors.add(descriptor);
	}

	/**
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetDescriptor#neuralNetType()
	 */
	public NeuralNetType neuralNetType() {
		return (netType);
	}

	/**
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetDescriptor#numInputs()
	 */
	public int numInputs() {
		return (this.netInputs);
	}

	/**
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetDescriptor#layerDescriptors()
	 */
	public Collection layerDescriptors() {
		return (this.layerDescriptors);
	}

	/**
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetLayerDescriptor#learnable()
	 */
	public Learnable learnable() {
		return (this.learnable);
	}

	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetDescriptor#isRecurrent()
	 */
	public boolean isRecurrent() {
		return (this.recurrent);
	}
}
