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
 *         Describes the structure of the NEAT network
 */
public class NEATNeuralNetDescriptor implements NeuralNetDescriptor {
	private static final long serialVersionUID = 5841418156581072968L;
	private ArrayList<NeuralNetLayerDescriptor> layerDescriptors = new ArrayList<NeuralNetLayerDescriptor>();
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

	@Override
	public void addLayerDescriptor(NeuralNetLayerDescriptor descriptor) {
		this.layerDescriptors.add(descriptor);
	}

	@Override
	public NeuralNetType neuralNetType() {
		return (netType);
	}

	@Override
	public int numInputs() {
		return (this.netInputs);
	}

	@Override
	public Collection<NeuralNetLayerDescriptor> layerDescriptors() {
		return (this.layerDescriptors);
	}

	@Override
	public Learnable learnable() {
		return (this.learnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetDescriptor#isRecurrent()
	 */
	@Override
	public boolean isRecurrent() {
		return (this.recurrent);
	}
}
