/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core;


/**
 * @author MSImmerson
 *
 * Gene that describes a NEAT node (neuron)
 */
public class NEATNodeGene implements NEATGene {
	private int innovationNumber;
	private int id;
	private double sigmoidFactor = -1.0;
	private int type;
	private double depth;
	private double bias;
	public static final int HIDDEN = 0;
	public static final int OUTPUT = 1;
	public static final int INPUT = 2;
	
	public NEATNodeGene(int innovationNumber, int id, double sigmoidF, int type, double bias) {
		this.innovationNumber = innovationNumber;
		this.id = id;
		this.sigmoidFactor = sigmoidF;
		this.type = type;
		this.bias = bias;
		this.initialiseDepth();
	}
	
	private void initialiseDepth() {
		if (this.type == INPUT) {
			this.depth = 0;
		} else if (this.type == OUTPUT) {
			this.depth = 1;
		}
	}
	
	/**
	 * @return Returns the depth.
	 */
	public double getDepth() {
		return depth;
	}
	/**
	 * @param depth The depth to set.
	 */
	public void setDepth(double depth) {
		this.depth = depth;
	}

	public void setSigmoidFactor(double bias) {
		this.sigmoidFactor = bias;
	}
	
	public int getType() {
		return type;
	}

	public int getInnovationNumber() {
		return (this.innovationNumber);
	}
	
	public int id() {
		return (this.id);
	}

	public double sigmoidFactor() {
		return (this.sigmoidFactor);
	}
	
	public Number geneAsNumber() {
		return (new Integer(this.innovationNumber));
	}

	public String geneAsString() {
		return (this.innovationNumber + ":" + this.id + ":" + this.sigmoidFactor);
	}

	public double bias() {
		return bias;
	}

	public void setBias(double bias) {
		this.bias = bias;
	}

}
