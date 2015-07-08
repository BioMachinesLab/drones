package commoninterface.neat.ga.core;

import commoninterface.neat.data.core.NetworkDataSet;
import commoninterface.neat.nn.core.NeuralNet;

/**
 * @author MSimmerson
 *
 */
public abstract class NeuralFitnessFunction implements FitnessFunction {
	private NeuralNet net;
	private NetworkDataSet evalDataSet;
	
	public NeuralFitnessFunction(NeuralNet net, NetworkDataSet dataSet) {
		this.evalDataSet = dataSet;
		this.net = net;
	}
	
	public NeuralNet net() {
		return (this.net);
	}
	
	public NetworkDataSet evaluationData() {
		return (this.evalDataSet);
	}
}
