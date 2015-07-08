package commoninterface.neat.nn.core;

import java.io.Serializable;


/**
 * @author msimmerson
 *
 */
public interface Neuron extends Serializable {
	public double lastActivation();
	public double activate(double[] nInputs);
	public ActivationFunction function();
	public void modifyWeights(double[] weightMods, double[] momentum, boolean mode);
	public void modifyBias(double biasMod, double momentum, boolean mode);
	public double[] weights();
	public double bias();
	public double[] lastWeightDeltas();
	public double lastBiasDelta();
}
