/*
 * Created on 25-Sep-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package commoninterface.neat.nn.core;

import commoninterface.neat.core.NEATNeuralNet;
import commoninterface.neat.core.control.NEAT;


/**
 * @author msimmerson
 *
 * Creates the required neural network
 */
public class NeuralNetFactory {
    private static final NeuralNetFactory factory = new NeuralNetFactory();

    private NeuralNetFactory() {
    }

    public static NeuralNetFactory getFactory() {
        return (factory);
    }
    /*
     * Takes a NeuralNetDescriptor to create the relevant type of nn
     */
    public NeuralNet createNN(NeuralNetDescriptor nnd) {
    	NeuralNetType type = nnd.neuralNetType();
    	NeuralNet net = null;
    	if (type instanceof NEAT) {
    		net = new NEATNeuralNet();
    	}
		net.createNetStructure(nnd);
        return (net);
    }
}
