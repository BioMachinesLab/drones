/*
 * Created on 23-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core;

import java.util.Collection;

import commoninterface.neat.core.control.NEAT;
import commoninterface.neat.ga.core.Chromosome;
import commoninterface.neat.nn.core.Learnable;
import commoninterface.neat.nn.core.NeuralNetDescriptor;
import commoninterface.neat.nn.core.NeuralNetLayerDescriptor;
import commoninterface.neat.nn.core.NeuralNetType;

/**
 * @author MSimmerson
 *
 * Descriptor for a NEAT neural net.
 */
public class NEATNetDescriptor implements NeuralNetDescriptor {
	private int inputSize;
	private Learnable learnable;
	private Chromosome structure;
	
	public NEATNetDescriptor(int inputSize, Learnable learnable) {
		this.inputSize = inputSize;
		this.learnable = learnable;
	}
	
	public void updateStructure(Chromosome structure) {
		this.structure = structure;
	}
	
	
	public Chromosome neatStructure() {
		return (this.structure);
	}
	
	public void addLayerDescriptor(NeuralNetLayerDescriptor descriptor) {
		throw new UnsupportedOperationException("addLayerDescriptor not used in NEAT");
	}

	public NeuralNetType neuralNetType() {
		return (new NEAT());
	}

	public int numInputs() {
		return (this.inputSize);
	}

	public Collection layerDescriptors() {
		throw new UnsupportedOperationException("layerDescriptors not used in NEAT");
	}

	public Learnable learnable() {
		return (this.learnable);
	}

	public boolean isRecurrent() {
		return false;
	}
}
