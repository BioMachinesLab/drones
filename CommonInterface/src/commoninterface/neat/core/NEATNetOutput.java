/*
 * Created on Oct 6, 2004
 *
 */
package commoninterface.neat.core;

import commoninterface.neat.data.core.NetworkOutput;

/**
 * @author MSimmerson
 *
 * Contains the output values of a NEAT neural net
 */
public class NEATNetOutput implements NetworkOutput {
	private double[] netOutputs;
	
	public NEATNetOutput(double[] outputs) {
		this.netOutputs = new double[outputs.length];
		System.arraycopy(outputs, 0, this.netOutputs, 0, this.netOutputs.length);
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutput#values()
	 */
	public double[] values() {
		return (this.netOutputs);
	}

}
