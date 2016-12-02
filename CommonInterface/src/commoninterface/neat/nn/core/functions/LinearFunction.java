package commoninterface.neat.nn.core.functions;

import commoninterface.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 *
 */
public class LinearFunction implements ActivationFunction {
	private static final long serialVersionUID = -4953768922043363045L;

	@Override
	public double activate(double neuronIp) {
		return (neuronIp);
	}

	@Override
	public double derivative(double neuronIp) {
		return (neuronIp);
	}

}
