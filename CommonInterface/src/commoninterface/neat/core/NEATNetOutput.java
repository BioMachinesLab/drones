/*
 * Created on Oct 6, 2004
 *
 */
package commoninterface.neat.core;

import commoninterface.neat.data.core.NetworkOutput;

/**
 * @author MSimmerson
 *
 *         Contains the output values of a NEAT neural net
 */
public class NEATNetOutput implements NetworkOutput {
	private static final long serialVersionUID = 7684740298768673866L;
	private double[] netOutputs;

	public NEATNetOutput(double[] outputs) {
		this.netOutputs = new double[outputs.length];
		System.arraycopy(outputs, 0, this.netOutputs, 0, this.netOutputs.length);
	}

	@Override
	public double[] values() {
		return (this.netOutputs);
	}

}
