package commoninterface.neat.nn.core;

import java.io.Serializable;

/**
 * @author msimmerson
 *
 */
public interface ActivationFunction extends Serializable
{
	public double activate(double neuronIp);
	public double derivative(double neuronIp);
}
