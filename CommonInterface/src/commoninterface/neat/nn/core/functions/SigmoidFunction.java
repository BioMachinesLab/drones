/*
 * Created on Sep 29, 2004
 *
 */
package commoninterface.neat.nn.core.functions;

import commoninterface.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 *
 */
public class SigmoidFunction implements ActivationFunction {
	private static final long serialVersionUID = -1441163373440029055L;
	private double factor;

	public SigmoidFunction() {
		this(-4.9);
	}

	public SigmoidFunction(double factor) {
		this.factor = factor;
	}

	/**
	 * Returns +/- 1
	 */
	@Override
	public double activate(double neuronIp) {
		return (1.0 / (1.0 + Math.exp(this.factor * neuronIp)));
	}

	@Override
	public double derivative(double neuronIp) {
		return (neuronIp * (1 - neuronIp));
	}

	public void setFactor(double mod) {
		this.factor = mod;
	}
}
