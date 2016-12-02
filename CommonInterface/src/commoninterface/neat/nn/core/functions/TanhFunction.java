/*
 * Created on Sep 30, 2004
 *
 */
package commoninterface.neat.nn.core.functions;

import commoninterface.neat.nn.core.ActivationFunction;

/**
 * @author MSimmerson
 *
 */
public class TanhFunction implements ActivationFunction {
	private static final long serialVersionUID = 4598392980233429820L;

	@Override
	public double activate(double neuronIp) {
		double op;
		if (neuronIp < -20) {
			op = -1;
		} else if (neuronIp > 20) {
			op = 1;
		} else {
			op = (1 - Math.exp(-2 * neuronIp)) / (1 + Math.exp(-2 * neuronIp));
		}
		return (op);
	}

	@Override
	public double derivative(double neuronIp) {
		double deriv = 0;
		deriv = (1 - Math.pow(neuronIp, 2));
		return (deriv);
	}
}
