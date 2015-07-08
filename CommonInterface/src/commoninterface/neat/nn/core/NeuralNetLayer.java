/*
 * Created on 12-Sep-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package commoninterface.neat.nn.core;

import java.io.Serializable;


/**
 * @author msimmerson
 *
*/
public interface NeuralNetLayer extends Serializable {
	LayerOutput propogate(LayerInput input);
	NeuralNetLayerDescriptor layerDescriptor();
	Neuron[] layerNeurons();
}
