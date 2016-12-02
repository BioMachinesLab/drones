/*
 * Created on 22-Jun-2005
 *
 */
package commoninterface.neat.core;

import commoninterface.neat.data.core.NetworkDataSet;
import commoninterface.neat.ga.core.Chromosome;
import commoninterface.neat.ga.core.NeuralFitnessFunction;
import commoninterface.neat.nn.core.NeuralNet;

/**
 * Provides common behavior for all NEAT Fitness functions which should all extend this class 
 * @author MSimmerson
 *
 */
public abstract class NEATFitnessFunction extends NeuralFitnessFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2938850031735637112L;

	/**
	 * @param net
	 * @param dataSet
	 */
	public NEATFitnessFunction(NeuralNet net, NetworkDataSet dataSet) {
		super(net, dataSet);
	}
	
	public void createNetFromChromo(Chromosome genoType) {
		((NEATNetDescriptor)this.net().netDescriptor()).updateStructure(genoType);
		((NEATNeuralNet)this.net()).updateNetStructure();
	}

	@Override
	public int requiredChromosomeSize() {
		return 0;
	}
}
