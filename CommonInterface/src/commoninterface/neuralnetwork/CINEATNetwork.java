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
import java.util.Iterator;
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
        if(arguments.getArgumentIsDefined("weights")) {
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
        ArrayList<Double> stuff = new ArrayList<Double>();
        for (String s : split) {
            stuff.add(Double.parseDouble(s));
        }

        ArrayList<Gene> genes = new ArrayList<Gene>();
        Iterator<Double> iter = stuff.iterator();
        while (iter.hasNext()) {
            double type = iter.next();
            if (type == NODE) {
                int id = (int) (double) iter.next();
                double sigF = iter.next();
                int t = (int) (double) iter.next();
                double bias = iter.next();
                genes.add(new NEATNodeGene(0, id, sigF, t, bias));
            } else if (type == LINK) {
                boolean enabled = iter.next() == 1d;
                int from = (int) (double) iter.next();
                int to = (int) (double) iter.next();
                double weight = iter.next();
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
    	StringBuilder builder = new StringBuilder();
		for(double d : weights) {
			builder.append(d);
			builder.append(',');
		}
		this.network = deserialize(builder.toString());
    }
}
