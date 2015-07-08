package commoninterface.neat.nn.core.functions;

import commoninterface.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 *
 */
public class LinearFunction implements ActivationFunction {

	public double activate(double neuronIp) {
		return (neuronIp);
	}

	public double derivative(double neuronIp) {
		return (neuronIp);
	}

}
