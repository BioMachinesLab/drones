package commoninterface.neat.nn.core;

import java.io.Serializable;
import java.util.Collection;


/**
 * @author msimmerson
 *
 */
public interface NeuralNetDescriptor extends Serializable
{
	public void addLayerDescriptor(NeuralNetLayerDescriptor descriptor);
	public NeuralNetType neuralNetType();
	public int numInputs();
	public Collection layerDescriptors();
    public Learnable learnable();
    public boolean isRecurrent();
}
