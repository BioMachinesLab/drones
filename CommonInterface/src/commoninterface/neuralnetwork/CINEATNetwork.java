/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commoninterface.neuralnetwork;

import commoninterface.neat.core.NEATChromosome;
import commoninterface.neat.core.NEATLinkGene;
import commoninterface.neat.core.NEATNetDescriptor;
import commoninterface.neat.core.NEATNeuralNet;
import commoninterface.neat.core.NEATNodeGene;
import commoninterface.neat.data.core.NetworkInput;
import commoninterface.neat.data.core.NetworkOutputSet;
import commoninterface.neat.data.csv.CSVInput;
import commoninterface.neat.ga.core.Gene;
import commoninterface.neuralnetwork.inputs.CINNInput;
import commoninterface.neuralnetwork.outputs.CINNOutput;
import commoninterface.utils.CIArguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author jorge
 */
public class CINEATNetwork extends CINeuralNetwork {

    public static final double NODE = 0d, LINK = 1d;
    private NEATNeuralNet network;

    public CINEATNetwork(Vector<CINNInput> inputs, Vector<CINNOutput> outputs, CIArguments arguments) {
        create(inputs, outputs);
        if (arguments.getArgumentIsDefined("weights")) {
            String net = arguments.getArgumentAsString("weights");
            network = deserialize(net);
        }
    }

    @Override
    protected double[] propagateInputs(double[] inputValues) {
        double[] vals = Arrays.copyOf(inputValues, inputValues.length);
        for (int i = 0; i < vals.length; i++) {
            vals[i] = vals[i] * 2 - 1;
        }
        NetworkInput in = new CSVInput(vals);
        NetworkOutputSet output = network.execute(in);
        return output.nextOutput().values();
    }

    @Override
    public void reset() {
        NEATNeuralNet newNet = new NEATNeuralNet();
        newNet.createNetStructure(network.netDescriptor());
        newNet.updateNetStructure();
        this.network = newNet;
    }

    public static NEATNeuralNet deserialize(String ser) {
        String[] split = ser.split(",");
        double[] stuff = new double[split.length];
        for (int i = 0 ; i < stuff.length ; i++) {
            stuff[i] = Double.parseDouble(split[i]);
        }
        return deserialize(stuff);
    }

    public static NEATNeuralNet deserialize(double[] weights) {
        ArrayList<Gene> genes = new ArrayList<Gene>();
        for (int i = 0; i < weights.length; ) {
            double type = weights[i++];
            if (type == NODE) {
                int id = (int) weights[i++];
                double sigF = weights[i++];
                int t = (int) weights[i++];
                double bias = weights[i++];
                genes.add(new NEATNodeGene(0, id, sigF, t, bias));
            } else if (type == LINK) {
                boolean enabled = (weights[i++] == 1d);
                int from = (int) weights[i++];
                int to = (int) weights[i++];
                double weight = weights[i++];
                genes.add(new NEATLinkGene(0, enabled, from, to, weight));
            }
        }
        Gene[] geneArray = new Gene[genes.size()];
        genes.toArray(geneArray);
        NEATChromosome chromo = new NEATChromosome(geneArray);
        NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
        descr.updateStructure(chromo);
        NEATNeuralNet network = new NEATNeuralNet();
        network.createNetStructure(descr);
        network.updateNetStructure();
        return network;
    }

    @Override
    public void setWeights(double[] weights) {
        this.network = deserialize(weights);
    }
}
