package commoninterface.neat.nn.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 * 
 * Used to describe a link between two neurons
 */
public class Synapse implements Serializable{
	private Neuron from;
	private Neuron to;
	private double weight;
	private boolean enabled;
	
	/**
	 * @return Returns the weight.
	 */
	public double getWeight() {
		return weight;
	}
	/**
	 * @param weight The weight to set.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	/**
	 * @return Returns the from.
	 */
	public Neuron getFrom() {
		return from;
	}
	/**
	 * @return Returns the to.
	 */
	public Neuron getTo() {
		return to;
	}
	
	public Synapse(Neuron from, Neuron to, double weight) { 
		this.from = from;
		this.to = to;
		this.weight = weight;
		this.enabled = true;
	}

	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
