/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core;


/**
 * @author MSimmerson
 * 
 * Link gene for a NEAT network
 */
public class NEATLinkGene implements NEATGene {
	private int innovationNumber;
	private boolean enabled;
	private int fromId;
	private int toId;
	private double weight;
	private boolean selfRecurrent = false;
	private boolean recurrent = false;
	
	/**
	 * @return Returns the recurrent.
	 */
	public boolean isRecurrent() {
		return recurrent;
	}
	/**
	 * @param recurrent The recurrent to set.
	 */
	public void setRecurrent(boolean recurrent) {
		this.recurrent = recurrent;
	}
	/**
	 * @return Returns the selfRecurrent.
	 */
	public boolean isSelfRecurrent() {
		return selfRecurrent;
	}
	/**
	 * @param selfRecurrent The selfRecurrent to set.
	 */
	public void setSelfRecurrent(boolean selfRecurrent) {
		this.selfRecurrent = selfRecurrent;
	}
	/**
	 * @param enabled The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (this.enabled == false) {			
			int i = 0;
		}
	}
	/**
	 * @param fromId The fromId to set.
	 */
	public void setFromId(int fromId) {
		this.fromId = fromId;
	}
	/**
	 * @param toId The toId to set.
	 */
	public void setToId(int toId) {
		this.toId = toId;
	}
	/**
	 * @param weight The weight to set.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @return Returns the fromId.
	 */
	public int getFromId() {
		return fromId;
	}
	/**
	 * @return Returns the innovationNumber.
	 */
	public int getInnovationNumber() {
		return innovationNumber;
	}
	/**
	 * @return Returns the toId.
	 */
	public int getToId() {
		return toId;
	}
	/**
	 * @return Returns the weight.
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * Creates the gene based on the params
	 * @param innovationNumber
	 * @param enabled
	 * @param fromId
	 * @param toId
	 * @param weight
	 */
	public NEATLinkGene(int innovationNumber, boolean enabled, int fromId, int toId, double weight) {
		this.innovationNumber = innovationNumber;
		this.setEnabled(enabled);
		this.fromId = fromId;
		this.toId = toId;
		this.weight = weight;
	}
	
	/**
	 * Not used within NEAT.  
	 */
	public Number geneAsNumber() {
		return (new Integer(this.innovationNumber));
	}

	public String geneAsString() {
		return (this.innovationNumber + ":" + 
				this.enabled + ":" + 
				this.fromId + ":" +
				this.toId + ":" + 
				this.weight);
	}
}
