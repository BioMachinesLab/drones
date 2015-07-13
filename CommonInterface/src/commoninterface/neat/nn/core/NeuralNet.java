package commoninterface.neat.nn.core;

import java.io.Serializable;
import java.util.Collection;

import commoninterface.neat.data.core.NetworkInput;
import commoninterface.neat.data.core.NetworkOutputSet;



/**
 * @author msimmerson
 *
 */
public interface NeuralNet extends Serializable
{
	public void createNetStructure(NeuralNetDescriptor descriptor);
	public NeuralNetDescriptor netDescriptor();
	public Collection hiddenLayers();
	public NeuralNetLayer outputLayer();
	public void seedNet(double[] weights);
	public int requiredWeightCount();
	public int netID();
	public NetworkOutputSet execute(NetworkInput netInput);
	public Neuron neuronAt(int x, int y);
}
