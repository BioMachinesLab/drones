package commoninterface.neat.nn.core;

import java.io.Serializable;

/**
 * @author msimmerson
 *
 */
public interface NeuralNetLayerDescriptor extends Serializable {
	public int layerSize();
    public int layerId();
    public int inputsIntoLayer();
    public ActivationFunction activationFunction();
    public boolean isOutputLayer();
    public boolean nodesSelfRecurrent();
}
